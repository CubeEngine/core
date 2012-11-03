package de.cubeisland.cubeengine.shout.task;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.Exceptions.ShoutException;

/*
 * Class to manage all the announcements and their receivers
 */
public class AnnouncementManager
{
	
	private Shout module;
	private Map<String, Queue<Announcement>> messages;
	private Map<String, Queue<Long>> delays;
	private Map<String, String> worlds;
	public Set<Announcement> announcements;
	
	public AnnouncementManager(Shout module)
	{
		this.module = module;
		this.messages = new ConcurrentHashMap<String, Queue<Announcement>>();
		this.delays = new ConcurrentHashMap<String, Queue<Long>>();
		this.worlds = new ConcurrentHashMap<String, String>();
		this.announcements = new HashSet<Announcement>();
	}
	
	/**
	 * Get all the announcements this user should receive.
	 * 
	 * @param	user	The user to get announcements of.
	 * @return			A list of all announcements that should be displayed to this user.
	 */
	public List<Announcement> getAnnouncemets(String user)
	{

		Announcement[] aarray = new Announcement[announcements.size()];
		messages.get(user).toArray(aarray);
		return Arrays.asList(aarray);
	}

	/**
	 * Get the greatest common divisor of the delays form the announcements this user should receive.
	 *  
	 * @param 	user	The user to get the gcd of their announcements.
	 * @return			The gcd of the users announcements.
	 */
	public long getGreatestCommonDivisor(String user)
	{
		List<Announcement> announcements = this.getAnnouncemets(user);
		long[] delays = new long[announcements.size()];
		for (int x = 0; x < delays.length; x++)
		{
			delays[x] = announcements.get(x).getDelay();
		}
		return greatestCommonDivisor(delays);
	}
	
	/**
	 * Get the greatest common divisor of a list of integers.
	 *  
	 * @param	ints	The list to get the gcd from.
	 * @return			gcd of all the integers in the list.
	 */
	private long greatestCommonDivisor(long[] ints)
	{
		long result = ints[0];
		
		for (int x = 1; x < ints.length; x++)
		{
			while (ints[x] > 0)
			{
				long t = ints[x];
				ints[x] = result % ints[x];
				result = t;
			}
		}
		return result;
	}
	
	/**
	 * Get next message that should be displayed to this user.
	 * 
	 * @param	user	User to get the next message of.
	 * @return			The next message that should be displayed to the user.
	 */
	public String getNextMessage(String user)
	{
		User us = module.getUserManager().getUser(user);
		Announcement announcement = null;
		boolean used = false;
		//Skip all announcements that don't apply to this world.
		while (!used)
		{
			if(messages.get(user).element().hasWorld(worlds.get(user)))
			{
				announcement = messages.get(user).poll();
				messages.get(user).add(announcement);
				used = true;
			}
		}
		if (announcement == null){
			return null;
		}
		return announcement.getMessage(us.getLanguage());
	}
	
	/**
	 * Get the next delay for this users MessageTask
	 * @param	user	The user to get the next delay of.
	 * @return			The next delay that should be used for this users MessageTask in milliseconds.
	 * @see		MessageTask
	 */
	public int getNextDelay(String user)
	{
		Announcement announcement = null;
		boolean used = false;
		
		//Skip all announcements that don't apply to the users current world.
		while (!used)
		{
			if(messages.get(user).element().hasWorld(worlds.get(user)))
			{
				announcement = messages.get(user).poll();
				messages.get(user).add(announcement);
				used = true;
			}
		}
		if (announcement == null){
			return 0;
		}
		//TODO add language support
		return (int) (announcement.getDelay()/getGreatestCommonDivisor(user));
	}

	/**
	 * Adds an announcement.
	 * Most be done before ay player joins!
	 * 
	 * @param messages
	 * @param world
	 * @param delay
	 * @param permNode
	 * @param group
	 * @throws ShoutException if there is something wrong with the values
	 */
	public void addAnnouncement(String name, Map<String, String> messages, String world, long delay, String permNode, String group) throws ShoutException
	{
		if (world == null ||  module.getCore().getServer().getWorld(world) == null)
		{
			world = "*";
		}
		if (delay == 0)
		{
			throw new ShoutException("No valid delay for anouncement: " + name);
		}
		if (messages == null || !messages.containsKey("en_US"))
		{
			throw new ShoutException("No valid message for anouncement: " + name);
		}
		if (!permNode.equals("*") || (permNode == null || permNode.isEmpty()))
		{
			throw new ShoutException("No valid permission for anouncement: " + name);
		}
		if (!group.equals("*") || (group == null || group.isEmpty()))
		{
			throw new ShoutException("No valid group for anouncement: " + name);
		}
		
		this.announcements.add(new Announcement(name, module.getCore().getConfiguration().defaultLanguage, permNode, world, messages, delay));
	}
	
	/**
	 * initialize this users announcements
	 * 
	 * @param user	The user
	 */
	public void initializeUser(User user) {
		if (module.getCore().isDebug() && announcements.isEmpty())
		{
			module.logger.log(Level.INFO, "There is no announcements loaded!!");
		}
		
		// Load what announcements should be displayed to the user
		for (Announcement a : announcements)
		{
			if (module.getCore().isDebug())
			{
				module.logger.log(Level.INFO, "Checking with new announcement");
			}
			
			if (a.getPermNode().equals("*") || user.hasPermission(a.getPermNode()))// TODO CubeRoles
			{
				if (!messages.containsKey(user.getName()))
				{
					messages.put(user.getName(), new LinkedList<Announcement>());
				}
				messages.get(user.getName()).add(a);
				
				if(!delays.containsKey(user.getName()))
				{
					delays.put(user.getName(), new LinkedList<Long>());
				}
				delays.get(user.getName()).add(a.getDelay());
				
				worlds.put(user.getName(), user.getWorld().getName());
				
				if (module.getCore().isDebug())
				{
					module.logger.log(Level.INFO, user.getName() + " is now receiving message: " + a.getName());
				}
			} else {
				if (module.getCore().isDebug())
				{
					module.logger.log(Level.INFO, "The user did not have permission");
				}
			}
			
		}
		
	}

	public void setWorld(String user, String world) {
		worlds.put(user, world);
	}


	public void clean(String user) {
		messages.remove(user);
		delays.remove(user);
		worlds.remove(user);
	}
	
    public void loadAnnouncements() throws ShoutException
    {
    	File moduleFolder = module.getFolder();
    	
    	if (moduleFolder == null)
    	{
    		throw new ShoutException("The folder for this plugin does not exist or could not be created");
    	}
    	
    	List<File> announcements = new ArrayList<File>();
    	announcements = Arrays.asList(moduleFolder.listFiles());
    	
    	for (File f : announcements)
    	{
    		if (f.isDirectory())
    		{
        		if (module.getCore().isDebug())
        		{
        			module.logger.log(Level.INFO, "Loading announcement "+f.getName());
        		}
        		this.loadAnnouncement(f);	
    		}
    	}
    	
    }
    
    private void loadAnnouncement(File f) throws ShoutException
    {
    	if (f.isFile())
    	{
    		throw new ShoutException("Tried to load an announcement that was a file!");
    	}
    	Map<String, String> messages = new HashMap<String, String>();
    	String world = "*";
    	long delay = 0;
    	String permNode = "*";
    	String group = "*";
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
		
		try {
    		for (File lang : languages)
    		{
				StringBuilder message = new StringBuilder();
				for (String line : FileUtil.readStringList(lang))
				{
					message.append(line + "\n");
				}
				messages.put(lang.getName().replace(".txt", ""), message.toString());
    		}
		} catch (IOException ex) {
			throw new ShoutException("Error while reading one of the language files for announcement: " + f.getName(), ex);
		}

		if (module.getCore().isDebug())
		{
			module.logger.log(Level.INFO, "Languages: "+ messages.keySet().toString());
			module.logger.log(Level.INFO, "World: " +  world);
			module.logger.log(Level.INFO, "Delay(in millisecounds): " +  delay);
			module.logger.log(Level.INFO, "Permission: " +  permNode);
			module.logger.log(Level.INFO, "Group: " +  group);
		}
    	this.addAnnouncement(f.getName(), messages, world, delay, permNode, group);
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
    long parseDelay(String delayText) {
		String[] parts = delayText.split(" ", 2);
		int tmpdelay = Integer.parseInt(parts[0]);
		String unit = parts[1].toLowerCase();
		if (unit.equalsIgnoreCase("secounds") || unit.equalsIgnoreCase("secound"))
		{
			return tmpdelay * 1000;
		}
		else if (unit.equalsIgnoreCase("minutes") || unit.equalsIgnoreCase("minute"))
		{
			return tmpdelay * 60 * 1000;
		}
		else if (unit.equalsIgnoreCase("hours") || unit.equalsIgnoreCase("hour"))
		{
			return tmpdelay * 60 * 60 * 1000;
		}
		else if (unit.equalsIgnoreCase("days") || unit.equalsIgnoreCase("day"))
		{
			return tmpdelay * 24 * 60 * 60 * 1000;
		}
		return 0;
	}
	
}
