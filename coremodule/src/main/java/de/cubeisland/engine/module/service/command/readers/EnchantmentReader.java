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
package de.cubeisland.engine.module.service.command.readers;

import java.util.stream.Collectors;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.SilentException;
import de.cubeisland.engine.butler.parameter.TooFewArgumentsException;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.DefaultValue;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.service.user.User;
import de.cubeisland.engine.module.core.util.ChatFormat;
import de.cubeisland.engine.module.core.util.matcher.EnchantMatcher;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;

import static de.cubeisland.engine.module.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.module.core.util.formatter.MessageType.NEUTRAL;
import static de.cubeisland.engine.module.core.util.formatter.MessageType.POSITIVE;

public class EnchantmentReader implements ArgumentReader<Enchantment>, DefaultValue<Enchantment>
{
    private GameRegistry registry;
    private EnchantMatcher enchantMatcher;

    public EnchantmentReader(EnchantMatcher enchantMatcher, Game game)
    {
        this.enchantMatcher = enchantMatcher;
        registry = game.getRegistry();
    }

    public static String getPossibleEnchantments(GameRegistry registry, ItemStack item)
    {
        String collect = registry.getAllOf(Enchantment.class).stream()
                                 .filter(e -> item == null || e.canBeAppliedToStack(item))
                                 .map(Enchantment::getName)
                                 .collect(Collectors.joining(ChatFormat.WHITE + ", " + ChatFormat.YELLOW));
        if (collect.isEmpty())
        {
            return null;
        }
        return ChatFormat.YELLOW + collect;
    }

    @Override
    public Enchantment read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String token = invocation.consume(1);
        Enchantment enchantment = enchantMatcher.enchantment(token);
        if (enchantment == null)
        {
            User sender = (User)invocation.getCommandSource();
            String possibleEnchs = getPossibleEnchantments(registry, sender.getItemInHand().orNull());

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
            throw new SilentException();
        }
        return enchantment;
    }

    @Override
    public Enchantment getDefault(CommandInvocation invocation)
    {
        User sender = (User)invocation.getCommandSource();
        sender.sendTranslated(POSITIVE, "Following Enchantments are availiable:\n{input#enchs}", getPossibleEnchantments(registry, null));
        throw new TooFewArgumentsException();
    }
}
