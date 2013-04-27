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
package de.cubeisland.cubeengine.roles.config;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.roles.Roles;

import gnu.trove.map.hash.TLongObjectHashMap;

public class RoleMirror
{
    public final String mainWorld;
    private TLongObjectHashMap<Triplet<Boolean, Boolean, Boolean>> worlds =
        new TLongObjectHashMap<Triplet<Boolean, Boolean, Boolean>>(); //mirror roles / assigned / users
    private Roles module;

    public RoleMirror(Roles module, String mainWorld)
    {
        this.module = module;
        this.mainWorld = mainWorld;
        Long worldId = CubeEngine.getCore().getWorldManager().getWorldId(mainWorld);
        if (worldId == null)
        {
            module.getLog().log(LogLevel.WARNING, "Unknown world " + mainWorld);
        }
        else
        {
            this.worlds.put(worldId, new Triplet<Boolean, Boolean, Boolean>(true, true, true));
        }
    }

    /**
     * Single-world-mirror
     *
     * @param worldId
     */
    public RoleMirror(Roles module, long worldId)
    {
        this.module = module;
        this.worlds.put(worldId, new Triplet<Boolean, Boolean, Boolean>(true, true, true));
        this.mainWorld = CubeEngine.getCore().getWorldManager().getWorld(worldId).getName();
    }

    /**
     * Returns a map of the mirrored worlds.
     * The mirrors are: roles | assigned roles | assigned permissions and metadata
     *
     * @return
     */
    public TLongObjectHashMap<Triplet<Boolean, Boolean, Boolean>> getWorldMirrors()
    {
        return this.worlds;
    }

    public void setWorld(String worldName, boolean roles, boolean assigned, boolean users)
    {
        Long world = CubeEngine.getCore().getWorldManager().getWorldId(worldName);
        if (world == null)
        {
            module.getLog().log(LogLevel.WARNING, "Unknown world " + worldName + "! Removing from config...");
            return;
        }
        this.worlds.put(world, new Triplet<Boolean, Boolean, Boolean>(roles, assigned, users));
    }
}
