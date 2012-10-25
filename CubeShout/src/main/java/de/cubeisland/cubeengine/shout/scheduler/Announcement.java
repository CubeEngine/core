package de.cubeisland.cubeengine.shout.scheduler;

import java.util.Map;

/*
 * Class to represent an announcement.
 */
public class Announcement
{
	
	private String name;
	private String defaultLocale = "en_US";
	private String permNode;
	private String world;
	private Map<String, String> messages;
	//in ticks
	private int delay = 0;
	
	/**
	 * Constructor of Announcement
	 * 
	 * @param	defaultLocale	Default Locale for this message
	 * @param	messages		The message in different languages
	 * @param	delay			The delay after this message in ticks
	 */
	public Announcement(String name, String defaultLocale, String permNode, String world, Map<String, String> messages, int delay)
	{
		this.name = name;
		this.defaultLocale = defaultLocale;
		this.permNode = permNode;
		this.world = world;
		this.messages = messages;
		this.delay = delay;
	}
	
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
	 * Get the delay after this message
	 * @return The delay in ticks
	 */
	public int getDelay()
	{
		return delay;
	}

	/**
	 * Get the permission node for this announcement
	 * 
	 * @return	the permission node for this announcement
	 */
	public String getPermNode()
	{
		return permNode;
	}

	/**
	 * Get the world this announcement should be displayed in
	 * 
	 * @return	The world this announcement should be displayed in.
	 */
	public String getWorld()
	{
		return world;
	}

	public boolean hasWorld(String world) {
		return (getWorld().equals("*") || getWorld().equals(world));
	}

	public String getName() {
		return name;
	}
}
