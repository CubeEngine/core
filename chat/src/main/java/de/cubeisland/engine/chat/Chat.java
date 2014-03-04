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

import org.bukkit.event.Listener;

import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.module.Module;

public class Chat extends Module implements Listener
{

    private ChatConfig config;
    private ChatPerm perms;

    @Override
    public void onEnable()
    {
        this.config = this.loadConfig(ChatConfig.class);
        perms = new ChatPerm(this);
        this.getCore().getEventManager().registerListener(this, this);
        this.getCore().getCommandManager().registerCommands(this, new ChatCommands(this), ReflectedCommand.class);
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

    public ChatPerm perms()
    {
        return perms;
    }
}
