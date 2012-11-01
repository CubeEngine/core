package de.cubeisland.cubeengine.shout;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.InvalidConfigurationException;
import de.cubeisland.cubeengine.core.config.annotations.From;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.shout.Exceptions.ShoutException;
import de.cubeisland.cubeengine.shout.interactions.ShoutCommand;
import de.cubeisland.cubeengine.shout.interactions.ShoutListener;
import de.cubeisland.cubeengine.shout.task.AnnouncementConfiguration;
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
    	} catch (ShoutException ex) {
    		this.logger.log(Level.SEVERE, "Something went wrong while parsing the config! Going into zombie state.\n" + ex.getLocalizedMessage());
			if (this.getCore().isDebug())
			{
				ex.printStackTrace();
			}
			return;
    	} catch (IOException ex) {
    		this.logger.log(Level.SEVERE, "An IO error occurred while reading announcements. Going into zombie state.\n" + ex.getMessage());
			if (this.getCore().isDebug())
			{
				ex.printStackTrace();
			}
    		return;
		} catch (InvalidConfigurationException ex) {
			this.logger.log(Level.SEVERE, "Something went wrong while parsing the config! Going into zombie state.\n" + ex.getLocalizedMessage());
			if (this.getCore().isDebug())
			{
				ex.printStackTrace();
			}
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
