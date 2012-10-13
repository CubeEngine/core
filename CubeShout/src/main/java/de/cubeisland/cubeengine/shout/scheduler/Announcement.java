package de.cubeisland.cubeengine.shout.scheduler;

import java.util.List;
import java.util.Map;

import de.cubeisland.cubeengine.core.user.User;

/*
 * Class to represent an announcement
 */
public class Announcement {
	
	private String defaultLocale = "en_US";
	private Map<String, String> messages = null;
	private String permNode = null;
	//in ticks
	private int delay = 0;
	
	public String getMessage(){
		return this.getMessage(defaultLocale);
	}

	public String getMessage(String locale) {
		return messages.get(locale);
	}
	
	public List<User> getRecivers(){
		return null;
	}
	
	public int getDelay(){
		return delay;
	}
	
	public boolean isReciver(User u){
		return (getRecivers().contains(u) || u.hasPermission(permNode));
	}
	
}
