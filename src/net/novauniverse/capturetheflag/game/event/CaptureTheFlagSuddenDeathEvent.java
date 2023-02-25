package net.novauniverse.capturetheflag.game.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CaptureTheFlagSuddenDeathEvent extends Event {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	public CaptureTheFlagSuddenDeathEvent() {
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}