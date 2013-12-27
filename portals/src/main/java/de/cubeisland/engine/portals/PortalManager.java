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
package de.cubeisland.engine.portals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import de.cubeisland.engine.core.util.LocationUtil;
import de.cubeisland.engine.portals.config.PortalConfig;

public class PortalManager implements Listener
{
    private final Portals module;
    private final File portalsDir;

    private Map<String, Portal> configs = new HashMap<>();

    private Map<Long, List<Portal>> chunksWithPortals = new HashMap<>();

    public PortalManager(Portals module)
    {
        this.module = module;
        this.portalsDir = this.module.getFolder().resolve("portals").toFile();
        this.portalsDir.mkdir();
    }

    private void loadPortals()
    {
        for (File file : this.portalsDir.listFiles())
        {
            if (!file.isDirectory() && file.getName().endsWith(".yml"))
            {
                PortalConfig load = this.module.getCore().getConfigFactory().load(PortalConfig.class, file);
                Portal portal = new Portal(file.getName().substring(0, file.getName().lastIndexOf(".yml")), load);
                this.configs.put(portal.getName(), portal);

                int chunkXFrom = load.location.from.x >> 4;
                int chunkZFrom =  load.location.from.z >> 4;
                int chunkXTo =  load.location.to.x >> 4;
                int chunkZTo = load.location.to.z >> 4;
                if (chunkXFrom > chunkXTo) // if from is greater swap
                {
                    chunkXFrom = chunkXFrom + chunkXTo;
                    chunkXTo = chunkXFrom - chunkXTo;
                    chunkXFrom = chunkXFrom - chunkXTo;
                }
                if (chunkZFrom > chunkZTo) // if from is greater swap
                {
                    chunkZFrom = chunkZFrom + chunkZTo;
                    chunkZTo = chunkZFrom - chunkZTo;
                    chunkZFrom = chunkZFrom - chunkZTo;
                }
                for (int x = chunkXFrom; x <= chunkXTo; x++)
                {
                    for (int z = chunkZFrom; z <= chunkZTo; z++)
                    {
                        long chunkKey = LocationUtil.getChunkKey(x, z);
                        List<Portal> list = this.chunksWithPortals.get(chunkKey);
                        if (list == null)
                        {
                            list = new ArrayList<>();
                            this.chunksWithPortals.put(chunkKey, list);
                        }
                        list.add(portal);
                    }
                }
            }
        }
        this.module.getLog().info("{} portals loaded!", this.configs.size());
        this.module.getLog().debug("{} chunks checked", this.chunksWithPortals.size());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event)
    {
        if (event.getFrom().getWorld() != event.getTo().getWorld())
        {
            return;
        }
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
         || event.getFrom().getBlockY() != event.getTo().getBlockY()
         || event.getFrom().getBlockZ() != event.getTo().getBlockZ())
        {
            List<Portal> portals = this.chunksWithPortals.get(LocationUtil.getChunkKey(event.getFrom()));
            if (portals != null)
            {
                for (Portal portal : portals)
                {
                    if (portal.has(event.getTo()))
                    {
                        event.getPlayer().sendMessage("You stand in a portal: " + portal.getName());
                    }
                    // else ignore
                }
            }
            // else movement is not in a chunk that has a portal
        }
    }
}
