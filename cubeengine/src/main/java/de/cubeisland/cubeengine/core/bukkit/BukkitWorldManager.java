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
package de.cubeisland.cubeengine.core.bukkit;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.server.v1_5_R3.RegionFileCache;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.world.AbstractWorldManager;
import de.cubeisland.cubeengine.core.world.WorldModel;

import gnu.trove.set.hash.THashSet;

public class BukkitWorldManager extends AbstractWorldManager
{
    private final Server server;

    public BukkitWorldManager(BukkitCore core)
    {
        super(core);
        this.server = core.getServer();
    }

    void loadWorlds()
    {
        Collection<WorldModel> models = this.storage.getAll();
        List<World> loadedWorlds = this.server.getWorlds();
        for (WorldModel model : models)
        {
            World world = this.server.getWorld(UUID.fromString(model.worldUUID));
            if (loadedWorlds.contains(world))
            {
                loadedWorlds.remove(world);
                this.worlds.put(world.getName(), model);
                this.worldIds.put(model.key, world);
            }
        }
        if (!loadedWorlds.isEmpty()) // new worlds?
        {
            for (World world : loadedWorlds)
            {
                WorldModel model = new WorldModel(world);
                this.storage.store(model);
                this.worlds.put(world.getName(), model);
                this.worldIds.put(model.key, world);
            }
        }
    }

    public World createWorld(WorldCreator creator)
    {
        assert CubeEngine.isMainThread() : "Must be executed from main thread!";

        return this.server.createWorld(creator);
    }

    @Override
    public World getWorld(String name)
    {
        assert CubeEngine.isMainThread() : "Must be executed from main thread!";

        return this.server.getWorld(name);
    }

    @Override
    public World getWorld(UUID uid)
    {
        assert CubeEngine.isMainThread() : "Must be executed from main thread!";

        return this.server.getWorld(uid);
    }

    @Override
    public boolean unloadWorld(World world, boolean save)
    {
        assert CubeEngine.isMainThread() : "Must be executed from main thread!";
        boolean success = this.server.unloadWorld(world, save);
        if (success && !save)
        {
            RegionFileCache.a();
        }
        return success;
    }

    @Override
    public boolean deleteWorld(World world) throws IOException
    {
        if (world == null)
        {
            return false;
        }
        if (!this.unloadWorld(world, false))
        {
            return false;
        }
        FileManager.deleteRecursive(world.getWorldFolder());
        return true;
    }

    @Override
    public Set<World> getWorlds()
    {
        assert CubeEngine.isMainThread() : "Must be executed from main thread!";

        return new THashSet<World>(this.server.getWorlds());
    }
}
