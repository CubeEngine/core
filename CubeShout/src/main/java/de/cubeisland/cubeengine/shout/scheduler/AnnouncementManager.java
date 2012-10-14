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
	
	public AnnouncementManager(Shout module){
		this.module = module;
	}
	
	public List<Announcement> getAnnouncemets(User u)
	{
		return null;
	}
}
