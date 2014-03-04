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
package de.cubeisland.engine.fun;

import de.cubeisland.engine.core.command.CommandManager;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.fun.commands.DiscoCommand;
import de.cubeisland.engine.fun.commands.InvasionCommand;
import de.cubeisland.engine.fun.commands.NukeCommand;
import de.cubeisland.engine.fun.commands.PlayerCommands;
import de.cubeisland.engine.fun.commands.RocketCommand;
import de.cubeisland.engine.fun.commands.ThrowCommands;

public class Fun extends Module
{
    private FunConfiguration config;
    private FunPerm perms;

    @Override
    public void onEnable()
    {
        this.config = this.loadConfig(FunConfiguration.class);
        // this.getCore().getFileManager().dropResources(FunResource.values());
        perms = new FunPerm(this);

        final CommandManager cm = this.getCore().getCommandManager();
        cm.registerCommands(this, new ThrowCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new NukeCommand(this), ReflectedCommand.class);
        cm.registerCommands(this, new PlayerCommands(this), ReflectedCommand.class);
        cm.registerCommands(this, new DiscoCommand(this), ReflectedCommand.class);
        cm.registerCommands(this, new InvasionCommand(this), ReflectedCommand.class);
        cm.registerCommands(this, new RocketCommand(this), ReflectedCommand.class);
    }

    public FunConfiguration getConfig()
    {
        return this.config;
    }

    public FunPerm perms()
    {
        return perms;
    }
}
