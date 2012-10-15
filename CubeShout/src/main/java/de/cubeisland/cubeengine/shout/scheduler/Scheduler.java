package de.cubeisland.cubeengine.shout.scheduler;

import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.cubeisland.cubeengine.shout.Shout;

/**
 * Class to manage tasks based on the system time, not bukkits.
 */
public class Scheduler
{
	
	private Shout module;
	private Queue<Message> messageQueue = new ConcurrentLinkedQueue<Message>();
	
	public Scheduler(Shout module)
	{
		this.module = module;
		// TODO schedule task to bukkit for doing the queue;
	}
	
	/**
	 * Schedule a task based on system time.
	 * 
	 * @param	task	The task to scheduler
	 * @param	delay	Delay between each time this task in run, in ticks.
	 */
	public void scheduleTask(TimerTask task, int delay)
	{
		// TODO
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
	
}
