/*
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
package org.cubeengine.libcube.service.command.parser;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;
import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEUTRAL;
import static org.cubeengine.libcube.service.i18n.formatter.MessageType.POSITIVE;
import static org.spongepowered.api.text.action.TextActions.showText;
import static org.spongepowered.api.text.format.TextColors.YELLOW;

import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.exception.SilentException;
import org.cubeengine.butler.parameter.TooFewArgumentsException;
import org.cubeengine.butler.parameter.argument.ArgumentParser;
import org.cubeengine.butler.parameter.argument.Completer;
import org.cubeengine.butler.parameter.argument.DefaultValue;
import org.cubeengine.butler.parameter.argument.ParserException;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.matcher.EnchantMatcher;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.stream.Collectors;

public class EnchantmentParser implements ArgumentParser<EnchantmentType>, DefaultValue<EnchantmentType>, Completer
{
    private GameRegistry registry;
    private EnchantMatcher enchantMatcher;
    private I18n i18n;

    public EnchantmentParser(EnchantMatcher enchantMatcher, Game game, I18n i18n)
    {
        this.enchantMatcher = enchantMatcher;
        this.i18n = i18n;
        registry = game.getRegistry();
    }

    public static Text getPossibleEnchantments(ItemStack item)
    {
        List<Text> enchantments = Sponge.getRegistry().getAllOf(EnchantmentType.class).stream()
                                          .filter(e -> item == null || e.canBeAppliedToStack(item))
                                          .map(e -> Text.of(YELLOW, e.getTranslation().get().replace(" ", "")).toBuilder() // TODO getTranslation
                                                    .onHover(showText(Text.of(YELLOW, e.getId()))).build())
                                          .collect(Collectors.toList());
        if (enchantments.isEmpty())
        {
            return null;
        }
        return Text.joinWith(Text.of(TextColors.WHITE, ", "), enchantments.toArray(new Text[enchantments.size()]));
    }

    @Override
    public EnchantmentType parse(Class type, CommandInvocation invocation) throws ParserException
    {
        String token = invocation.consume(1);
        EnchantmentType enchantment = enchantMatcher.enchantment(token);
        if (enchantment == null)
        {
            CommandSource sender = (CommandSource)invocation.getCommandSource();
            Text possibleEnchs = getPossibleEnchantments(sender instanceof Player ? ((Player)sender).getItemInHand(HandTypes.MAIN_HAND).orElse(null) : null);

            i18n.send(sender, NEGATIVE, "Enchantment {input#enchantment} not found!", token);
            if (possibleEnchs != null)
            {
                i18n.send(sender, NEUTRAL, "Try one of those instead:");
                sender.sendMessage(possibleEnchs);
            }
            else
            {
                i18n.send(sender, NEGATIVE, "You can not enchant this item!");
            }
            throw new SilentException();
        }
        return enchantment;
    }

    @Override
    public EnchantmentType provide(CommandInvocation invocation)
    {
        CommandSource sender = (CommandSource)invocation.getCommandSource();
        i18n.send(sender, POSITIVE, "Following Enchantments are availiable:");
        sender.sendMessage(getPossibleEnchantments(null));
        throw new TooFewArgumentsException();
    }

    @Override
    public List<String> suggest(Class clazz, CommandInvocation invocation)
    {
        CommandSource sender = (CommandSource)invocation.getCommandSource();
        ItemStack item = sender instanceof Player ? ((Player) sender).getItemInHand(HandTypes.MAIN_HAND).orElse(null) : null;
        String token = invocation.currentToken();

        return Sponge.getRegistry().getAllOf(EnchantmentType.class).stream()
                .filter(e -> item == null || e.canBeAppliedToStack(item))
                .map(e -> e.getTranslation().get().replace(" ", ""))
                .filter(name -> name.toLowerCase().startsWith(token.toLowerCase()))
                .collect(Collectors.toList());
    }
}
