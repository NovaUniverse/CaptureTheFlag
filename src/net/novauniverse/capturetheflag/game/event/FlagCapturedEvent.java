package net.novauniverse.capturetheflag.game.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.novauniverse.capturetheflag.game.config.CTFTeam;
import net.novauniverse.capturetheflag.game.objects.flag.CTFFlag;

public class FlagCapturedEvent extends Event {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	private final CTFFlag flag;
	private final CTFTeam carrierTeam;
	private final Player carrier;

	public FlagCapturedEvent(CTFFlag flag, CTFTeam carrierTeam, Player carrier) {
		this.flag = flag;
		this.carrierTeam = carrierTeam;
		this.carrier = carrier;
	}

	public CTFFlag getFlag() {
		return flag;
	}

	public CTFTeam getCarrierTeam() {
		return carrierTeam;
	}

	public Player getCarrier() {
		return carrier;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}