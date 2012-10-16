package de.cubeisland.cubeengine.shout.scheduler;

import de.cubeisland.cubeengine.core.user.User;
import java.util.TimerTask;

public class MessageTask extends TimerTask
{
	
	AnnouncementManager aManager;
	Scheduler scheduler;
	String user;
	int runs;
	int nextExcecution;
	
	public MessageTask(AnnouncementManager aManager,Scheduler scheduler, User user)
	{
		this.aManager = aManager;
		this.scheduler = scheduler;
		this.user = user.getName();
		runs = 0;
		nextExcecution = 0;
	}
	
	public void run()
	{
		if(runs == nextExcecution)
		{
			if(aManager.getNext(user) != null){
				scheduler.queueMessage(user, aManager.getNext(user));
				this.nextExcecution = this.runs + aManager.getNextDelay(user);
			}
			this.nextExcecution = this.runs+1;
		}
		runs++;
	}
	
}