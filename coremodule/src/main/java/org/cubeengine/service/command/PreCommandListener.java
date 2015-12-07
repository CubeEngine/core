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
package org.cubeengine.service.command;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Optional;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.module.core.sponge.CoreModule;
import org.cubeengine.module.core.util.matcher.StringMatcher;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.command.CommandSource;

import static org.cubeengine.module.core.util.StringUtils.implode;
import static org.cubeengine.service.i18n.formatter.MessageType.NEGATIVE;
import static org.cubeengine.service.i18n.formatter.MessageType.NEUTRAL;
import static org.spongepowered.api.event.Order.POST;

public class PreCommandListener
{
    private final CoreModule core;
    private final StringMatcher stringMatcher;
    private Game game;
    private I18n i18n;

    public PreCommandListener(CoreModule core, I18n i18n, StringMatcher stringMatcher, Game game)
    {
        this.core = core;
        this.i18n = i18n;
        this.stringMatcher = stringMatcher;
        this.game = game;
    }

    @Listener(order = POST)
    private void handleCommand(SendCommandEvent event, @First Player player)
    {
        event.setCancelled(isCommandMissing(player, event.getCommand()));
    }

    private boolean isCommandMissing(CommandSource sender, String label)
    {
        label = label.trim();
        if (label.isEmpty())
        {
            return false;
        }
        //String label = explode(" ", label)[0].toLowerCase(Locale.ENGLISH);
        Set<String> aliases = game.getCommandManager().getAliases();
        if (!aliases.contains(label))
        {
            final Locale language = sender instanceof Player ? ((Player)sender).getLocale() : Locale.getDefault();
            List<String> matches = new LinkedList<>(stringMatcher.getBestMatches(label, aliases, 1));
            if (matches.size() > 0 && matches.size() <= this.core.getConfiguration().commands.maxCorrectionOffers)
            {
                if (matches.size() == 1)
                {
                    sender.sendMessage(Texts.of(i18n.translate(language, NEGATIVE,
                                                               "Couldn't find {input#command}. Did you mean {input#command}?",
                                                               label, matches.iterator().next())));
                }
                else
                {
                    Collections.sort(matches, String.CASE_INSENSITIVE_ORDER);
                    sender.sendMessage(Texts.of(i18n.translate(language, NEUTRAL,
                                                               "Did you mean one of these: {input#command}?", implode(
                            ", /", matches))));
                }
            }
            else
            {
                sender.sendMessage(Texts.of(i18n.translate(language, NEGATIVE,
                                                           "I couldn't find any command for {input#command} ...",
                                                           label)));
            }
            return true;
        }
        return false;
    }
}
