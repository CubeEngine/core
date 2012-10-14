package de.cubeisland.cubeengine.shout.interactions;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.scheduler.AnnouncementManager;
import de.cubeisland.cubeengine.shout.scheduler.MessageTask;
import de.cubeisland.cubeengine.shout.scheduler.Scheduler;

public class ShoutListener implements Listener{
	
	private Shout module;
	private AnnouncementManager aManager;
	private Scheduler scheduler;
	
	public ShoutListener(Shout module){
		this.module = module;
		this.aManager = module.getAManager();
		this.scheduler = module.getScheduler();
	}
	
	public void PlayerJoinEvent(PlayerJoinEvent event)
	{
		User user = module.getUserManager().getUser(event.getPlayer());
		scheduler.scheduleTask(new MessageTask(aManager, scheduler, user), aManager.getGCD(user));
	}
	
}
