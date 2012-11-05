package de.cubeisland.cubeengine.shout.interactions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.permissions.PermissionDefault;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.task.Announcement;
import de.cubeisland.cubeengine.shout.task.AnnouncementConfiguration;

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
	
	@Command
	(
			desc = "Create the structure for a new announcement",
			permDefault = PermissionDefault.OP,
			permNode = "shout.list",
			min = 1,
			params = 
			{
					@Param
					(
							names = {"delay", "d"},
							types = String.class
					),
					@Param
					(
							names = {"world", "w"},
							types = String.class
					),
					@Param
					(
							names = {"permission", "p"},
							types = Permission.class
					),
					@Param
					(
							names = {"group", "g"},
							types = String.class
					),
					@Param
					(
							names = {"message", "m"},
							types = String.class
					)
			}
	)
	public void create(CommandContext context)
	{
		try {
			String name = context.getIndexed(0, String.class);
			File folder = new File(module.announcementFolder, name);
			folder.mkdirs();
			File configFile = new File(folder, "announcement.yml");
			configFile.createNewFile();
			File language = new File(folder, "en_US.txt"); // TODO change for users/servers language
			language.createNewFile();
			
			AnnouncementConfiguration config = new AnnouncementConfiguration();
			config.delay = context.getNamed("delay", String.class, "10 minutes");
			config.world = context.getNamed("world", String.class, "*");
			config.permNode = context.getNamed("permission", String.class, "*");
			config.group = context.getNamed("group", String.class, "*");
			
			FileWriter fw = new FileWriter(language);
			BufferedWriter bw = new BufferedWriter(fw);
			
			String message = "";
			for (String s : (String[])context.getNamed("message"))
			{
				message += s;
			}
			bw.write(message);
			
			bw.close();
			fw.close();
			
			module.getAnnouncementManager().clean();
			
			context.sendMessage("shout", "Your announcement have been created and loaded into the plugin");
		}catch (IOException e) {
			context.sendMessage("shout", "Could not create some of the files or folders.");
			context.sendMessage("shout", "Please contact an administrator and tell him to check their console.");
			e.printStackTrace();
		} catch (ConversionException e) {} // This should never happen
		
		
	}

	@Command
	(
			desc = "clean all loaded announcements form memory and load from disk"
	)
	public void reload(CommandContext context)
	{
		module.getAnnouncementManager().clean();
		context.sendMessage("shout", "All players and announcements have now been reloaded");
	}
	
}
