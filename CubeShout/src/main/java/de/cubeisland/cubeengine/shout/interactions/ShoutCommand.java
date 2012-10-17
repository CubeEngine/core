package de.cubeisland.cubeengine.shout.interactions;

import org.bukkit.World;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.shout.Shout;

public class ShoutCommand{
	
	private Shout module;
	
	public ShoutCommand(Shout module){
		this.module = module;;
	}
	
    @Command(
    	names = {"broadcast", "say", "shout", "announce"},
    	min = 1,
    	desc = "Announce a message to players on the server",
    	usage = "<[World world] <message> | <Announcment-name> >",
    	params =
    	    {
    	        @Param(names = {"World", "W"},types = {String.class})
    	    }
    )
	public void broadcast(CommandContext context)
	{
		if (context.hasNamed("World") || context.indexedCount() > 1)
		{
			String[] message = (String[])context.getIndexed().toArray();
			World world = module.getCore().getServer().getWorld(context.getNamed("World", String.class));
			if (world != null)
			{
				for (Player p : world.getPlayers())
				{
					p.sendMessage(message);
				}
			}
			else
			{
				module.getCore().getServer().broadcastMessage(arrayToString(message));
			}
		}
		else if (context.indexedCount() == 1)
		{
			// TODO
		}
	}
	
    private String arrayToString(String[] array)
    {
    	StringBuilder result = new StringBuilder();
    	for (String s : array)
    	{
    		result.append(s);
    	}
    	return result.toString().trim();
    }
}
