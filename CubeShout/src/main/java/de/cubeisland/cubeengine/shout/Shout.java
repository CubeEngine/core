package de.cubeisland.cubeengine.shout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.shout.Exceptions.ShoutException;
import de.cubeisland.cubeengine.shout.interactions.ShoutCommand;
import de.cubeisland.cubeengine.shout.interactions.ShoutListener;
import de.cubeisland.cubeengine.shout.scheduler.AnnouncementManager;
import de.cubeisland.cubeengine.shout.scheduler.Scheduler;

public class Shout extends Module
{
	
	private AnnouncementManager aManager;
	private ShoutListener listener;
	private ShoutCommand command;
	private Scheduler scheduler;
	
	public static Shout instance;
	
	// TODO CubeRoles
	
    @Override
    public void onEnable()
    {
    	instance = this;
    	
    	this.getFileManager().dropResources(ShoutResource.values());
    	
    	this.scheduler = new Scheduler(this);
    	this.aManager = new AnnouncementManager(this);
    	this.listener = new ShoutListener(this);
    	this.command = new ShoutCommand(this);
    	
    	try{
    		loadAnnouncements();
    	}
    	catch (ShoutException ex)
    	{
    		this.getLogger().log(Level.SEVERE, ex.getMessage());
    	} catch (IOException ex) {
    		this.getLogger().log(Level.SEVERE, "Error while reading announcements. Going into zombe state.");
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
    		this.loadAnnouncement(f);
    	}
    	
    }
    
    private void loadAnnouncement(File f) throws IOException
    {
    	Map<String, String> messages = new HashMap<String, String>();
    	String world = "*";
    	int delay = 0;
    	String permNode = "*";
    	String group = "*";
    	if (f.isDirectory())
    	{
    		// TODO parse info.yml
    		
    		List<File> languages = new ArrayList<File>();
    		languages = Arrays.asList(f.listFiles());
    		for (File lang : languages)
    		{
    			if (lang.getName().contains("_") && lang.getName().endsWith(".txt"))
    			{
    				StringBuilder message = new StringBuilder();
    				for (String line : readFile(lang))
    				{
    					message.append(line + "\n");
    				}
    				messages.put(lang.getName().replace(".txt", ""), message.toString());
    			}
    		}
    		
        	aManager.addAnnouncement(messages, world, delay, permNode, group);
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
    private int parseDelay(String delayText) {
		String[] parts = delayText.split(" ", 2);
		int tmpdelay = Integer.parseInt(parts[1]);
		
		switch(parts[1].toLowerCase()){
			case "secounds":
				return tmpdelay * 20;
			case "minutes":
				return tmpdelay * 20 * 60;
			case "hours":
				return tmpdelay * 20 * 60 * 60;
			case "days":
				return tmpdelay * 20 * 60 * 60 * 24;
			
		}
    	
		return (Integer)null;
	}

	private List<String> readFile(File f) throws IOException
    {
    	List<String> lines = new ArrayList<String>();
    	
    	BufferedReader reader = new BufferedReader(new FileReader(f));
        String line;
        while ((line = reader.readLine()) != null)
        {
            lines.add(line.trim().replaceAll("&([a-f0-9])", "\u00A7$1"));
        }
        reader.close();
    	return lines;
    }
    
	public AnnouncementManager getAManager()
	{
		return this.aManager;
	}

	public Scheduler getScheduler()
	{
		return scheduler;
	}
	
}
