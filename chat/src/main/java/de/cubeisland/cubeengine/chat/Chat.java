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
package de.cubeisland.cubeengine.chat;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Inject;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.RolesAttachment;

public class Chat extends Module implements Listener
{
    private static final String DEFAULT_FORMAT = new AsyncPlayerChatEvent(true, null, null, null).getFormat();
    private ChatConfig config;
    @Inject private Roles roles;
    private String format;

    @Override
    public void onEnable()
    {
        this.config = Configuration.load(ChatConfig.class, this);
        new ChatPerm(this);
        this.getCore().getEventManager().registerListener(this, this);
        this.format = this.config.format;
        if (this.config.parseColors)
        {
            this.format = ChatFormat.parseFormats(this.format);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPlayerChat(AsyncPlayerChatEvent event)
    {
        // don't overwrite other plugins
        if (!DEFAULT_FORMAT.equals(event.getFormat()))
        {
            return;
        }

        Player player = event.getPlayer();
        String format = this.format;

        if (config.parseColors && ChatPerm.COLOR.isAuthorized(player))
        {
            event.setMessage(ChatFormat.parseFormats(event.getMessage()));
        }

        ChatFormatEvent formatEvent = new ChatFormatEvent(player, event.getMessage(), format, event.isAsynchronous());

        format = format.replace("{NAME}", player.getName());
        // set the placeholder instead of the actual value to allow other plugins to change the value
        format = format.replace("{DISPLAY_NAME}", "%1$s");
        format = format.replace("{WORLD}", player.getWorld().getName());
        // set the placeholder instead of the actual value to allow other plugins to change the value
        format = format.replace("{MESSAGE}", "%2$s");

        if (roles != null)
        {
            User user = this.getCore().getUserManager().getExactUser(player.getName());
            RolesAttachment rolesAttachment = user.get(RolesAttachment.class);
            if (rolesAttachment == null)
            {
                this.roles.getLog().warn("Missing RolesAttachment!");
                return;
            }
            if (format.contains("{ROLE.PREFIX}"))
            {
                String prefix = rolesAttachment.getCurrentMetadata("prefix");
                format = format.replace("{ROLE.PREFIX}", prefix == null ? "" : ChatFormat.parseFormats(prefix));
            }
            if (format.contains("{ROLE.SUFFIX}"))
            {
                String suffix = rolesAttachment.getCurrentMetadata("suffix");
                format = format.replace("{ROLE.SUFFIX}", suffix == null ? "" : ChatFormat.parseFormats(suffix));
            }
        }

        this.getCore().getEventManager().fireEvent(formatEvent);

        for (Map.Entry<String, String> entry : formatEvent.variables.entrySet())
        {
            format = format.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        event.setFormat(format);
    }
}
