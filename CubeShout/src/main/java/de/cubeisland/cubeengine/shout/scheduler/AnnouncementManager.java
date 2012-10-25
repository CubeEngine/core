package de.cubeisland.cubeengine.shout.scheduler;

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

/*
 * Class to manage all the announcements and their receivers
 */
public class AnnouncementManager
{
	
	private Shout module;
	private Map<String, Queue<Announcement>> messages;
	private Map<String, Queue<Integer>> delays;
	private Map<String, String> worlds;
	private Set<Announcement> announcements;
	
	public AnnouncementManager(Shout module)
	{
		this.module = module;
		this.messages = new ConcurrentHashMap<String, Queue<Announcement>>();
		this.delays = new ConcurrentHashMap<String, Queue<Integer>>();
		this.worlds = new ConcurrentHashMap<String, String>();
		this.announcements = new HashSet<Announcement>();
	}
	
	/**
	 * Get all the announcements this user should receive.
	 * 
	 * @param	user	The user to get announcements of.
	 * @return			A list of all announcements that should be displayed to this user.
	 */
	public List<Announcement> getAnnouncemets(User user)
	{
		return Arrays.asList((Announcement[])messages.get(user.getName()).toArray());
	}

	/**
	 * Get the greatest common divisor of the delays form the announcements this user should receive.
	 *  
	 * @param 	user	The user to get the gcd of their announcements.
	 * @return			The gcd of the users announcements.
	 */
	public int getGCD(User user)
	{
		List<Announcement> announcements = this.getAnnouncemets(user);
		int[] delays = new int[announcements.size()];
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
	private int gcd(int[] ints)
	{
		int result = ints[0];
		
		for (int x = 1; x < ints.length; x++)
		{
			while (ints[x] > 0)
			{
				int t = ints[x];
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
		//TODO add language support
		return returnn.getMessage();
	}
	
	/**
	 * Get the next delay for this users MessageTask
	 * @param	user	The user to get the next delay of.
	 * @return			The next delay that should be used from this users MessageTask.
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
		return returnn.getDelay();
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
	 */
	public void addAnnouncement(String name, Map<String, String> messages, String world, int delay, String permNode, String group)
	{
		if ( !(world == "*" ||module.getCore().getServer().getWorld(world) != null))
		{
			// TODO throw exception
			return;
		}
		if (delay == 0)
		{
			// TODO throw exception
			return;
		}
		if (messages == null || !messages.containsKey("en_US"))
		{
			// TODO throw exception
			return;
		}
		if (!permNode.equals("*") || (permNode == null || permNode.isEmpty()))
		{
			// TODO throw exception
			return;
		}
		if (!group.equals("*") || (group == null || group.isEmpty()))
		{
			// TODO throw exception
			return;
		}
		
		this.announcements.add(new Announcement(name, module.getCore().getConfiguration().defaultLanguage, permNode, world, messages, delay));
	}
	
	/**
	 * initialize this users announcements
	 * 
	 * @param user	The user
	 */
	public void initializeUser(User user) {
		// Load what announcements should be displayed to the user
		for (Announcement a : announcements)
		{
			if (a.getPermNode().equals("*") || user.hasPermission(a.getPermNode()))// TODO CubeRoles
			{
				if (!messages.containsKey(user.getName()))
				{
					messages.put(user.getName(), new LinkedList<Announcement>());
				}
				messages.get(user.getName()).add(a);
				
				if(!delays.containsKey(user.getName()))
				{
					delays.put(user.getName(), new LinkedList<Integer>());
				}
				delays.get(user.getName()).add(a.getDelay());
			}
			
			if (module.getCore().isDebug())
			{
				module.logger.log(Level.INFO, user.getName() + " is now receiving message: " + a.getName());
			}
			
		}
		
	}

	public void setWorld(String user, String world) {
		worlds.put(user, world);
	}
	
}
