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
package de.cubeisland.engine.core.command.readers;

import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.parameter.TooFewArgumentsException;
import de.cubeisland.engine.command.parameter.reader.ArgumentReader;
import de.cubeisland.engine.command.parameter.reader.DefaultProvider;
import de.cubeisland.engine.command.parameter.reader.ReaderException;
import de.cubeisland.engine.command.parameter.reader.ReaderManager;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.matcher.Match;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEUTRAL;
import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

public class EnchantmentReader implements ArgumentReader<Enchantment>, DefaultProvider<Enchantment>
{
    public static String getPossibleEnchantments(ItemStack item)
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Enchantment enchantment : Enchantment.values())
        {
            if (item == null || enchantment.canEnchantItem(item))
            {
                if (first)
                {
                    sb.append(ChatFormat.YELLOW).append(Match.enchant().nameFor(enchantment));
                    first = false;
                }
                else
                {
                    sb.append(ChatFormat.WHITE).append(", ").append(ChatFormat.YELLOW).append(Match.enchant()
                                                                                                   .nameFor(enchantment));
                }
            }
        }
        if (sb.length() == 0)
        {
            return null;
        }
        return sb.toString();
    }

    @Override
    public Enchantment read(ReaderManager manager, Class type, CommandInvocation invocation) throws ReaderException
    {
        String token = invocation.consume(1);
        Enchantment enchantment = Match.enchant().enchantment(token);
        if (enchantment == null)
        {
            User sender = (User)invocation.getCommandSource();
            String possibleEnchs = getPossibleEnchantments(sender.getItemInHand());

            sender.sendTranslated(NEGATIVE, "Enchantment {input#enchantment} not found!", token);
            if (possibleEnchs != null)
            {
                sender.sendTranslated(NEUTRAL, "Try one of those instead:");
                sender.sendMessage(possibleEnchs);
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "You can not enchant this item!");
            }
            return null;
        }
        return enchantment;
    }

    @Override
    public Enchantment getDefault(CommandInvocation invocation)
    {
        User sender = (User)invocation.getCommandSource();
        sender.sendTranslated(POSITIVE, "Following Enchantments are availiable:\n{input#enchs}", EnchantmentReader.getPossibleEnchantments(null));
        throw new TooFewArgumentsException();
    }
}
