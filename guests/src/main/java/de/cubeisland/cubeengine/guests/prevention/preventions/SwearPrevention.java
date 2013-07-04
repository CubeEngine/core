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
package de.cubeisland.cubeengine.guests.prevention.preventions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;

import gnu.trove.set.hash.THashSet;

/**
 * Prevents the user from swearing.
 */
public class SwearPrevention extends Prevention
{
    private static final String REGEX_PREFIX = "regex:";
    private Set<Pattern> swearPatterns;

    public SwearPrevention(Guests guests)
    {
        super("swear", guests);
        setEnableByDefault(true);
        setEnablePunishing(true);
    }

    @Override
    public String getConfigHeader()
    {
        return super.getConfigHeader() + "\n"
            + "Every message a guest sends will be checked for the words listed under words.\n"
            + "More words will result in more time to check the message. Even though the words\n"
            + "get compiled on startup, an extreme list may lag the chat for guests.\n"
            + "The words may contain usual filesystem patterns.\n"
            + "Words prefixed with 'regex:' are interpreted as a Java regular expression\n"
            + "\nFilesystem patterns:\n"
            + " * -> any number (including none) of any character\n"
            + " ? -> one or none of any character\n"
            + " { , , } -> a group of strings of which one must match\n"
            + " \\ -> escape character to write the above character as a normal character";
    }

    @Override
    public Configuration getDefaultConfig()
    {
        Configuration config = super.getDefaultConfig();

        config.set("words", new String[]
            {
                "hitler",
                "nazi",
                "asshole",
                "shit",
                "fuck"
            });

        return config;
    }

    @Override
    public void enable()
    {
        super.enable();
        this.swearPatterns = new THashSet<Pattern>();
        for (String word : getConfig().getStringList("words"))
        {
            this.swearPatterns.add(this.compile(word));
        }
    }

    @Override
    public void disable()
    {
        super.disable();
        this.swearPatterns.clear();
        this.swearPatterns = null;
    }

    private Pattern compile(String string)
    {
        if (string.startsWith(REGEX_PREFIX))
        {
            return Pattern.compile(string.substring(REGEX_PREFIX.length()));
        }
        else
        {
            char current;
            boolean ignoreNext = false;
            boolean inGroup = false;
            StringBuilder pattern = new StringBuilder();
            StringBuilder plain = null;
            String replacement = null;
            for (int i = 0; i < string.length(); ++i)
            {
                current = string.charAt(i);
                ignore: if (!ignoreNext)
                {
                    if (current == '\\')
                    {
                        ignoreNext = true;
                    }
                    else if (current == '*')
                    {
                        replacement = ".*?";
                    }
                    else if (current == '?')
                    {
                        replacement = ".?";
                    }
                    else if (current == '{')
                    {
                        if (string.indexOf('}') <= i)
                        {
                            ignoreNext = true;
                            break ignore;
                        }
                        inGroup = true;
                        replacement = "(";
                    }
                    else if (inGroup && current == ',')
                    {
                        replacement = "|";
                    }
                    else if (inGroup && current == '}')
                    {
                        inGroup = false;
                        replacement = ")";
                    }
                    else
                    {
                        ignoreNext = true;
                        break ignore;
                    }

                    pattern.append(Pattern.quote(plain.toString()));
                    plain = null;

                    if (replacement != null)
                    {
                        pattern.append(replacement);
                        replacement = null;
                    }

                    continue;
                }
                if (ignoreNext)
                {
                    ignoreNext = false;
                }
                if (plain == null)
                {
                    plain = new StringBuilder();
                }
                plain.append(current);
            }

            if (plain != null)
            {
                pattern.append(Pattern.quote(plain.toString()));
            }

            return Pattern.compile("\\b" + pattern.append("\\b").toString(), Pattern.CASE_INSENSITIVE);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void chat(AsyncPlayerChatEvent event)
    {
        final Player player = event.getPlayer();
        if (!can(player))
        {
            final String message = event.getMessage();
            synchronized (this)
            {
                for (Pattern badword : this.swearPatterns)
                {
                    if (badword.matcher(message).find())
                    {
                        sendMessage(player);
                        punish(player);
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @Command(desc = "", usage = "[word]")
    public void badword(CommandContext context)
    {
        if (context.hasArg(0))
        {
            String word = context.getString(0);
            if (word != null)
            {
                Configuration config = getConfig();
                List<String> words = new ArrayList<String>(config.getStringList("words"));
                words.add(word);
                config.set("words", words);
                saveConfig();

                synchronized (this)
                {
                    this.swearPatterns.add(this.compile(word));
                }

                context.sendTranslated("wordAdded");
            }
            else
            {
                context.sendTranslated("noWord");
            }
        }
    }
}
