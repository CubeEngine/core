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
    		loadAnnouncements();
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

    private void loadAnnouncements() throws ShoutException, IOException
    {
    	File moduleFolder = this.getFolder();
    	
    	if (moduleFolder == null)
    	{
    		throw new ShoutException("The folder for this plugin does not exist or could not be created");
    	}
    	
    	List<File> announcements = new ArrayList<File>();
    	announcements = Arrays.asList(moduleFolder.listFiles());
    	
    	for (File f : announcements)
    	{
    		if (this.getCore().isDebug())
    		{
    			this.logger.log(Level.INFO, "Loading announcement "+f.getName());
    		}
    		this.loadAnnouncement(f);
    	}
    	
    }
    
    private void loadAnnouncement(File f) throws IOException, ShoutException
    {
    	Map<String, String> messages = new HashMap<String, String>();
    	String world = "*";
    	long delay = 0;
    	String permNode = "*";
    	String group = "*";
    	if (f.isDirectory())
    	{
    		AnnouncementConfiguration conf = Configuration.load(AnnouncementConfiguration.class, new File(f, "announcement.yml"));
    		if (conf == null)
    		{
    			throw new ShoutException("No configfile to announcement: "+ f.getName());
    		}
    		
    		world = conf.world;
    		permNode = conf.permNode;
    		group = conf.group;
    		delay = parseDelay(conf.delay);
    		
    		if (delay == 0)
    		{
    			throw new ShoutException("No valid delay in announcement: "+ f.getName());
    		}
    		
    		List<File> languages = new ArrayList<File>();
    		languages = Arrays.asList(f.listFiles((FilenameFilter)new FileExtentionFilter("txt")));
    		
    		
    		for (File lang : languages)
    		{
				StringBuilder message = new StringBuilder();
				for (String line : FileUtil.readStringList(lang))
				{
					message.append(line + "\n");
				}
				messages.put(lang.getName().replace(".txt", ""), message.toString());
    		}
    		if (this.getCore().isDebug())
    		{
    			this.logger.log(Level.INFO, "Languages: "+ messages.keySet().toString());
    			this.logger.log(Level.INFO, "World: " +  world);
    			this.logger.log(Level.INFO, "Delay(in millisecounds): " +  delay);
    			this.logger.log(Level.INFO, "Permission: " +  permNode);
    			this.logger.log(Level.INFO, "Group: " +  group);
    		}
        	aManager.addAnnouncement(f.getName(), messages, world, delay, permNode, group);
    	}
    }
    
    /**
     * parse a delay in this format:
     * 	10 minutes
     * to
     * 	1200 ticks
     * 
     * @param delayText	the text to parse
     * @return the delay in ticks
     */
    private long parseDelay(String delayText) {
		String[] parts = delayText.split(" ", 2);
		int tmpdelay = Integer.parseInt(parts[0]);
		
		switch(parts[1].toLowerCase()){
			case "secounds":
				return tmpdelay * 1000;
			case "minutes":
				return tmpdelay * 60 * 1000;
			case "hours":
				return tmpdelay * 60 * 60 * 100;
			case "days":
				return tmpdelay * 24 * 60 * 60 * 1000;
			case "secound":
				return tmpdelay * 1000;
			case "minute":
				return tmpdelay * 60 * 1000;
			case "hour":
				return tmpdelay * 60 * 60 * 100;
			case "day":
				return tmpdelay * 24 * 60 * 60 * 1000;
			
		}
    	
		return 0;
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
