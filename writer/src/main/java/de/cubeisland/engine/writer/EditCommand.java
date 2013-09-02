/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.writer;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;

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
            context.sendTranslated("&cEdit what?");
            return;
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

            user.sendTranslated("Your book is now unsigned and ready to be edited");
        }
        else
        {
            Block target = user.getTargetBlock(null, 10);

            if (target.getType() == Material.WALL_SIGN || target.getType() == Material.SIGN_POST)
            {
                Map<String, Object> params = context.getParams();
                if (params.size() < 1)
                {
                    context.sendTranslated("&cYou need to specify at least one parameter");
                    return;
                }
                Sign sign = (Sign)target.getState();
                String[] lines = sign.getLines();
                for (Entry<String, Object> entry : params.entrySet())
                {
                    lines[Integer.parseInt(entry.getKey()) - 1] = (String)entry.getValue();
                }
                SignChangeEvent event = new SignChangeEvent(sign.getBlock(), user, sign.getLines());
                context.getCore().getEventManager().fireEvent(event);
                if (event.isCancelled())
                {
                    context.sendTranslated("&cCould not change the sign!");
                    return;
                }
                for (int i = 0; i < 4; ++i)
                {
                    sign.setLine(i, lines[i]);
                }
                sign.update();
                user.sendTranslated("The sign has been changed");
            }
            else
            {
                user.sendTranslated("&cYou need to have a signed book in hand or be looking at a sign less than 10 blocks away");
            }
        }
    }
}
