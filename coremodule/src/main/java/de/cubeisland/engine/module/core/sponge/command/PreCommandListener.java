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
package de.cubeisland.engine.module.core.sponge.command;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import de.cubeisland.engine.module.core.i18n.I18n;
import de.cubeisland.engine.module.core.sponge.SpongeCore;
import de.cubeisland.engine.module.core.util.matcher.Match;
import de.cubeisland.engine.module.core.util.matcher.StringMatcher;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.message.CommandEvent;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandSource;

import static de.cubeisland.engine.module.core.util.StringUtils.implode;
import static de.cubeisland.engine.module.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.module.core.util.formatter.MessageType.NEUTRAL;
import static org.spongepowered.api.event.Order.POST;

public class PreCommandListener
{
    private final SpongeCore core;

    public PreCommandListener(SpongeCore core)
    {
        this.core = core;
    }

    @Subscribe(order = POST)
    private void handleCommand(CommandEvent event)
    {
        event.setCancelled(isCommandMissing(event.getSource(), event.getCommand()));
    }

    private boolean isCommandMissing(CommandSource sender, String label)
    {
        label = label.trim();
        if (label.isEmpty())
        {
            return false;
        }
        //String label = explode(" ", label)[0].toLowerCase(Locale.ENGLISH);
        Set<String> aliases = core.getGame().getCommandDispatcher().getAliases();
        if (!aliases.contains(label))
        {
            I18n i18n = core.getModularity().start(I18n.class);

            final Locale language = sender instanceof Player ? ((Player)sender).getLocale() : Locale.getDefault();
            List<String> matches = new LinkedList<>(core.getModularity().start(StringMatcher.class).getBestMatches(label, aliases, 1));
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
