package de.cubeisland.cubeengine.shout.interactions;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.cubeisland.cubeengine.shout.Shout;

public class ShoutListener implements Listener{
	
	private Shout module;
	
	public ShoutListener(Shout module){
		this.module = module;
	}
	
	public void PlayerJoinEvent(PlayerJoinEvent event)
	{
		
	}
	
}
