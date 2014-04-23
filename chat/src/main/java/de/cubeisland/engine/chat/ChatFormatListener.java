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
package de.cubeisland.engine.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.MacroProcessor;


public class ChatFormatListener implements Listener
{
    private static final String DEFAULT_FORMAT = new AsyncPlayerChatEvent(true, null, null, null).getFormat();
    protected final Chat module;

    public ChatFormatListener(Chat module)
    {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        // don't overwrite other plugins
        if (!DEFAULT_FORMAT.equals(event.getFormat()))
        {
            return;
        }
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
        if (module.getConfig().allowColors)
        {
            if (module.perms().COLOR.isAuthorized(user))
            {
                event.setMessage(ChatFormat.parseFormats(event.getMessage()));
            }
            else
            {
                String message = this.stripFormat(event.getMessage(), user);
                if (message.trim().isEmpty())
                {
                    event.setCancelled(true);
                    return;
                }
                event.setMessage(message);
            }
        }
        MacroProcessor processor = new MacroProcessor();
        Map<String, String> args = new HashMap<>();

        args.put("NAME", user.getName());
        // set the placeholder instead of the actual value to allow other plugins to change the value
        args.put("DISPLAY_NAME", "%1$s");
        args.put("WORLD", user.getWorld().getName());
        // set the placeholder instead of the actual value to allow other plugins to change the value
        args.put("MESSAGE", "%2$s");

        String format = this.getFormat(user);
        if (module.getConfig().allowColors)
        {
            format = ChatFormat.parseFormats(format);
        }

        ChatFormatEvent formatEvent = new ChatFormatEvent(user, event.getMessage(), format, event.isAsynchronous());

        this.module.getCore().getEventManager().fireEvent(formatEvent);

        args.putAll(formatEvent.variables);

        event.setFormat(processor.process(format, args));
    }

    protected String getFormat(User user)
    {
        return this.module.getConfig().format;
    }

    private String stripFormat(String message, Player player)
    {
        String toStrip = "";
        if( !module.perms().COLOR_BLACK.isAuthorized(player))
        {
            toStrip += "0";
        }
        if( !module.perms().COLOR_DARK_BLUE.isAuthorized(player))
        {
            toStrip += "1";
        }
        if( !module.perms().COLOR_DARK_GREEN.isAuthorized(player))
        {
            toStrip += "2";
        }
        if( !module.perms().COLOR_DARK_AQUA.isAuthorized(player))
        {
            toStrip += "3";
        }
        if( !module.perms().COLOR_DARK_RED.isAuthorized(player))
        {
            toStrip += "4";
        }
        if( !module.perms().COLOR_DARK_PURPLE.isAuthorized(player))
        {
            toStrip += "5";
        }
        if( !module.perms().COLOR_GOLD.isAuthorized(player))
        {
            toStrip += "6";
        }
        if( !module.perms().COLOR_GRAY.isAuthorized(player))
        {
            toStrip += "7";
        }
        if( !module.perms().COLOR_DARK_GRAY.isAuthorized(player))
        {
            toStrip += "8";
        }
        if( !module.perms().COLOR_BLUE.isAuthorized(player))
        {
            toStrip += "9";
        }
        if( !module.perms().COLOR_GREEN.isAuthorized(player))
        {
            toStrip += "aA";
        }
        if( !module.perms().COLOR_AQUA.isAuthorized(player))
        {
            toStrip += "bB";
        }
        if( !module.perms().COLOR_RED.isAuthorized(player))
        {
            toStrip += "cC";
        }
        if( !module.perms().COLOR_LIGHT_PURPLE.isAuthorized(player))
        {
            toStrip += "dD";
        }
        if( !module.perms().COLOR_YELLOW.isAuthorized(player))
        {
            toStrip += "eE";
        }
        if( !module.perms().COLOR_WHITE.isAuthorized(player))
        {
            toStrip += "fF";
        }
        if( !module.perms().COLOR_OBFUSCATED.isAuthorized(player))
        {
            toStrip += "kK";
        }
        if( !module.perms().COLOR_BOLD.isAuthorized(player))
        {
            toStrip += "lL";
        }
        if( !module.perms().COLOR_STRIKE.isAuthorized(player))
        {
            toStrip += "mM";
        }
        if( !module.perms().COLOR_UNDERLINE.isAuthorized(player))
        {
            toStrip += "nN";
        }
        if( !module.perms().COLOR_ITALIC.isAuthorized(player))
        {
            toStrip += "oO";
        }
        if( !module.perms().COLOR_RESET.isAuthorized(player))
        {
            toStrip += "rR";
        }
        if (toStrip.isEmpty()) return message;
        Pattern stripFormats = Pattern.compile("&[" + toStrip + "]");
        return stripFormats.matcher(message).replaceAll("");
    }
}
