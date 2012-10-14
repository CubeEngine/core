package de.cubeisland.cubeengine.shout.scheduler;

import java.util.List;
import java.util.Map;

import de.cubeisland.cubeengine.core.user.User;

/*
 * Class to represent an announcement.
 */
public class Announcement
{
	
	private String defaultLocale = "en_US";
	private Map<String, String> messages = null;
	private String permNode = null;
	//in ticks
	private int delay = 0;
	
	/**
	 * Get the message from this announcement in the default language, as specified by CubeEngine
	 * 
	 * @return 	The message for this announcement in default language
	 */
	public String getMessage()
	{
		return this.getMessage(defaultLocale);
	}
	
	/**
	 * Get the message from this announcement in a specified language
	 * 
	 * @param 	locale	The language to get the message in
	 * @return			The message in that language if exist.
	 */
	public String getMessage(String locale)
	{
		return messages.get(locale);
	}
	
	/**
	 * Get all users that should receive this message
	 * 
	 * @return 	All users that should receive this message
	 */
	public List<User> getReceivers()
	{
		return null;
	}
	
	/**
	 * Get the delay after this message
	 * @return The delay in ticks
	 */
	public int getDelay()
	{
		return delay;
	}
	
	/**
	 * Check if an user is a receiver of this message
	 * 
	 * @param 	user	User to check with
	 * @return			If the user is a receiver of this message
	 */
	public boolean isReciver(User user)
	{
		return (getReceivers().contains(user) || user.hasPermission(permNode));
	}
	
}
