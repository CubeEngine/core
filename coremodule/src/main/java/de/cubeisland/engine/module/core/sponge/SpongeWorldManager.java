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
package de.cubeisland.engine.module.core.sponge;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import com.google.common.base.Optional;
import de.cubeisland.engine.converter.ConverterManager;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.module.core.CubeEngine;
import de.cubeisland.engine.module.core.filesystem.FileUtil;
import de.cubeisland.engine.module.core.util.converter.LocationConverter;
import de.cubeisland.engine.module.core.world.AbstractWorldManager;
import de.cubeisland.engine.module.core.world.ConfigWorld;
import de.cubeisland.engine.module.core.world.ConfigWorldConverter;
import de.cubeisland.engine.module.core.world.WorldEntity;
import de.cubeisland.engine.module.core.world.WorldManager;
import de.cubeisland.engine.reflect.Reflector;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.spongepowered.api.Server;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import static de.cubeisland.engine.module.core.contract.Contract.expect;
import static de.cubeisland.engine.module.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.module.core.world.TableWorld.TABLE_WORLD;

@ServiceImpl(WorldManager.class)
@Version(1)
public class SpongeWorldManager extends AbstractWorldManager
{
    private final CoreModule core;
    private final Server server;

    @Inject
    public SpongeWorldManager(final CoreModule core, Reflector reflector)
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

        ConverterManager convManager = reflector.getDefaultConverterManager();
        convManager.registerConverter(new ConfigWorldConverter(this), ConfigWorld.class);
        convManager.registerConverter(new LocationConverter(this), Location.class);
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
    public boolean unloadWorld(World world)
    {
        expect(CubeEngine.isMainThread() , "Must be executed from main thread!");
        return this.server.unloadWorld(world);
    }

    @Override
    public boolean deleteWorld(World world) throws IOException
    {
        if (world == null)
        {
            return false;
        }
        if (!this.unloadWorld(world))
        {
            return false;
        }
        FileUtil.deleteRecursive(world.getWorldFolder().toPath()); // TODO delay this until world is no longer loaded
        return true;
    }

    @Override
    public Set<World> getWorlds()
    {
        expect(CubeEngine.isMainThread() , "Must be executed from main thread!");

        return new HashSet<>(this.server.getWorlds());
    }
}
