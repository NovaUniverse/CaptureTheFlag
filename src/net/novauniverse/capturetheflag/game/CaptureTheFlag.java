package net.novauniverse.capturetheflag.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.novauniverse.capturetheflag.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.DragType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.brunogamer.novacore.spigot.utils.ColorUtils;
import net.md_5.bungee.api.ChatColor;
import net.novauniverse.capturetheflag.game.config.CTFTeam;
import net.novauniverse.capturetheflag.game.config.CaptureTheFlagConfig;
import net.novauniverse.capturetheflag.game.event.CaptureTheFlagSuddenDeathEvent;
import net.novauniverse.capturetheflag.game.event.FlagCapturedEvent;
import net.novauniverse.capturetheflag.game.event.FlagRecoverEvent;
import net.novauniverse.capturetheflag.game.objects.flag.CTFFlag;
import net.novauniverse.capturetheflag.game.objects.flag.CTFRespawnTimer;
import net.novauniverse.capturetheflag.game.objects.flag.FlagState;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.tasks.Task;
import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.abstraction.VersionIndependentUtils;
import net.zeeraa.novacore.spigot.abstraction.enums.ColoredBlockType;
import net.zeeraa.novacore.spigot.abstraction.enums.VersionIndependentSound;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameEndReason;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.MapGame;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.elimination.PlayerEliminationReason;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.elimination.PlayerQuitEliminationAction;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.events.PlayerEliminatedEvent;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.events.TeamEliminatedEvent;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.triggers.GameTrigger;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.triggers.TriggerCallback;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.triggers.TriggerFlag;
import net.zeeraa.novacore.spigot.module.ModuleManager;
import net.zeeraa.novacore.spigot.module.modules.compass.CompassTracker;
import net.zeeraa.novacore.spigot.tasks.SimpleTask;
import net.zeeraa.novacore.spigot.tasks.TimeBasedTask;
import net.zeeraa.novacore.spigot.teams.Team;
import net.zeeraa.novacore.spigot.teams.TeamManager;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;
import net.zeeraa.novacore.spigot.utils.PlayerUtils;

public class CaptureTheFlag extends MapGame implements Listener {
	private boolean started;
	private boolean ended;

	private CaptureTheFlagConfig config;
	private List<CTFTeam> teams;

	private HashMap<Player, Inventory> inventoryPreset;

	private Task tickTask;
	private Task flagRespawnTask;
	private Task foodTask;

	private Task invSaveTask;
	private TimeBasedTask suddenDeathTask;

	private List<CTFRespawnTimer> respawnTimers;
	private List<Consumer<Player>> tpToSpawnCallbacks;

	private boolean suddenDeathActive;

	public CaptureTheFlag(Plugin plugin) {
		super(plugin);

		this.started = false;
		this.ended = false;

		this.teams = new ArrayList<>();
		this.respawnTimers = new ArrayList<>();
		this.tpToSpawnCallbacks = new ArrayList<>();
		this.suddenDeathTask = null;
		this.suddenDeathActive = false;
		this.inventoryPreset = new HashMap<>();

		this.foodTask = new SimpleTask(getPlugin(), () -> {
			Bukkit.getServer().getOnlinePlayers().forEach(p -> p.setFoodLevel(20));
		}, 10L);

		this.invSaveTask = new SimpleTask(getPlugin(), () -> teams.stream().forEach(ctfTeam -> ctfTeam.getTeam().getMembers().forEach(uuid -> {
			OfflinePlayer ofp = Bukkit.getOfflinePlayer(uuid);
			if (ofp.isOnline()) {
				Player player = ofp.getPlayer();
				if (player.getGameMode() != GameMode.SPECTATOR && player.getGameMode() != GameMode.CREATIVE && !player.isDead()) {
					Inventory iv = Bukkit.createInventory(null, 36);
					for (int i = 0; i < 36; i++) {
						iv.setItem(i, player.getInventory().getItem(i));
					}
					inventoryPreset.put(player, iv);
				}
			}
		})), 20L);

		this.tickTask = new SimpleTask(plugin, () -> {
			respawnTimers.forEach(CTFRespawnTimer::tick);

			teams.forEach(CTFTeam::tick);
			teams.stream().filter(CTFTeam::isFlagCarried).forEach(team -> {
				Player carrier = team.getFlag().getCarrier();
				carrier.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 0, false, false), true);
				if (team.getFlag().isCarrierEnemy()) {
					teams.stream().filter(t -> t.isMember(carrier)).findFirst().ifPresent(carrierTeam -> {
						if (carrierTeam.getFlagArea().isInside(carrier)) {
							carrierTeam.getTeam().sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Captured the flag of " + team.getTeam().getTeamColor() + ChatColor.BOLD + team.getTeam().getDisplayName());
							carrierTeam.getTeam().playSound(VersionIndependentSound.ORB_PICKUP);

							team.getTeam().sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Your teams flag was captured by " + carrierTeam.getTeam().getTeamColor() + ChatColor.BOLD + carrierTeam.getTeam().getDisplayName());
							team.getTeam().sendTitle(ChatColor.RED + "Flag captured", ChatColor.GREEN + "You can no longer respawn", 0, 60, 20);
							team.getTeam().playSound(VersionIndependentSound.WITHER_HURT);
							flagCaptureEffect(carrier.getLocation(), carrierTeam.getTeam());
							team.getFlag().capture();

							Bukkit.getServer().getPluginManager().callEvent(new FlagCapturedEvent(team.getFlag(), carrierTeam, carrier));
						}
					});
				} else {
					if (team.getFlagArea().isInside(carrier)) {
						// Team recover flag
						team.getTeam().playSound(VersionIndependentSound.ORB_PICKUP);
						team.getTeam().sendTitle(ChatColor.GREEN + "Flag recovered", ChatColor.GREEN + "Your teams flag was recovered by " + carrier.getName(), 0, 60, 20);
						flagCaptureEffect(carrier.getLocation(), team.getTeam());
						team.getFlag().reclaim();

						Bukkit.getServer().getPluginManager().callEvent(new FlagRecoverEvent(team.getFlag(), carrier));
					}
				}
			});

			if (config.isUseActionBar()) {
				teams.stream().filter(t -> t.getFlag().hasCarrier()).forEach(team -> {
					Player carrier = team.getFlag().getCarrier();
					VersionIndependentUtils.get().sendActionBarMessage(carrier, ChatColor.GREEN + "Carrying " + team.getTeam().getTeamColor() + team.getTeam().getDisplayName() + "'s" + ChatColor.GREEN + " flag");
				});
			}

			respawnTimers.removeIf(CTFRespawnTimer::shouldRemove);
		}, 0L);

		this.flagRespawnTask = new SimpleTask(plugin, () -> {
			teams.stream().filter(CTFTeam::isActive).forEach(team -> {
				if (team.getFlag().getStand() != null) {
					if (team.getFlag().getStand().getLocation().getBlockY() < config.getFlagTpBackY()) {
						team.getTeam().sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Your teams flag was respawned in your base since it was dropped in an inaccessible location");
						team.getFlag().reclaim();
					}
				}
			});
		}, 5L);
	}

	public void flagCaptureEffect(Location location, Team team) {
		Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
		FireworkMeta meta = firework.getFireworkMeta();
		meta.addEffect(FireworkEffect.builder().with(Type.BALL_LARGE).withColor(VersionIndependentUtils.get().bungeecordChatColorToBukkitColor(team.getTeamColor())).trail(true).build());
		firework.setFireworkMeta(meta);
		new BukkitRunnable() {
			@Override
			public void run() {
				firework.detonate();
			}
		}.runTaskLater(getPlugin(), 1L);
	}

	public CaptureTheFlagConfig getConfig() {
		return config;
	}

	public List<CTFTeam> getTeams() {
		return teams;
	}

	@Override
	public String getName() {
		return "capturetheflag";
	}

	@Override
	public String getDisplayName() {
		return "Capture The Flag";
	}

	@Override
	public PlayerQuitEliminationAction getPlayerQuitEliminationAction() {
		return PlayerQuitEliminationAction.DELAYED;
	}

	@Override
	public boolean eliminatePlayerOnDeath(Player player) {
		if (suddenDeathActive) {
			return true;
		}

		Team team = TeamManager.getTeamManager().getPlayerTeam(player);
		if (team != null) {
			return !teams.stream().anyMatch(t -> t.getTeam().equals(team) && t.hasFlag());
		}

		return true;
	}

	@Override
	public boolean isPVPEnabled() {
		return true;
	}

	@Override
	public boolean autoEndGame() {
		return true;
	}

	@Override
	public boolean hasStarted() {
		return started;
	}

	@Override
	public boolean hasEnded() {
		return ended;
	}

	@Override
	public boolean isFriendlyFireAllowed() {
		return false;
	}

	@Override
	public boolean canAttack(LivingEntity attacker, LivingEntity target) {
		return true;
	}

	@Nullable
	public CTFTeam getPlayerTeam(Player player) {
		return teams.stream().filter(t -> t.isMember(player)).findFirst().orElse(null);
	}

	public void delayedRespawn(Player player) {
		tpToSpectator(player);

		int respawnTime = getConfig().getRespawnTime();

		respawnTimers.removeIf(t -> t.getUuid().equals(player.getUniqueId()));
		respawnTimers.add(new CTFRespawnTimer(player.getUniqueId(), respawnTime * 20, this::tpToTeam));
	}

	public void tpToTeam(Player player) {
		TeamManager.getTeamManager().ifHasTeam(player, team -> {
			teams.stream().filter(t -> t.getTeam().equals(team)).findAny().ifPresent(ctfTeam -> {
				player.teleport(ctfTeam.getSpawnLocation());
				player.setHealth(20D);
				player.setFallDistance(0F);
				player.setFireTicks(0);
				player.setFoodLevel(20);
				player.setSaturation(20F);
				player.setGameMode(GameMode.SURVIVAL);

				PlayerUtils.resetMaxHealth(player);
				PlayerUtils.clearPlayerInventory(player);
				PlayerUtils.clearPotionEffects(player);
				PlayerUtils.resetPlayerXP(player);

				player.getInventory().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).setLeatherArmorColor(team.getTeamColor()).setAmount(1).setUnbreakable(true).build());
				if (!inventoryPreset.containsKey(player)) {
					player.getInventory().setItem(0, new ItemBuilder(Material.STONE_SWORD).setAmount(1).setUnbreakable(true).build());
					player.getInventory().setItem(1, new ItemBuilder(Material.SHEARS).setAmount(1).setUnbreakable(true).build());
					player.getInventory().setItem(2, new ItemBuilder(getWoolItemStack(player)).setAmount(16).build());
					player.getInventory().setItem(8, new ItemBuilder(Material.COMPASS).setAmount(1).setUnbreakable(true).build());
				} else {
					Inventory iv = inventoryPreset.get(player);
					for (int i = 0; i < 36; i++) {
						if (iv.getItem(i) != null) {
							if (iv.getItem(i).getType() == Material.WOOL) {
								player.getInventory().setItem(i, new ItemBuilder(getWoolItemStack(player)).setAmount(16).build());
							} else {
								player.getInventory().setItem(i, iv.getItem(i));
							}
						}
					}
					if (InventoryUtils.slotsWith(iv, Material.WOOL).isEmpty()) {
						player.getInventory().addItem(new ItemBuilder(getWoolItemStack(player)).setAmount(16).build());
					}
				}

				tpToSpawnCallbacks.forEach(c -> c.accept(player));

				new BukkitRunnable() {
					@Override
					public void run() {
						player.teleport(ctfTeam.getSpawnLocation());
					}
				}.runTaskLater(getPlugin(), 5L);
			});
		});
	}

	public ItemStack getWoolItemStack(Player player) {
		DyeColor color = DyeColor.WHITE;
		Team team = TeamManager.getTeamManager().getPlayerTeam(player);
		if (team != null) {
			color = ColorUtils.getDyeColorByChatColor(team.getTeamColor());
		}
		return new ItemBuilder(ColoredBlockType.WOOL, color).setAmount(1).build();
	}

	public boolean isSuddenDeathActive() {
		return suddenDeathActive;
	}

	public TimeBasedTask getSuddenDeathTask() {
		return suddenDeathTask;
	}

	@Override
	public void onStart() {
		if (started) {
			return;
		}

		ModuleManager.enable(CompassTracker.class);

		CaptureTheFlagConfig cfg = (CaptureTheFlagConfig) this.getActiveMap().getMapData().getMapModule(CaptureTheFlagConfig.class);
		if (cfg == null) {
			Log.fatal("ChickenOut", "The map " + this.getActiveMap().getMapData().getMapName() + " has no CaptureTheFlagConfig config map module");
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "CaptureTheFlagConfig has run into an uncorrectable error and has to be ended");
			this.endGame(GameEndReason.ERROR);
			return;
		}
		this.config = cfg;

		List<Team> teamsLeft = new ArrayList<>(TeamManager.getTeamManager().getTeams());

		Log.trace("CTF", "Initial teams left: " + teamsLeft.size());

		config.getConfiguredTeams().forEach(team -> {
			if (teamsLeft.size() > 0) {
				teams.add(new CTFTeam(team, teamsLeft.remove(0), world));
			}
		});

		Log.trace("CTF", "Final teams left: " + teamsLeft.size());

		if (teamsLeft.size() > 0) {
			Log.error("CaptureTheFlag", teamsLeft.size() + "/" + TeamManager.getTeamManager().getTeamCount() + " teams could not be set up due to the map not having enough configured spawn points. Some players might now spawn correctly. " + teamsLeft.size() + " teams left");
		}

		players.forEach(p -> {
			Player player = Bukkit.getPlayer(p);
			if (player != null) {
				Team team = TeamManager.getTeamManager().getPlayerTeam(player);
				if (team == null) {
					player.sendMessage(ChatColor.RED + "Failed to add you to the game since you are not in a team");
				} else {
					CTFTeam ctfTeam = teams.stream().filter(t -> t.getTeam().equals(team)).findAny().orElse(null);
					if (ctfTeam != null) {
						ctfTeam.setActive(true);
						tpToTeam(player);
					} else {
						player.sendMessage(ChatColor.RED + "Failed to add you to the game since the maps does not have enough spawn points");
					}
				}
			}
		});

		GameTrigger suddenDeathTrigger = new GameTrigger("ctf.suddendeath", new TriggerCallback() {
			@Override
			public void run(GameTrigger trigger, TriggerFlag reason) {
				startSuddenDeath();
			}
		});
		suddenDeathTrigger.setDescription("Instantly activate sudden death");
		suddenDeathTrigger.addFlag(TriggerFlag.RUN_ONLY_ONCE);
		addTrigger(suddenDeathTrigger);

		suddenDeathTask = new TimeBasedTask(this::startSuddenDeath, getPlugin(), config.getSuddenDeathTime() * 1000L, true);

		teams.stream().filter(t -> !t.isActive()).forEach(CTFTeam::deactivate);

		Task.tryStartTask(tickTask);
		Task.tryStartTask(flagRespawnTask);
		Task.tryStartTask(suddenDeathTask);
		Task.tryStartTask(foodTask);
		Task.tryStartTask(invSaveTask);

		started = true;

		VersionIndependentUtils.get().setGameRule(world, "doTileDrops", "false");

		if (config.isDebug()) {
			teams.forEach(team -> {
				team.getFlagLocation().getBlock().setType(Material.IRON_BLOCK);
				team.getSpawnLocation().getBlock().setType(Material.DIAMOND_BLOCK);
				team.getFlagArea().getOutline().forEach(v -> v.toLocation(world).getBlock().setType(Material.REDSTONE_BLOCK));
			});
		}

		sendBeginEvent();
	}

	public void startSuddenDeath() {
		if (suddenDeathActive) {
			return;
		}
		Task.tryStopTask(suddenDeathTask);
		suddenDeathActive = true;
		teams.forEach(ctfTeam -> {
			ctfTeam.getFlag().setCarrier(null);
			ctfTeam.deactivate();

		});
		VersionIndependentSound.WITHER_HURT.broadcast();
		VersionIndependentUtils.get().broadcastTitle(ChatColor.RED + "Sudden death", ChatColor.RED + "Players will no longer respawn", 0, 60, 20);
		Bukkit.getServer().broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Sudden death. Players will no longer respawn");
		Bukkit.getServer().getPluginManager().callEvent(new CaptureTheFlagSuddenDeathEvent());
	}

	@Override
	public void onEnd(GameEndReason reason) {
		if (!started || ended) {
			return;
		}

		Task.tryStopTask(tickTask);
		Task.tryStopTask(flagRespawnTask);
		Task.tryStopTask(suddenDeathTask);
		Task.tryStopTask(foodTask);
		Task.tryStopTask(invSaveTask);

		ended = true;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onTeamEliminated(TeamEliminatedEvent e) {
		teams.stream().filter(t -> t.getTeam().equals(e.getTeam()) && t.hasFlag()).findFirst().ifPresent(CTFTeam::deactivate);
	}

	public void handleStandInteraction(Player player, CTFTeam clickedFlagTeam) {
		if (getCarriedFlag(player) != null) {
			return;
		}

		if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) {
			return;
		}

		if (clickedFlagTeam.isMember(player)) {
			if (clickedFlagTeam.getFlagState() == FlagState.ON_GROUND) {
				VersionIndependentSound.ORB_PICKUP.play(player);
				player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Picked up your flag. Run back to your base to recover it");
				player.sendMessage(ChatColor.GOLD + "Use your compass to find your base");
				clickedFlagTeam.getFlag().setCarrier(player);
			}
		} else {
			if (clickedFlagTeam.getFlagState() == FlagState.ON_GROUND || clickedFlagTeam.getFlagState() == FlagState.IN_BASE) {
				ChatColor teamColor = TeamManager.getTeamManager().tryGetPlayerTeamColor(player, ChatColor.AQUA);
				String teamName = TeamManager.getTeamManager().tryGetTeamDisplayName(player, "Unknown");

				VersionIndependentSound.ORB_PICKUP.play(player);
				player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Picked up enemy flag. Run back to your base to capture it");
				player.sendMessage(ChatColor.GOLD + "Use your compass to find your base");

				clickedFlagTeam.getTeam().playSound(VersionIndependentSound.BLAZE_HIT);
				clickedFlagTeam.getTeam().sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Your flag was picked up by " + teamColor + ChatColor.BOLD + player.getName() + ChatColor.RED + ChatColor.BOLD + " from team " + teamColor + ChatColor.BOLD + teamName);
				clickedFlagTeam.getTeam().sendMessage(ChatColor.GOLD + "Use your compass to track your flag");
				clickedFlagTeam.getTeam().sendTitle(ChatColor.RED + "Flag picked up", ChatColor.RED + "Your flag was picked up by " + teamColor + player.getName(), 0, 60, 20);
				clickedFlagTeam.getFlag().setCarrier(player);
			}
		}
	}

	public boolean isCarryingFlag(Player player) {
		return this.getCarriedFlag(player) != null;
	}

	public CTFFlag getCarriedFlag(Player player) {
		CTFTeam team = teams.stream().filter(t -> t.getFlag().isCarrier(player)).findFirst().orElse(null);
		if (team != null) {
			return team.getFlag();
		}
		return null;
	}

	@Override
	public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		if (!started) {
			return;
		}
		final Player player = event.getPlayer();

		event.setRespawnLocation(getActiveMap().getSpectatorLocation());
		player.setGameMode(GameMode.SPECTATOR);

		Bukkit.getScheduler().scheduleSyncDelayedTask(NovaCore.getInstance(), () -> {
			Log.trace(getName(), "Calling tpToSpectator(" + player.getName() + ")");
			tpToSpectator(player);
			teams.stream().filter(t -> t.isMember(player)).findFirst().ifPresent(team -> {
				if (team.hasFlag()) {
					Log.trace(getName(), "Calling delayedRespawn(" + player.getName() + ")");
					delayedRespawn(player);
				}
			});
		}, 2L);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerEliminated(PlayerEliminatedEvent e) {
		if (e.getReason() == PlayerEliminationReason.DEATH) {
			return;
		}

		if (e.getReason() == PlayerEliminationReason.KILLED) {
			return;
		}

		PlayerUtils.ifOnline(e.getPlayer().getUniqueId(), player -> {
			CTFFlag flag = getCarriedFlag(player);
			if (flag != null) {
				flag.getTeam().getTeam().sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Your flag was dropped on the ground since the carrier was eliminated");
				flag.dropOnGround();
			}
		});
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if (started && !ended) {
			if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		e.setKeepInventory(true);
		e.getEntity().setGameMode(GameMode.SPECTATOR);
		CTFFlag carried = getCarriedFlag(e.getEntity());

		if (carried != null) {
			if (e.getEntity().getKiller() != null) {
				Player killer = e.getEntity().getKiller();
				if (carried.getTeam().isMember(killer)) {
					CTFFlag alreadyCarried = getCarriedFlag(killer);
					if (alreadyCarried != null) {
						alreadyCarried.dropOnGround(false);
					}
					VersionIndependentSound.ORB_PICKUP.play(killer);
					carried.setCarrier(killer);
					killer.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Picked up your teams flag. Run back to your base to reclaim it");
				} else {
					// Killed enemy carrying other flag
					carried.getTeam().getTeam().sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Your flag was dropped on the ground");
					carried.dropOnGround(true);
				}
			} else {
				carried.getTeam().getTeam().sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Your flag was dropped on the ground since the carrier died");
				carried.dropOnGround(true);
			}
		}
		e.getEntity().spigot().respawn();
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		e.getBlock().getDrops().clear();
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent e) {
		CTFFlag carried = getCarriedFlag(e.getPlayer());
		if (carried != null) {
			carried.dropOnGround();
			carried.getTeam().getTeam().sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Your flag was dropped on the ground since the carrier left");
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (suddenDeathActive) {
			return;
		}

		if (started && !ended) {
			Player player = e.getPlayer();

			CTFTeam team = getPlayerTeam(player);
			if (team != null) {
				if (!team.hasFlag()) {
					return;
				}
			}

			if (isPlayerInGame(player)) {
				delayedRespawn(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof ArmorStand) {
			CTFTeam team = teams.stream().filter(t -> t.getFlag().isEntityStand(e.getEntity())).findFirst().orElse(null);
			if (team != null) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onEntityDamagByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof ArmorStand && e.getDamager() instanceof Player) {
			if (((Player) e.getDamager()).getGameMode() == GameMode.SPECTATOR || ((Player) e.getDamager()).getGameMode() == GameMode.CREATIVE) {
				e.setCancelled(true);
				return;
			}
			CTFTeam team = teams.stream().filter(t -> t.getFlag().isEntityStand(e.getEntity())).findFirst().orElse(null);
			if (team != null) {
				handleStandInteraction((Player) e.getDamager(), team);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
		if (e.getRightClicked() instanceof ArmorStand) {
			if (e.getPlayer().getGameMode() == GameMode.SPECTATOR || e.getPlayer().getGameMode() == GameMode.CREATIVE) {
				e.setCancelled(true);
				return;
			}

			CTFTeam team = teams.stream().filter(t -> t.getFlag().isEntityStand(e.getRightClicked())).findFirst().orElse(null);
			if (team != null) {
				e.setCancelled(true);
				handleStandInteraction(e.getPlayer(), team);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {

		if (e.getRightClicked() != null) {
			if (e.getPlayer().getGameMode() == GameMode.SPECTATOR || e.getPlayer().getGameMode() == GameMode.CREATIVE) {
				e.setCancelled(true);
				return;
			}
			CTFTeam team = teams.stream().filter(t -> t.getFlag().isEntityStand(e.getRightClicked())).findFirst().orElse(null);
			if (team != null) {
				e.setCancelled(true);
				handleStandInteraction(e.getPlayer(), team);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e) {
		if (started && !ended) {
			if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
				e.setCancelled(true);
				return;
			}

			switch (e.getAction()) {
			case PLACE_ALL:
			case PICKUP_ALL:
			case HOTBAR_SWAP:
			case SWAP_WITH_CURSOR:
			case MOVE_TO_OTHER_INVENTORY:
			case HOTBAR_MOVE_AND_READD:
			case COLLECT_TO_CURSOR:
				e.setCancelled(false);
				break;
			default:
				e.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e) {

		if (e.getOldCursor().getType() == Material.WOOL) {
			if (e.getType() != DragType.EVEN || e.getInventorySlots().size() != 1) {
				e.setCancelled(true);
			}
		}
	}

	public List<Consumer<Player>> getTpToSpawnCallbacks() {
		return tpToSpawnCallbacks;
	}

	public void addPlayerTpToTeamCallback(Consumer<Player> callback) {
		tpToSpawnCallbacks.add(callback);
	}
}