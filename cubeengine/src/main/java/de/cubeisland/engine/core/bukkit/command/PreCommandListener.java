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
package de.cubeisland.engine.core.bukkit.command;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.bukkit.BukkitUtils;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.core.util.matcher.Match;

import static de.cubeisland.engine.core.util.StringUtils.explode;
import static de.cubeisland.engine.core.util.StringUtils.implode;

public class PreCommandListener implements Listener
{
    private final BukkitCore core;
    private final CommandInjector injector;

    public PreCommandListener(BukkitCore core)
    {
        this.core = core;
        this.injector = core.getCommandManager().getInjector();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void handleCommand(PlayerCommandPreprocessEvent event)
    {
        event.setCancelled(isCommandMissing(event.getPlayer(), event.getMessage().substring(1)));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void handleCommand(ServerCommandEvent event)
    {
        isCommandMissing(event.getSender(), event.getCommand());
    }

    private boolean isCommandMissing(CommandSender sender, String message)
    {
        message = message.trim();
        if (message.isEmpty())
        {
            return false;
        }
        String label = explode(" ", message)[0].toLowerCase(Locale.ENGLISH);
        if (this.injector.getCommand(label) == null)
        {
            final Locale language = BukkitUtils.getLocaleFromSender(sender);
            List<String> matches = new LinkedList<>(Match.string().getBestMatches(label, injector.getKnownCommands().keySet(), 1));
            if (matches.size() > 0 && matches.size() <= this.core.getConfiguration().commands.maxCorrectionOffers)
            {
                if (matches.size() == 1)
                {
                    sender.sendMessage(this.core.getI18n().translate(language, MessageType.NEGATIVE, "Couldn't find {input#command}. Did you mean {input#command}?", label, matches.iterator().next()));
                }
                else
                {
                    Collections.sort(matches, String.CASE_INSENSITIVE_ORDER);
                    sender.sendMessage(this.core.getI18n().translate(language, MessageType.NEUTRAL, "Did you mean one of these: {input#command}?", implode(", /", matches)));
                }
            }
            else
            {
                sender.sendMessage(this.core.getI18n().translate(language, MessageType.NEGATIVE, "I couldn't find any command for {input#command} ...", label));
            }
            return true;
        }
        return false;
    }
}
