package de.cubeisland.cubeengine.shout.scheduler;

import de.cubeisland.cubeengine.core.user.User;

public class MessageTask implements Runnable
{
	
	AnnouncementManager aManager;
	Scheduler scheduler;
	User user;
	int runs;
	int nextExcecution;
	
	public MessageTask(AnnouncementManager aManager,Scheduler scheduler, User user){
		this.aManager = aManager;
		this.scheduler = scheduler;
		this.user = user;
		runs = 0;
		nextExcecution = 0;
	}
	
	public void run()
	{
		if(runs == nextExcecution)
		{
			scheduler.queueMessage(user, aManager.getNext(user));
			this.nextExcecution = this.runs + aManager.getNextDelay(user);
		}
		runs++;
	}
	
}