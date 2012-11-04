package de.cubeisland.cubeengine.shout.interactions;

import org.bukkit.permissions.PermissionDefault;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.task.Announcement;

public class ShoutSubCommands {
	
	private Shout module;
	
	public ShoutSubCommands(Shout module)
	{
		this.module = module;
	}
	
	@Command
	(
			desc = "List all announcements",
			permDefault = PermissionDefault.OP,
			permNode = "shout.list"
	)
	public void list(CommandContext context)
	{
		StringBuilder announcements = new StringBuilder();
		for (Announcement a : module.getAnnouncementManager().getAnnouncemets())
		{
			announcements.append(a.getName() + ", ");
		}
		
		context.sendMessage("shout", "Here is the list of announcements: %s", announcements.substring(0, announcements.length()-2));
	}
}
