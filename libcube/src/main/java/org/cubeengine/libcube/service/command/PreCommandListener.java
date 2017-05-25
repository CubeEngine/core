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
package org.cubeengine.libcube.service.command;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.matcher.StringMatcher;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.cubeengine.libcube.util.StringUtils.implode;
import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;
import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEUTRAL;
import static org.spongepowered.api.event.Order.POST;

public class PreCommandListener
{
    public static final int MAX_CORRECTION_OFFERS = 5;
    private final StringMatcher stringMatcher;
    private I18n i18n;

    public PreCommandListener(I18n i18n, StringMatcher stringMatcher)
    {
        this.i18n = i18n;
        this.stringMatcher = stringMatcher;
    }

    @Listener(order = POST)
    public void handleCommand(SendCommandEvent event, @First CommandSource sender)
    {
        event.setCancelled(isCommandMissing(sender, event.getCommand()));
    }

    private boolean isCommandMissing(CommandSource sender, String label)
    {
        label = label.trim();
        if (label.isEmpty())
        {
            return false;
        }
        //String label = explode(" ", label)[0].toLowerCase(Locale.ENGLISH);
        Set<String> aliases = Sponge.getCommandManager().getAliases().stream().map(String::toLowerCase).collect(toSet());
        if (!aliases.contains(label.toLowerCase()))
        {
            final Locale language = sender.getLocale();
            List<String> matches = new LinkedList<>(stringMatcher.getBestMatches(label, aliases, 1));
            if (matches.size() > 0 && matches.size() <= MAX_CORRECTION_OFFERS) // TODO config
            {
                if (matches.size() == 1)
                {
                    sender.sendMessage(i18n.getTranslation(language, NEGATIVE,
                           "Couldn't find {input#command}. Did you mean {input#command}?",
                           label, matches.iterator().next()));
                }
                else
                {
                    Collections.sort(matches, String.CASE_INSENSITIVE_ORDER);
                    if (sender instanceof Player)
                    {
                        matches = matches.stream().map(m -> "/" + m).collect(toList());
                    }
                    sender.sendMessage(i18n.getTranslation(language, NEUTRAL,
                            "Did you mean one of these: {input#command}?", implode(", ", matches)));
                }
            }
            else
            {
                sender.sendMessage(i18n.getTranslation(language, NEGATIVE,
                        "I couldn't find any command for {input#command} ...",
                        label));
            }
            return true;
        }
        return false;
    }
}
