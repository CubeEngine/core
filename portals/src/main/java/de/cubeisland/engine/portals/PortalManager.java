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
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.LocationUtil;
import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.portals.config.PortalConfig;

public class PortalManager implements Listener
{
    public final Portals module;
    protected final File portalsDir;

    private final Map<String, Portal> portals = new HashMap<>();
    private final Map<Long, List<Portal>> chunksWithPortals = new HashMap<>();

    public PortalManager(Portals module)
    {
        this.module = module;
        this.portalsDir = this.module.getFolder().resolve("portals").toFile();
        this.portalsDir.mkdir();
        this.module.getCore().getCommandManager().registerCommand(new PortalCommands(this.module, this));
        this.module.getCore().getCommandManager().registerCommand(new PortalModifyCommand(this.module, this), "portals");
        this.module.getCore().getEventManager().registerListener(this.module, this);
        this.loadPortals();
        this.module.getCore().getTaskManager().runTimer(module, new Runnable()
        {
            @Override
            public void run()
            {
                checkForEntitiesInPortals();
            }
        }, 5, 5);
    }

    private void loadPortals()
    {
        for (File file : this.portalsDir.listFiles())
        {
            if (!file.isDirectory() && file.getName().endsWith(".yml"))
            {
                PortalConfig load = this.module.getCore().getConfigFactory().load(PortalConfig.class, file);
                Portal portal = new Portal(module, this, file.getName().substring(0, file.getName().lastIndexOf(".yml")), load);
                this.addPortal(portal);
            }
        }
        this.module.getLog().info("{} portals loaded!", this.portals.size());
        this.module.getLog().debug("in {} chunks", this.chunksWithPortals.size());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event)
    {
        List<Portal> portals = this.chunksWithPortals.get(LocationUtil.getChunkKey(event.getTo()));
        if (portals == null)
        {
            return;
        }
        for (Portal portal : portals)
        {
            if (portal.has(event.getTo()))
            {
                User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
                PortalsAttachment attachment = user.attachOrGet(PortalsAttachment.class, module);
                attachment.setInPortal(true);
                if (attachment.isDebug())
                {
                    user.sendTranslated("&e[Portals] Debug: &aTeleported into portal: &6%s", portal.getName());
                }
                return;
            }
            // else ignore
        }
    }

    final WeakHashMap<Portal, List<Entity>> entitesInPortals = new WeakHashMap<>();

    private void checkForEntitiesInPortals()
    {
        for (Portal portal : this.portals.values())
        {
            if (portal.config.teleportNonPlayers)
            {
                for (Pair<Integer, Integer> chunk : portal.getChunks())
                {
                    if (portal.getWorld().isChunkLoaded(chunk.getLeft(), chunk.getRight()))
                    {
                        Location helperLoc = new Location(null, 0,0,0);
                        for (Entity entity : portal.getWorld().getChunkAt(chunk.getLeft(), chunk.getRight()).getEntities())
                        {
                            List<Entity> entities = entitesInPortals.get(portal);
                            if (portal.has(entity.getLocation(helperLoc)))
                            {
                                if (entities == null || entities.isEmpty() || !entities.contains(entity))
                                {
                                    portal.teleport(entity);
                                }
                            }
                            else if (entities != null)
                            {
                                entities.remove(entity);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event)
    {
        List<Portal> portals = this.chunksWithPortals.get(LocationUtil.getChunkKey(event.getFrom()));
        if (portals == null)
        {
            return;
        }
        for (Portal portal : portals)
        {
            List<Entity> entities = this.entitesInPortals.get(portal);
            if (portal.has(event.getTo()))
            {
                if (entities == null)
                {
                    entities = new ArrayList<>();
                    this.entitesInPortals.put(portal, entities);
                }
                entities.add(event.getEntity());
                return;
            }
            else if (entities != null)
            {
                entities.remove(event.getEntity());
            }
        }
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
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            PortalsAttachment attachment = user.attachOrGet(PortalsAttachment.class, module);
            if (portals != null)
            {
                for (Portal portal : portals)
                {
                    if (portal.has(event.getTo()))
                    {
                        if (attachment.isDebug())
                        {
                            if (attachment.isInPortal())
                            {
                                user.sendTranslated("&e[Portals] Debug: &aMove in portal: &6%s", portal.getName());
                            }
                            else
                            {
                                user.sendTranslated("&e[Portals] Debug: &aEntered portal: &6%s", portal.getName());
                                portal.showInfo(user);
                                attachment.setInPortal(true);
                            }
                        }
                        else if (!attachment.isInPortal())
                        {
                            portal.teleport(user);
                        }
                        return;
                    }
                    // else ignore
                }
            }
            attachment.setInPortal(false);
            // else movement is not in a chunk that has a portal
        }
    }

    public Portal getPortal(String name)
    {
        return this.portals.get(name.toLowerCase());
    }

    protected void addPortal(Portal portal)
    {
        this.portals.put(portal.getName().toLowerCase(), portal);

        List<Pair<Integer,Integer>> chunks = portal.getChunks();
        for (Pair<Integer, Integer> chunk : chunks)
        {
            long chunkKey = LocationUtil.getChunkKey(chunk.getLeft(), chunk.getRight());
            List<Portal> list = this.chunksWithPortals.get(chunkKey);
            if (list == null)
            {
                list = new ArrayList<>();
                this.chunksWithPortals.put(chunkKey, list);
            }
            list.add(portal);
        }
    }

    protected void removePortal(Portal portal)
    {
        this.portals.remove(portal.getName().toLowerCase());
        for (List<Portal> portalList : this.chunksWithPortals.values())
        {
            portalList.remove(portal);
        }
    }
}
