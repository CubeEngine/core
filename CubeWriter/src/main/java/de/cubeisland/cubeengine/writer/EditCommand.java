package de.cubeisland.cubeengine.writer;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;

public class EditCommand 
{
	
	@Command(
			names = {"edit", "rewrite"}, 
			desc = "Edit a sign or a book",
			min = 0,
			max = 4,
			usage = "/edit Line1 \"This is line one\" 2 \"this line contains a number 2\" 3 \"This is line three\"",
			params = {
					@Param(
							names = {"1", "Line1"}, 
							types = {String.class}
						),
					@Param(
							names = {"2", "Line2"}, 
							types = {String.class}
						),
					@Param(
							names = {"3", "Line3"}, 
							types = {String.class}
						),
					@Param(
							names = {"4", "Line4"}, 
							types = {String.class}
						)
					}
		)
	public void edit(CommandContext context)
	{
		
		User user = context.getSenderAsUser("writer", "This command can only be used from ingame");
		
		if (user.getItemInHand().getType() == Material.WRITTEN_BOOK)
		{
			ItemStack item = user.getItemInHand();
			BookItem unsigned = new BookItem(item);
			unsigned.setAuthor("");
			unsigned.setTitle("");
			item = unsigned.getItemStack();
			item.setType(Material.BOOK_AND_QUILL);
			user.sendMessage("Your book is now unsigned and ready to be edited");
			
		}
		else
		{
			Block target = user.getTargetBlock(null, 10);
			
			if (!(target.getType() == Material.SIGN || target.getType() == Material.SIGN_POST))
			{
				user.sendMessage(ChatColor.RED + "You need to be looking at a sign less than 10 blocks away");
				return;
			}
			
			if (context.namedCount() < 1){
				user.sendMessage(ChatColor.RED + "You need to speccify at least one parameter");
				return;
			}
			
			Sign sign = (Sign)target.getState();
			
			Map<String, Object[]> params = context.getNamed();
			for (String key : params.keySet())
			{
				sign.setLine(Integer.parseInt(key) - 1, context.getNamed(key, String.class));
			}
			
			sign.update();
			
			user.sendMessage("The sign has been changed");	
		}
		
		
	}
	
}
