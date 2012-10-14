package de.cubeisland.cubeengine.shout.scheduler;

import java.util.List;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.shout.Shout;

/*
 * Class to manage all the announcements and their recivers
 */
public class AnnouncementManager
{
	
	private Shout module;
	
	public AnnouncementManager(Shout module)
	{
		this.module = module;
	}
	
	/**
	 * Get all the announcements this user should receive.
	 * 
	 * @param	user	The user to get announcements of.
	 * @return			A list of all announcements that should be displayed to this user.
	 */
	public List<Announcement> getAnnouncemets(User user)
	{
		return null;
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
	public String getNext(User user)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Get the next delay for this users MessageTask
	 * @param	user	The user to get the next delay of.
	 * @return			The next delay that should be used from this users MessageTask.
	 * @see		MessageTask
	 */
	public int getNextDelay(User user)
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}
