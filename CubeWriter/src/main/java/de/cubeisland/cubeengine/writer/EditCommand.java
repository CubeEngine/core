package de.cubeisland.cubeengine.writer;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.Argument;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;

public class EditCommand
{
    @Command(
    names = {"edit", "rewrite"},
    desc = "Edit a sign or unsign a book",
    usage = "[Line1 \"text\"] [Line2 \"text\"] [Line3 \"text\"] [Line4 \"text\"] ",
    params =
    {
        @Param(names = {"1", "Line1"}),
        @Param(names = {"2", "Line2"}),
        @Param(names = {"3", "Line3"}),
        @Param(names = {"4", "Line4"})
    })
    public void edit(CommandContext context)
    {
        User user = context.getSenderAsUser("writer", "This command can only be used by players");

        if (user.getItemInHand().getType() == Material.WRITTEN_BOOK)
        {
            ItemStack item = user.getItemInHand();
            BookItem unsigned = new BookItem(item);

            unsigned.setAuthor("");
            unsigned.setTitle("");

            item = unsigned.getItemStack();
            item.setType(Material.BOOK_AND_QUILL);

            user.sendMessage("writer", "Your book is now unsigned and ready to be edited");
        }
        else
        {
            Block target = user.getTargetBlock(null, 10);

            if (target.getType() == Material.WALL_SIGN || target.getType() == Material.SIGN_POST)
            {
                if (context.namedCount() < 1)
                {
                    invalidUsage(context, "writer", "&cYou need to specify at least one parameter");
                }
                Sign sign = (Sign)target.getState();
                String[] lines = sign.getLines();
                Map<String, Argument<?>> params = context.getNamed();
                for (String key : params.keySet()) // TODO refactor
                {
                    lines[Integer.parseInt(key) - 1] = context.getNamed(key, String.class); 
                }
                SignChangeEvent event = new SignChangeEvent(sign.getBlock(), user, sign.getLines());
                CubeEngine.getEventManager().fireEvent(event);
                if (event.isCancelled())
                {
                    context.sendMessage("basics", "&cCould not change the sign!");
                    return;
                }
                for (int i = 0; i < 4; ++i)
                {
                    sign.setLine(i, lines[i]);
                }
                sign.update();
                
                
                user.sendMessage("writer", "The sign has been changed");
            }
            else
            {
                user.sendMessage("writer", "&cYou need to have a signed book in hand or be looking at a sign less than 10 blocks away");
                if (context.getCore().isDebug())
                {
                    user.sendMessage("writer", "You where looking at: %s", target.getType().name());
                }
            }
        }
    }
}