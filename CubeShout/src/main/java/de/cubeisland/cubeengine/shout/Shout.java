package de.cubeisland.cubeengine.shout;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.shout.interactions.ShoutCommand;
import de.cubeisland.cubeengine.shout.interactions.ShoutListener;
import de.cubeisland.cubeengine.shout.scheduler.AnnouncementManager;
import de.cubeisland.cubeengine.shout.scheduler.Scheduler;

public class Shout extends Module{
	
	private AnnouncementManager aManager;
	private ShoutListener listener;
	private ShoutCommand command;
	private Scheduler scheduler;
	
	public static Shout instance;
	
    @Override
    public void onEnable()
    {
    	instance = this;
    	
    	this.scheduler = new Scheduler(this);
    	
    	this.aManager = new AnnouncementManager(this);
    	// TODO load announcements
    	
    	this.listener = new ShoutListener(this);
    	this.registerListener(listener);
    	
    	this.command = new ShoutCommand(this);
    	this.registerCommands(command);
    	
    }
    
    @Override
    public void onDisable()
    {
    	
    }
	
}
