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
package org.cubeengine.libcube.service.command.readers;

import java.util.List;
import java.util.stream.Collectors;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.exception.SilentException;
import org.cubeengine.butler.parameter.TooFewArgumentsException;
import org.cubeengine.butler.parameter.reader.ArgumentReader;
import org.cubeengine.butler.parameter.reader.DefaultValue;
import org.cubeengine.butler.parameter.reader.ReaderException;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.matcher.EnchantMatcher;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.*;
import static org.spongepowered.api.text.action.TextActions.showText;
import static org.spongepowered.api.text.format.TextColors.YELLOW;

public class EnchantmentReader implements ArgumentReader<Enchantment>, DefaultValue<Enchantment>
{
    private GameRegistry registry;
    private EnchantMatcher enchantMatcher;
    private I18n i18n;

    public EnchantmentReader(EnchantMatcher enchantMatcher, Game game, I18n i18n)
    {
        this.enchantMatcher = enchantMatcher;
        this.i18n = i18n;
        registry = game.getRegistry();
    }

    public static Text getPossibleEnchantments(GameRegistry registry, ItemStack item)
    {
        List<Text> enchantments = registry.getAllOf(Enchantment.class).stream()
                                          .filter(e -> item == null || e.canBeAppliedToStack(item))
                                          .map(e -> Text.of(YELLOW, e.getTranslation()).toBuilder()
                                                    .onHover(showText(Text.of(YELLOW, e.getId()))).build())
                                          .collect(Collectors.toList());
        if (enchantments.isEmpty())
        {
            return null;
        }
        return Text.joinWith(Text.of(TextColors.WHITE, ", "), enchantments.toArray(new Text[enchantments.size()]));
    }

    @Override
    public Enchantment read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String token = invocation.consume(1);
        Enchantment enchantment = enchantMatcher.enchantment(token);
        if (enchantment == null)
        {
            CommandSource sender = (CommandSource)invocation.getCommandSource();
            Text possibleEnchs = getPossibleEnchantments(registry, sender instanceof Player ? ((Player)sender).getItemInHand().orElse(null) : null);

            i18n.sendTranslated(sender, NEGATIVE, "Enchantment {input#enchantment} not found!", token);
            if (possibleEnchs != null)
            {
                i18n.sendTranslated(sender, NEUTRAL, "Try one of those instead:");
                sender.sendMessage(possibleEnchs);
            }
            else
            {
                i18n.sendTranslated(sender, NEGATIVE, "You can not enchant this item!");
            }
            throw new SilentException();
        }
        return enchantment;
    }

    @Override
    public Enchantment getDefault(CommandInvocation invocation)
    {
        CommandSource sender = (CommandSource)invocation.getCommandSource();
        i18n.sendTranslated(sender, POSITIVE, "Following Enchantments are availiable:");
        sender.sendMessage(getPossibleEnchantments(registry, null));
        throw new TooFewArgumentsException();
    }
}
