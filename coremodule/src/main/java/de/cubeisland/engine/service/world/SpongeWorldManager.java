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
package de.cubeisland.engine.service.world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import com.google.common.base.Optional;
import de.cubeisland.engine.converter.ConverterManager;
import de.cubeisland.engine.modularity.core.marker.Disable;
import de.cubeisland.engine.modularity.core.marker.Enable;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.module.core.sponge.EventManager;
import de.cubeisland.engine.service.database.Database;
import de.cubeisland.engine.module.core.util.converter.LocationConverter;
import de.cubeisland.engine.reflect.Reflector;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.world.WorldLoadEvent;
import org.spongepowered.api.event.world.WorldUnloadEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import static de.cubeisland.engine.module.core.contract.Contract.expect;
import static de.cubeisland.engine.module.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.module.core.sponge.CoreModule.isMainThread;
import static de.cubeisland.engine.service.world.TableWorld.TABLE_WORLD;

@ServiceImpl(WorldManager.class)
@Version(1)
public class SpongeWorldManager extends AbstractWorldManager implements WorldManager
{
    @Inject private EventManager em;
    @Inject private WorldModule module;
    private final Server server;

    @Inject
    public SpongeWorldManager(final Game game, Database database, Reflector reflector)
    {
        super(database);

        this.server = game.getServer();

        ConverterManager convManager = reflector.getDefaultConverterManager();
        convManager.registerConverter(new ConfigWorldConverter(this), ConfigWorld.class);
        convManager.registerConverter(new LocationConverter(this), Location.class);
    }

    @Enable
    public void onEnable()
    {
        super.onEnable();
        DSLContext dsl = database.getDSL();
        Result<WorldEntity> worldEntities = dsl.selectFrom(TABLE_WORLD).fetch();
        Collection<World> loadedWorlds = server.getWorlds();
        for (WorldEntity entity : worldEntities) // on serverstart this is empty (this is for reload)
        {
            Optional<World> world = server.getWorld(entity.getWorldUUID());
            if (world.isPresent())
            {
                if (loadedWorlds.contains(world.get()))
                {
                    loadedWorlds.remove(world.get());
                    worlds.put(world.get().getName(), entity);
                    worldIds.put(entity.getValue(TABLE_WORLD.KEY), world.get());
                    worldUUIDs.add(world.get().getUniqueId());
                }
            }
            // TODO else mark world as not found in db

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

        em.registerListener(module, this);
    }

    @Disable
    public void onDisable()
    {
        em.removeListener(module, this);
    }

    @Subscribe(order = Order.FIRST)
    public void onWorldLoad(WorldLoadEvent event)
    {
        getWorldEntity(event.getWorld()); // loads from db if not yet loaded
    }

    @Subscribe(order = Order.POST)
    public void onWorldUnload(WorldUnloadEvent event)
    {
        World world = event.getWorld();
        WorldEntity entity = getWorldEntity(world);
        worlds.remove(world.getName());
        worldIds.remove(entity.getValue(TABLE_WORLD.KEY));
        worldUUIDs.remove(world.getUniqueId());
    }

    @Override
    public World createWorld(WorldProperties properties)
    {
        expect(isMainThread(), "Must be executed from main thread!");
        return this.server.loadWorld(properties).get();
    }

    @Override
    public Optional<World> getWorld(String name)
    {
        expect(isMainThread() , "Must be executed from main thread!");
        expectNotNull(name, "The world name must not be null!");

        return this.server.getWorld(name);
    }

    @Override
    public Optional<World> getWorld(UUID uid)
    {
        expect(isMainThread() , "Must be executed from main thread!");
        expectNotNull(uid, "The world UUID must not be null!");

        return this.server.getWorld(uid);
    }

    @Override
    public boolean unloadWorld(World world)
    {
        expect(isMainThread() , "Must be executed from main thread!");
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
        // TODO FileUtil.deleteRecursive(world.getWorldFolder().toPath()); // TODO delay this until world is no longer loaded
        return true;
    }

    @Override
    public List<World> getWorlds()
    {
        expect(isMainThread() , "Must be executed from main thread!");

        return new ArrayList<>(this.server.getWorlds());
    }
}
