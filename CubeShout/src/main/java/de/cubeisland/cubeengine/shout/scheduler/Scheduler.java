package de.cubeisland.cubeengine.shout.scheduler;

import de.cubeisland.cubeengine.shout.Shout;

/*
 * Class to manage task based on the system time, not bukkit.
 */
public class Scheduler {
	
	private Shout instance;
	
	public Scheduler(){
		this.instance = Shout.instance;
	}
	
}
