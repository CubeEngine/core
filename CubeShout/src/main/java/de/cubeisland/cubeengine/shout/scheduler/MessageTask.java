package de.cubeisland.cubeengine.shout.scheduler;

import de.cubeisland.cubeengine.core.user.User;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		this.runs = 0;
		this.nextExcecution = 0;
	}
	
	public void run()
	{
		Logger.getLogger("Debug").log(Level.INFO, "Task run "+ runs);
		Logger.getLogger("Debug").log(Level.INFO, "Next excecution "+ nextExcecution);
		if(this.runs == this.nextExcecution)
		{
			Logger.getLogger("Debug").log(Level.INFO, "excecutiong now!");
			if(aManager.getNext(user) != null){
				scheduler.queueMessage(user, aManager.getNext(user));
				this.nextExcecution = this.runs + aManager.getNextDelay(user);
			}else{
				this.nextExcecution = this.runs+1;	
			}
		}
		runs++;
	}
	
}