package net.novauniverse.capturetheflag.game.objects.flag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.Assert;

import net.md_5.bungee.api.ChatColor;
import net.novauniverse.capturetheflag.game.config.CTFTeam;
import net.novauniverse.capturetheflag.utils.CTFPlayerUtils;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.teams.Team;
import net.zeeraa.novacore.spigot.teams.TeamManager;
import net.zeeraa.novacore.spigot.utils.BannerBuilder;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;

public class CTFFlag {
	private final CTFTeam team;

	private ArmorStand stand;
	private FlagState state;
	private Player carrier;

	private Location lastLocation;
	private Location lastGroundLocation;

	private boolean lastCarrierFriendly;

	public CTFFlag(CTFTeam team) {
		this.team = team;
		this.state = FlagState.IN_BASE;
		this.carrier = null;
		this.stand = null;
		this.lastLocation = null;
		this.lastGroundLocation = null;
		this.lastCarrierFriendly = false;
	}

	public void setupArmorStand() {
		Log.trace("CTFFlag", "Spawning flag for team " + team.getTeam().getDisplayName());
		if (stand != null) {
			stand.remove();
		}
		stand = (ArmorStand) team.getWorld().spawnEntity(team.getFlagLocation(), EntityType.ARMOR_STAND);

		stand.setRemoveWhenFarAway(false);
		stand.setBasePlate(false);
		stand.setVisible(true);
		//stand.setGravity(true);
		stand.setGravity(false);
		stand.setCustomName(team.getTeam().getTeamColor() + team.getTeam().getDisplayName() + "'s" + ChatColor.GREEN + " flag");
		stand.setCustomNameVisible(true);

		lastLocation = team.getFlagLocation();
		lastGroundLocation = team.getFlagLocation();
		stand.teleport(team.getFlagLocation());

		if (state == FlagState.DEACTIVATED || state == FlagState.CAPTURED) {
			stand.teleport(new Location(team.getWorld(), 69420, 255, 0));
			stand.setGravity(false);
			state = FlagState.DEACTIVATED;
		} else if (state == FlagState.CAPTURED) {
			stand.teleport(new Location(team.getWorld(), 69420, 255, 0));
			stand.setGravity(false);
		}

		ItemStack flag = new BannerBuilder(team.getFlagColor()).setAmount(1).build();
		stand.setHelmet(flag);

	}

	public boolean isLastCarrierFriendly() {
		return lastCarrierFriendly;
	}

	public boolean isEntityStand(Entity entity) {
		if (stand != null) {
			return stand.getUniqueId().equals(entity.getUniqueId());
		}
		return false;
	}

	public boolean isCarrierEnemy() {
		if (carrier != null) {
			Team team = TeamManager.getTeamManager().getPlayerTeam(carrier.getUniqueId());
			if (team != null) {
				return !this.getTeam().getTeam().equals(team);
			}
			return true;
		} else {
			return false;
		}
	}

	public ArmorStand getStand() {
		return stand;
	}

	public CTFTeam getTeam() {
		return team;
	}

	public FlagState getState() {
		return state;
	}

	@Nullable
	public Player getCarrier() {
		return carrier;
	}

	public boolean hasCarrier() {
		return carrier != null;
	}

	public void setCarrier(@Nullable Player carrier) {
		if (carrier == null && this.carrier != null) {
			this.carrier.getInventory().setHelmet(ItemBuilder.AIR);
		}
		if (stand != null) {
			if (carrier == null) {
				state = FlagState.ON_GROUND;
				stand.setVisible(true);
				//stand.setGravity(true);
				stand.teleport(lastLocation);
			} else {
				state = FlagState.CARRIED;
				stand.setGravity(false);
				stand.teleport(new Location(team.getWorld(), 69420, 255, 0));
				carrier.getInventory().setHelmet(new BannerBuilder(team.getFlagColor()).setAmount(1).build());

				lastCarrierFriendly = team.isMember(carrier);
			}
		}
		this.carrier = carrier;
	}

	public void returnToLastGroundLocation() {
		if (lastGroundLocation != null) {
			if (stand != null) {
				dropOnGround();
				stand.teleport(lastGroundLocation);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void tick() {
		if (stand != null) {
			if (stand.isDead()) {
				setupArmorStand();
			}
		} else {
			setupArmorStand();
		}

		if (carrier != null) {
			lastLocation = carrier.getLocation();
			if (carrier.isOnGround() || CTFPlayerUtils.isCloseToGround(carrier)) {
				lastGroundLocation = carrier.getLocation();
			}
			// Location location = carrier.getLocation().clone();
			// location.setPitch(0F);
			// location.add(0D, CARRY_Y_OFFSET, 0D);
			// stand.teleport(location);
		}
	}

	public void pickUp(@Nonnull Player carrier) {
		Assert.assertNotNull(carrier);
		setCarrier(carrier);
		state = FlagState.CARRIED;
	}

	public void dropOnGround() {
		this.dropOnGround(false);
	}

	public void dropOnGround(boolean useLastGroundLocation) {
		if (useLastGroundLocation) {
			lastLocation = lastGroundLocation;
		}
		setCarrier(null);
		state = FlagState.ON_GROUND;
	}

	public void reclaim() {
		if (carrier != null) {
			setCarrier(null);
		}
		if (stand != null) {
			stand.teleport(team.getFlagLocation());
		}
		state = FlagState.IN_BASE;
	}

	public void deactivate() {
		Log.trace("CTFFlag", "Deactivating flag for team " + team.getTeam().getDisplayName());
		if (stand != null) {
			stand.setGravity(false);
			stand.setVisible(false);
			stand.teleport(new Location(team.getWorld(), 69420, 255, 0));
		}
		state = FlagState.DEACTIVATED;
	}

	public void capture() {
		setCarrier(null);
		state = FlagState.CAPTURED;
		deactivate();
	}

	public boolean isCarrier(Entity entity) {
		if (carrier != null) {
			return carrier.getUniqueId().equals(entity.getUniqueId());
		}
		return false;
	}
}