package de.cubeisland.cubeengine.shout.interactions;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.cubeisland.cubeengine.shout.Shout;

public class ShoutListener implements Listener{
	
	private Shout instance;
	
	public ShoutListener(){
		this.instance = Shout.instance;
	}
	
	public void PlayerJoinEvent(PlayerJoinEvent event)
	{
		
	}
	
}
