package de.cubeisland.cubeengine.shout;

import java.io.File;
import java.util.logging.Logger;

import de.cubeisland.cubeengine.core.config.annotations.From;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.shout.interactions.ShoutCommand;
import de.cubeisland.cubeengine.shout.interactions.ShoutListener;
import de.cubeisland.cubeengine.shout.interactions.ShoutSubCommands;
import de.cubeisland.cubeengine.shout.task.AnnouncementManager;
import de.cubeisland.cubeengine.shout.task.TaskManager;

public class Shout extends Module
{
	
	private AnnouncementManager announcementManager;
	private ShoutListener listener;
	private ShoutCommand command;
	private ShoutSubCommands subCommands;
	private TaskManager taskManager;
	@From
	private ShoutConfiguration config;
	
	public Logger logger;
	public File announcementFolder;
	
	// TODO CubeRoles
	
    @Override
    public void onEnable()
    {
    	this.logger = this.getLogger();
    	this.announcementFolder = this.getFolder();
    	this.getFileManager().dropResources(ShoutResource.values());
    	
    	this.taskManager = new TaskManager(this, config.initDelay, config.messagerPeriod);
    	this.announcementManager = new AnnouncementManager(this);
    	this.listener = new ShoutListener(this);
    	this.command = new ShoutCommand(this);
    	this.subCommands = new ShoutSubCommands(this);
    	
    	this.announcementManager.loadAnnouncements(this.announcementFolder); // Should we move this to a separate folder? config?
    	
    	this.registerListener(listener);
    	this.registerCommands(command);
    	this.registerCommands(subCommands, "shout");
    	
    }
    
    @Override
    public void onDisable()
    {
    	
    }
    
	public AnnouncementManager getAnnouncementManager()
	{
		return this.announcementManager;
	}

	public TaskManager getTaskManager()
	{
		return taskManager;
	}
	
}
