package de.cubeisland.cubeengine.writer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;

public class EditCommand 
{
	
	@Command(
			names = {"edit", "rewrite"}, 
			desc = "Edit a sign or a book",
			min = 1,
			max = 4,
			usage = "Look at a sign and excecute command. Example:\n /edit Line1 This is line one 2 \"this line contains a number 2\" 3 This is line three",
			params = {
					@Param(
							names = {"Line1", "1"}, 
							types = {String.class}
						),
					@Param(
							names = {"Line2", "2"}, 
							types = {String.class}
						),
					@Param(
							names = {"Line3", "3"}, 
							types = {String.class}
						),
					@Param(
							names = {"Line3", "3"}, 
							types = {String.class}
						),
					}
		)
	public void edit(CommandContext context)
	{
		// TODO add books
		User user = context.getSenderAsUser();
		
		if (user == null)
		{
			context.getSender().sendMessage("This commands can only be used from ingame");
			return;
		}
		
		Block target = user.getTargetBlock(null, 10);
		
		if (!(target.getType() == Material.SIGN || target.getType() == Material.SIGN_POST))
		{
			user.sendMessage(ChatColor.RED + "You need to be looking at a sign less than 10 blocks away");
			return;
		}
		
		if (context.namedCount() < 0){
			user.sendMessage(ChatColor.RED + "You need to speccify at least one parameter");
			return;
		}
		
		Sign sign = (Sign)target;
		
		if (context.hasNamed("Line1"))
		{
			user.sendMessage("Line one is: " + context.getNamed("Line1", String.class));
			sign.setLine(1, context.getNamed("Line1", String.class));
		}
		if (context.hasNamed("Line2"))
		{
			user.sendMessage("Line two is: " + context.getNamed("Line2", String.class));
			sign.setLine(2, context.getNamed("Line2", String.class));
		}
		if (context.hasNamed("Line3"))
		{
			user.sendMessage("Line three is: " + context.getNamed("Line3", String.class));
			sign.setLine(3, context.getNamed("Line3", String.class));
		}
		if (context.hasNamed("Line4"))
		{
			user.sendMessage("Line four is: " + context.getNamed("Line4", String.class));
			sign.setLine(4, context.getNamed("Line4", String.class));
		}
	}
	
}
