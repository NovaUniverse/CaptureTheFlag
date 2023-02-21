package net.novauniverse.capturetheflag.game.objects.flag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.junit.Assert;

import net.novauniverse.capturetheflag.game.config.CTFTeam;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.utils.BannerBuilder;

public class CTFFlag {
	private final CTFTeam team;

	private ArmorStand stand;
	private FlagState state;
	private Entity carrier;

	public CTFFlag(CTFTeam team) {
		this.team = team;
		this.state = FlagState.IN_BASE;
		this.carrier = null;
		this.stand = null;
	}

	public void setupArmorStand() {
		Log.trace("CTFFlag", "Spawning flag for team " + team.getTeam().getDisplayName());
		if (stand != null) {
			stand.remove();
		}
		stand = (ArmorStand) team.getWorld().spawnEntity(team.getFlagLocation(), EntityType.ARMOR_STAND);

		stand.setRemoveWhenFarAway(false);
		stand.setBasePlate(false);
		if (carrier == null) {
			stand.setVisible(true);
			stand.setGravity(true);
		} else {
			stand.setVisible(false);
			stand.setGravity(false);
		}
		stand.teleport(team.getFlagLocation());

		if (state == FlagState.DEACTIVATED || state == FlagState.CAPTURED) {
			stand.teleport(new Location(team.getWorld(), 69420, 0, 0));
			state = FlagState.DEACTIVATED;
		}

		ItemStack flag = new BannerBuilder(team.getFlagColor()).setAmount(1).build();
		stand.setHelmet(flag);

	}

	public boolean isEntityStand(Entity entity) {
		if (stand != null) {
			return stand.getUniqueId().equals(entity.getUniqueId());
		}
		return false;
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
	public Entity getCarrier() {
		return carrier;
	}

	public void setCarrier(@Nullable Entity carrier) {
		if (carrier == null) {
			stand.setVisible(true);
			stand.setGravity(true);
		} else {
			stand.setVisible(false);
			stand.setGravity(false);
		}
		this.carrier = carrier;
	}

	public void tick() {
		if (stand != null) {
			if (stand.isDead()) {
				setupArmorStand();
			}
		}

		if (carrier != null) {
			Location location = carrier.getLocation().clone();
			location.setPitch(0F);
			stand.teleport(location);
		}
	}

	public void capture(@Nonnull Entity carrier) {
		Assert.assertNotNull(carrier);
		setCarrier(carrier);
		state = FlagState.CARRIED;
	}

	public void dropOnGround() {
		setCarrier(null);
		state = FlagState.ON_GROUND;
	}

	public void reclaim() {
		if (carrier != null) {
			setCarrier(null);
		}
		stand.teleport(team.getFlagLocation());
		state = FlagState.IN_BASE;
	}

	public void deactivate() {
		Log.trace("CTFFlag", "Deactivating flag for team " + team.getTeam().getDisplayName());
		stand.setGravity(false);
		stand.setVisible(false);
		stand.teleport(new Location(team.getWorld(), 69420, 0, 0));
		state = FlagState.DEACTIVATED;
	}

	public void capture() {
		deactivate();
		state = FlagState.CAPTURED;
	}
}