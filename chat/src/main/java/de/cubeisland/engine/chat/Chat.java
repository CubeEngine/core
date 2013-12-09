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

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.MacroProcessor;
import de.cubeisland.engine.roles.role.RolesAttachment;

import static de.cubeisland.engine.chat.ChatPerm.*;
import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;

public class Chat extends Module implements Listener
{
    private static final String DEFAULT_FORMAT = new AsyncPlayerChatEvent(true, null, null, null).getFormat();
    private ChatConfig config;
    private String format;

    private Module rolesModule;

    @Override
    public void onEnable()
    {
        this.config = this.loadConfig(ChatConfig.class);
        new ChatPerm(this);
        this.getCore().getEventManager().registerListener(this, this);
        this.getCore().getCommandManager().registerCommands(this, this, ReflectedCommand.class);
        this.format = this.config.format;
        if (this.config.allowColors)
        {
            this.format = ChatFormat.parseFormats(this.format);
        }
        if (this.getCore().getModuleManager().getModule("roles") != null)
        {
            this.getCore().getEventManager().registerListener(this, new RoleChatFormatListener(this));
        }
        else
        {
            this.getLog().info("No Roles-Module found!");
        }
    }

    @Command(desc = "Allows you to emote", min = 1, max = NO_MAX, usage = "<message>")
    public void me(CommandContext context)
    {
        String message = context.getStrings(0);
        this.getCore().getUserManager().broadcastStatus(message, context.getSender());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPlayerChat(AsyncPlayerChatEvent event)
    {
        // don't overwrite other plugins
        if (!DEFAULT_FORMAT.equals(event.getFormat()))
        {
            return;
        }

        User user = this.getCore().getUserManager().getExactUser(event.getPlayer().getName());

        if (config.allowColors)
        {
            if (ChatPerm.COLOR.isAuthorized(user))
            {
                event.setMessage(ChatFormat.parseFormats(event.getMessage()));
            }
            else
            {
                event.setMessage(this.stripFormat(event.getMessage(), user));
            }
        }

        ChatFormatEvent formatEvent = new ChatFormatEvent(user, event.getMessage(), format, event.isAsynchronous());

        MacroProcessor processor = new MacroProcessor();

        Map<String, String> args = new HashMap<>();

        args.put("NAME", user.getName());
        // set the placeholder instead of the actual value to allow other plugins to change the value
        args.put("DISPLAY_NAME", "%1$s");
        args.put("WORLD", user.getWorld().getName());
        // set the placeholder instead of the actual value to allow other plugins to change the value
        args.put("MESSAGE", "%2$s");

        this.getCore().getEventManager().fireEvent(formatEvent);

        args.putAll(formatEvent.variables);

        event.setFormat(processor.process(this.format, args));
    }

    private String stripFormat(String message, Player player)
    {
        String toStrip = "";
        if( !COLOR_BLACK.isAuthorized(player))
        {
            toStrip += "0";
        }
        if( !COLOR_DARK_BLUE.isAuthorized(player))
        {
            toStrip += "1";
        }
        if( !COLOR_DARK_GREEN.isAuthorized(player))
        {
            toStrip += "2";
        }
        if( !COLOR_DARK_AQUA.isAuthorized(player))
        {
            toStrip += "3";
        }
        if( !COLOR_DARK_RED.isAuthorized(player))
        {
            toStrip += "4";
        }
        if( !COLOR_DARK_PURPLE.isAuthorized(player))
        {
            toStrip += "5";
        }
        if( !COLOR_GOLD.isAuthorized(player))
        {
            toStrip += "6";
        }
        if( !COLOR_GRAY.isAuthorized(player))
        {
            toStrip += "7";
        }
        if( !COLOR_DARK_GRAY.isAuthorized(player))
        {
            toStrip += "8";
        }
        if( !COLOR_BLUE.isAuthorized(player))
        {
            toStrip += "9";
        }
        if( !COLOR_GREEN.isAuthorized(player))
        {
            toStrip += "aA";
        }
        if( !COLOR_AQUA.isAuthorized(player))
        {
            toStrip += "bB";
        }
        if( !COLOR_RED.isAuthorized(player))
        {
            toStrip += "cC";
        }
        if( !COLOR_LIGHT_PURPLE.isAuthorized(player))
        {
            toStrip += "dD";
        }
        if( !COLOR_YELLOW.isAuthorized(player))
        {
            toStrip += "eE";
        }
        if( !COLOR_WHITE.isAuthorized(player))
        {
            toStrip += "fF";
        }
        if( !COLOR_OBFUSCATED.isAuthorized(player))
        {
            toStrip += "kK";
        }
        if( !COLOR_BOLD.isAuthorized(player))
        {
            toStrip += "lL";
        }
        if( !COLOR_STRIKE.isAuthorized(player))
        {
            toStrip += "mM";
        }
        if( !COLOR_UNDERLINE.isAuthorized(player))
        {
            toStrip += "nN";
        }
        if( !COLOR_ITALIC.isAuthorized(player))
        {
            toStrip += "oO";
        }
        if( !COLOR_RESET.isAuthorized(player))
        {
            toStrip += "rR";
        }
        if (toStrip.isEmpty()) return message;
        Pattern stripFormats = Pattern.compile("&[" + toStrip + "]");
        return stripFormats.matcher(message).replaceAll("");
    }
}
