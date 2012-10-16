package de.cubeisland.cubeengine.shout.scheduler;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.cubeisland.cubeengine.shout.Shout;

/**
 * Class to manage tasks based on the system time, not bukkits.
 */
public class Scheduler implements Runnable
{
	
	private Shout module;
	private Queue<Message> messageQueue;
	private Timer timer;
	
	public Scheduler(Shout module)
	{
		this.module = module;
		this.messageQueue = new ConcurrentLinkedQueue<Message>();
		this.timer = new Timer();
		
		//Schedule a task in main thread after 1 second with 1 second periods to take care of the messageQueue 
		module.getCore().getTaskManager().scheduleSyncRepeatingTask(module, this, 1000, 1000);
	}
	
	/**
	 * Schedule a task based on system time.
	 * 
	 * @param	task	The task to scheduler
	 * @param	delay	Delay between each time this task in run, in ticks.
	 */
	public void scheduleTask(TimerTask task, int delay)
	{
		timer.schedule(task, 1000L, delay*50);
	}
	
	/**
	 * Queue a message to be displayed to an user.
	 * 
	 * @param	user		User to display this message to.
	 * @param	message		the message to display to this user.
	 */
	public void queueMessage(String user, String message)
	{
		messageQueue.add(new Message(user, message));
	}
	
	/**
	 * Class to represent a message to be sent to a player.
	 */
	public class Message
	{
		String user;
		String message;
		
		public Message(String user, String message)
		{
			this.user = user;
			this.message = message;
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
