package de.cubeisland.cubeengine.shout;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.config.annotations.Revision;

@Codec("yml")
@Revision(1)
public class ShoutConfiguration extends Configuration {
	
	@Option("InitialDelay")
	@Comment("The delay after a player joins before he receives his first message, in millisecounds")
	public int initDelay = 20;
	
	@Option("MessagerPeriod")
	@Comment("The period the task that sends the messages should run at, in millisecounds")
	public int messagerPeriod = 40;
}
