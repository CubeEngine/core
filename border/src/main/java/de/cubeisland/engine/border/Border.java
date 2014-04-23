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
package de.cubeisland.engine.border;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.world.WorldManager;

public class Border extends Module
{
    private BorderConfig globalConfig;
    private Map<Long, BorderConfig> worldConfigs;
    private WorldManager wm;
    private File folder;
    private BorderPerms perms;

    @Override
    public void onEnable()
    {
        wm = this.getCore().getWorldManager();
        this.globalConfig = this.getCore().getConfigFactory().load(BorderConfig.class, this.getFolder().resolve("globalconfig.yml").toFile());
        folder = this.getFolder().resolve("worlds").toFile();
        folder.mkdir();
        this.worldConfigs = new HashMap<>();
        for (World world : Bukkit.getWorlds())
        {
            this.loadConfig(world);
        }
        perms = new BorderPerms(this);
        this.getCore().getEventManager().registerListener(this, new BorderListener(this));
        this.getCore().getCommandManager().registerCommand(new BorderCommands(this));

    }

    private BorderConfig loadConfig(World world)
    {
        File worldFile = new File(folder, world.getName() + ".yml");
        BorderConfig worldConfig = this.globalConfig.loadChild(worldFile);
        this.worldConfigs.put(this.wm.getWorldId(world), worldConfig);
        if (!worldConfig.center.checkCenter(world))
        {
            this.getLog().warn("The world spawn of {} is not inside the border!", world.getName());
        }
        return worldConfig;
    }

    public BorderConfig getConfig(World world)
    {
        BorderConfig worldConfig = this.worldConfigs.get(this.wm.getWorldId(world));
        if (worldConfig == null)
        {
            return this.loadConfig(world);
        }
        return worldConfig;
    }

    public BorderPerms perms()
    {
        return perms;
    }
}
