package net.novauniverse.capturetheflag.game.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.novauniverse.capturetheflag.game.objects.flag.CTFFlag;

public class FlagRecoverEvent extends Event {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	private final CTFFlag flag;
	private final Player carrier;

	public FlagRecoverEvent(CTFFlag flag, Player carrier) {
		this.flag = flag;
		this.carrier = carrier;
	}

	public CTFFlag getFlag() {
		return flag;
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