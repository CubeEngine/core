package de.cubeisland.cubeengine.shout.task;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

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
	public long getGCD(String user)
	{
		List<Announcement> announcements = this.getAnnouncemets(user);
		long[] delays = new long[announcements.size()];
		for (int x = 0; x < delays.length; x++)
		{
			delays[x] = announcements.get(x).getDelay();
		}
		return gcd(delays);
	}
	
	/**
	 * Get the greatest common divisor of a list of integers.
	 *  
	 * @param	ints	The list to get the gcd from.
	 * @return			gcd of all the integers in the list.
	 */
	private long gcd(long[] ints)
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
	public String getNext(String user)
	{
		User us = module.getUserManager().getUser(user);
		Announcement returnn = null;
		boolean used = false;
		//Skip all announcements that don't apply to this world.
		while (!used)
		{
			if(messages.get(user).element().hasWorld(worlds.get(user)))
			{
				returnn = messages.get(user).poll();
				messages.get(user).add(returnn);
				used = true;
			}
		}
		if (returnn == null){
			return null;
		}
		return returnn.getMessage(us.getLanguage());
	}
	
	/**
	 * Get the next delay for this users MessageTask
	 * @param	user	The user to get the next delay of.
	 * @return			The next delay that should be used for this users MessageTask in milliseconds.
	 * @see		MessageTask
	 */
	public int getNextDelay(String user)
	{
		Announcement returnn = null;
		boolean used = false;
		
		//Skip all announcements that don't apply to the users current world.
		while (!used)
		{
			if(messages.get(user).element().hasWorld(worlds.get(user)))
			{
				returnn = messages.get(user).poll();
				messages.get(user).add(returnn);
				used = true;
			}
		}
		if (returnn == null){
			return 0;
		}
		//TODO add language support
		return (int) (returnn.getDelay()/getGCD(user));
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
		if ( !(world.equals("*") ||module.getCore().getServer().getWorld(world) != null))
		{
			throw new ShoutException("No valid world for this anouncement");
		}
		if (delay == 0)
		{
			throw new ShoutException("No valid delay for this anouncement");
		}
		if (messages == null || !messages.containsKey("en_US"))
		{
			throw new ShoutException("No valid message for this anouncement");
		}
		if (!permNode.equals("*") || (permNode == null || permNode.isEmpty()))
		{
			throw new ShoutException("No valid permission for this anouncement");
		}
		if (!group.equals("*") || (group == null || group.isEmpty()))
		{
			throw new ShoutException("No valid group for this anouncement");
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
	
}
