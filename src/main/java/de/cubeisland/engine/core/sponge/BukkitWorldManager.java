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
package de.cubeisland.engine.core.sponge;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.google.common.base.Optional;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.filesystem.FileUtil;
import de.cubeisland.engine.core.world.AbstractWorldManager;
import de.cubeisland.engine.core.world.WorldEntity;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.spongepowered.api.Server;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import static de.cubeisland.engine.core.contract.Contract.expect;
import static de.cubeisland.engine.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;

public class BukkitWorldManager extends AbstractWorldManager
{
    private final SpongeCore core;
    private final Server server;

    public BukkitWorldManager(final SpongeCore core)
    {
        super(core);
        this.core = core;
        this.server = core.getGame().getServer();

        core.addInitHook(() -> {
            DSLContext dsl = database.getDSL();
            Result<WorldEntity> worldEntities = dsl.selectFrom(TABLE_WORLD).fetch();
            Collection<World> loadedWorlds = server.getWorlds();
            for (WorldEntity entity : worldEntities)
            {
                World world = server.getWorld(entity.getWorldUUID()).get();
                if (loadedWorlds.contains(world))
                {
                    loadedWorlds.remove(world);
                    worlds.put(world.getName(), entity);
                    worldIds.put(entity.getValue(TABLE_WORLD.KEY), world);
                    worldUUIDs.add(world.getUniqueId());
                }
            }
            if (!loadedWorlds.isEmpty()) // new worlds?
            {
                for (World world : loadedWorlds)
                {
                    WorldEntity entity = dsl.newRecord(TABLE_WORLD).newWorld(world);
                    entity.insertAsync();
                    worlds.put(world.getName(), entity);
                    worldIds.put(entity.getValue(TABLE_WORLD.KEY), world);
                    worldUUIDs.add(world.getUniqueId());
                }
            }
        });
    }

    @Override
    public World createWorld(WorldProperties properties)
    {
        expect(CubeEngine.isMainThread() , "Must be executed from main thread!");
        return this.server.loadWorld(properties).get();
    }

    @Override
    public Optional<World> getWorld(String name)
    {
        expect(CubeEngine.isMainThread() , "Must be executed from main thread!");
        expectNotNull(name, "The world name must not be null!");

        return this.server.getWorld(name);
    }

    @Override
    public Optional<World> getWorld(UUID uid)
    {
        expect(CubeEngine.isMainThread() , "Must be executed from main thread!");
        expectNotNull(uid, "The world UUID must not be null!");

        return this.server.getWorld(uid);
    }

    @Override
    public boolean unloadWorld(World world, boolean save)
    {
        expect(CubeEngine.isMainThread() , "Must be executed from main thread!");
        if (!save)
        {
            this.core.getLog().warn(new IllegalArgumentException(), "This is unstable on CraftBukkit servers");
        }
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
        // This can fail with a FileSystemException due to the file somehow still being in use
        FileUtil.deleteRecursive(world.getWorldFolder().toPath());
        return true;
    }

    @Override
    public Set<World> getWorlds()
    {
        expect(CubeEngine.isMainThread() , "Must be executed from main thread!");

        return new HashSet<>(this.server.getWorlds());
    }
}
