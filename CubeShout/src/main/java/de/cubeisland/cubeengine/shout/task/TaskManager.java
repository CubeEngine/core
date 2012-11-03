package de.cubeisland.cubeengine.shout.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.shout.Shout;

/**
 * Class to manage tasks based on the system time, not bukkits.
 */
public class TaskManager implements Runnable
{
	
	private Shout module;
	private Queue<Message> messageQueue;
	private Timer timer;
	private Map<String, TimerTask> tasks;
	private int initDelay;
	private int messagerPeriod;
	
	public TaskManager(Shout module, int initDelay, int messagerPeriod)
	{
		this.module = module;
		this.messageQueue = new ConcurrentLinkedQueue<Message>();
		this.timer = new Timer();
		this.tasks = new HashMap<String, TimerTask>();
		this.initDelay = initDelay;
		this.messagerPeriod = messagerPeriod;
		
		//Schedule a task in main thread after 1 second with 1 second periods to take care of the messageQueue 
		module.getCore().getTaskManager().scheduleSyncRepeatingTask(module, this, 20, this.messagerPeriod/50);
	}
	
	/**
	 * Schedule a task based on system time.
	 * 
	 * @param	task	The task to scheduler
	 * @param	delay	Delay between each time this task in run.
	 */
	public void scheduleTask(String user, TimerTask task, long delay)
	{
		tasks.put(user, task);
		timer.schedule(task, this.initDelay, delay);
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
	 * Stops a user from receiving announcements
	 * @param user
	 */
	public void stopUser(String user)
	{
		tasks.get(user).cancel();
	}
	
	public void run() {
		if (!messageQueue.isEmpty())
		{
			Message m = messageQueue.poll();
			User u = 	module.getCore().getUserManager().getUser(m.user);
			if (u != null)
			{
				if(module.getCore().isDebug())
				{
					module.logger.log(Level.INFO, u.getName()+" Is now receiving a message");	
				}
				u.sendMessage(ChatFormat.parseFormats('&', m.message));
			}
		}
	}
	
	/**
	 * Class to represent a message to be sent to a player.
	 */
	private class Message
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
