package de.cubeisland.cubeengine.writer;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Map;
import java.util.Map.Entry;

public class EditCommand
{
    @Command(names = {
        "edit", "rewrite"
    }, desc = "Edit a sign or unsign a book", usage = "[Line1 \"text\"] [Line2 \"text\"] [Line3 \"text\"] [Line4 \"text\"] ", params = {
        @Param(names =
        {
            "1", "Line1"
        }),
        @Param(names =
        {
            "2", "Line2"
        }),
        @Param(names =
        {
            "3", "Line3"
        }),
        @Param(names =
        {
            "4", "Line4"
        })
    }, max = 0)
    public void edit(ParameterizedContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendMessage("writer", "This command can only be used by players");
        }
        User user = (User)context.getSender();

        if (user.getItemInHand().getType() == Material.WRITTEN_BOOK)
        {
            ItemStack item = user.getItemInHand();
            BookMeta meta = ((BookMeta)item.getItemMeta());
            meta.setAuthor("");
            meta.setTitle("");
            item.setItemMeta(meta);
            item.setType(Material.BOOK_AND_QUILL);

            user.sendMessage("writer", "Your book is now unsigned and ready to be edited");
        }
        else
        {
            Block target = user.getTargetBlock(null, 10);

            if (target.getType() == Material.WALL_SIGN || target.getType() == Material.SIGN_POST)
            {
                Map<String, Object> params = context.getParams();
                if (params.size() < 1)
                {
                    context.sendMessage("writer", "&cYou need to specify at least one parameter");
                    return;
                }
                Sign sign = (Sign)target.getState();
                String[] lines = sign.getLines();
                for (Entry<String, Object> entry : params.entrySet()) // TODO refactor
                {
                    lines[Integer.parseInt(entry.getKey()) - 1] = (String)entry.getValue();
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
