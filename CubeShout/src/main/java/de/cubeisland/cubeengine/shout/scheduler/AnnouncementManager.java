package de.cubeisland.cubeengine.shout.scheduler;

import java.util.List;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.shout.Shout;

/*
 * Class to manage all the announcements and their recivers
 */
public class AnnouncementManager
{
	
	private Shout instance;
	
	public AnnouncementManager(){
		this.instance = Shout.instance;
	}
	
	public List<Announcement> getAnnouncemets(User u)
	{
		return null;
	}
}
