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

import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.MacroProcessor;

import static de.cubeisland.engine.chat.ChatPerm.*;

public class Chat extends Module implements Listener
{

    private ChatConfig config;
    private String defaultFormat;

    @Override
    public void onEnable()
    {
        this.config = this.loadConfig(ChatConfig.class);
        new ChatPerm(this);
        this.getCore().getEventManager().registerListener(this, this);
        this.getCore().getCommandManager().registerCommands(this, new ChatCommands(this), ReflectedCommand.class);
        this.defaultFormat = this.config.format;
        if (this.config.allowColors)
        {
            this.defaultFormat = ChatFormat.parseFormats(this.defaultFormat);
        }
        if (this.getCore().getModuleManager().getModule("roles") != null)
        {
            this.getCore().getEventManager().registerListener(this, new RoleChatFormatListener(this));
        }
        else
        {
            this.getCore().getEventManager().registerListener(this, new ChatFormatListener(this));
            this.getLog().info("No Roles-Module found!");
        }
    }

    protected ChatConfig getConfig()
    {
        return config;
    }
}
