package de.cubeisland.cubeengine.shout;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.cubeisland.cubeengine.core.config.annotations.From;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.shout.interactions.ShoutCommand;
import de.cubeisland.cubeengine.shout.interactions.ShoutListener;
import de.cubeisland.cubeengine.shout.task.AnnouncementManager;
import de.cubeisland.cubeengine.shout.task.TaskManager;

public class Shout extends Module
{
	
	private AnnouncementManager aManager;
	private ShoutListener listener;
	private ShoutCommand command;
	private TaskManager scheduler;
	@From
	private ShoutConfiguration config;
	
	public Logger logger;
	
	// TODO CubeRoles
	
    @Override
    public void onEnable()
    {
    	this.logger = this.getLogger();
    	
    	if (this.getCore().isDebug())
    	{
    		this.logger.log(Level.INFO, "Enabling CubeShout");
    	}
    	
    	this.getFileManager().dropResources(ShoutResource.values());
    	
    	this.scheduler = new TaskManager(this, config.initDelay, config.messagerPeriod);
    	this.aManager = new AnnouncementManager(this);
    	this.listener = new ShoutListener(this);
    	this.command = new ShoutCommand(this);
    	
    	try{
    		this.aManager.loadAnnouncements();
    	} catch (Exception ex) {
    		this.logger.log(Level.SEVERE, "Something went wrong while parsing the announcements! The error message was: " + ex.getLocalizedMessage());
			if (this.getCore().isDebug())
			{
				ex.printStackTrace();
			}
    		this.logger.log(Level.WARNING, "The plugin is now going into zombie state");
			return;
    	}
    	
    	this.registerListener(listener);
    	this.registerCommands(command);
    	
    }
    
    @Override
    public void onDisable()
    {
    	
    }
    
	public AnnouncementManager getAManager()
	{
		return this.aManager;
	}

	public TaskManager getScheduler()
	{
		return scheduler;
	}
	
}
