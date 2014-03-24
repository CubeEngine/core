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
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;

/**
 * A module to edit signs and/or unsign written books
 */
public class Writer extends Module
{
    @Override
    public void onEnable()
    {
        this.getCore().getCommandManager().registerCommands(this, this, ReflectedCommand.class);
    }

    @Command(names = {"edit", "rewrite"},
             desc = "Edit a sign or unsign a book",
             usage = "[1 \"line1\"] [2 \"line2\"] [3 \"line3\"] [4 \"line4\"] ",
             params = {@Param(names ={"1", "Line1"}),
                       @Param(names ={"2", "Line2"}),
                       @Param(names ={"3", "Line3"}),
                       @Param(names ={"4", "Line4"})})
    public void edit(ParameterizedContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendTranslated(MessageType.NEGATIVE, "Edit what?");
            return;
        }
        User user = (User)context.getSender();
        if (!this.unsignBook(user))
        {
            Map<String, Object> params = context.getParams();
            if (params.size() < 1)
            {
                context.sendTranslated(MessageType.NEGATIVE, "You need to specify at least one parameter to edit a sign!");
                context.sendTranslated(MessageType.NEGATIVE, "Or hold a signed book in your hand to edit it.");
                return;
            }
            if (!this.editSignInSight(user, params))
            {
                user.sendTranslated(MessageType.NEGATIVE, "You need to have a signed book in hand or be looking at a sign less than 10 blocks away!");
            }
        }
    }

    /**
     * Edits the sign the user is looking at
     *
     * @param user the user
     * @param params the parameters (only 1-4 are allowed as key)
     * @return false of there is no sign
     *
     * @throws java.lang.NumberFormatException when the parameter keys are not numbers
     * @throws java.lang.ArrayIndexOutOfBoundsException when the parameter keys are other numbers than 1-4
     */
    public boolean editSignInSight(User user, Map<String, Object> params)
    {
        Block target = user.getTargetBlock(null, 10);
        if (target.getType() == Material.WALL_SIGN || target.getType() == Material.SIGN_POST)
        {
            Sign sign = (Sign)target.getState();
            String[] lines = sign.getLines();
            for (Entry<String, Object> entry : params.entrySet())
            {
                lines[Integer.parseInt(entry.getKey()) - 1] = (String)entry.getValue();
            }
            SignChangeEvent event = new SignChangeEvent(sign.getBlock(), user, sign.getLines());
            user.getCore().getEventManager().fireEvent(event);
            if (event.isCancelled())
            {
                user.sendTranslated(MessageType.NEGATIVE, "Could not change the sign!");
                return true;
            }
            for (int i = 0; i < 4; ++i)
            {
                sign.setLine(i, lines[i]);
            }
            sign.update();
            user.sendTranslated(MessageType.POSITIVE, "The sign has been changed!");
            return true;
        }
        // No Sign in sight
        return false;
    }

    /**
     * Unsigns a written book in the hand of given user
     *
     * @param user the user
     * @return false if there is no written book in the hand of given user
     */
    public boolean unsignBook(User user)
    {
        if (user.getItemInHand().getType() == Material.WRITTEN_BOOK)
        {
            ItemStack item = user.getItemInHand();
            BookMeta meta = ((BookMeta)item.getItemMeta());
            meta.setAuthor("");
            meta.setTitle("");
            item.setItemMeta(meta);
            item.setType(Material.BOOK_AND_QUILL);
            user.sendTranslated(MessageType.POSITIVE, "Your book is now unsigned and ready to be edited.");
            return true;
        }
        return false;
    }
}
