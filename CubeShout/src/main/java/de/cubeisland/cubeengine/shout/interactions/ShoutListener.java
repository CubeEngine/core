package de.cubeisland.cubeengine.shout.interactions;

import java.util.logging.Level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.task.AnnouncementManager;
import de.cubeisland.cubeengine.shout.task.MessageTask;
import de.cubeisland.cubeengine.shout.task.TaskManager;

public class ShoutListener implements Listener
{
	
	private Shout module;
	private AnnouncementManager aManager;
	private TaskManager scheduler;
	
	public ShoutListener(Shout module)
	{
		this.module = module;
		this.aManager = module.getAManager();
		this.scheduler = module.getScheduler();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void PlayerJoinEvent(PlayerJoinEvent event)
	{
		User user = module.getUserManager().getUser(event.getPlayer());
		
		if (module.getCore().isDebug())
		{
			module.logger.log(Level.INFO, "Loading user: " + user.getName());	
		}
		aManager.initializeUser(user);
		
		if (module.getCore().isDebug())
		{
			module.logger.log(Level.INFO, "User is initialized");
			module.logger.log(Level.INFO, String.format("Scheduling a task for: %s every %d ticks.", user.getName(), aManager.getGCD(user.getName())));
		}
		scheduler.scheduleTask(user.getName(), new MessageTask(aManager, scheduler, user), aManager.getGCD(user.getName()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void PlayerLeaveEvent(PlayerQuitEvent event)
	{
		scheduler.stopUser(event.getPlayer().getName());
		aManager.clean(event.getPlayer().getName());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void PlayerChangedWorld(PlayerChangedWorldEvent event)
	{
		aManager.setWorld(event.getPlayer().getName(), event.getPlayer().getWorld().getName());
	}
	
}
