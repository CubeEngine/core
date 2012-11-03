package de.cubeisland.cubeengine.shout.task;

import de.cubeisland.cubeengine.core.user.User;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageTask extends TimerTask
{
	
	AnnouncementManager aManager;
	TaskManager taskManager;
	String user;
	int runs;
	int nextExcecution;
	
	public MessageTask(AnnouncementManager aManager,TaskManager scheduler, User user)
	{
		this.aManager = aManager;
		this.taskManager = scheduler;
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
			if(aManager.getNextMessage(user) != null){
				taskManager.queueMessage(user, aManager.getNextMessage(user));
				this.nextExcecution = this.runs + aManager.getNextDelay(user);
			}else{
				this.nextExcecution = this.runs+1;	
			}
		}
		runs++;
	}
	
}