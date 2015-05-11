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
package de.cubeisland.engine.module.core.world;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.google.common.base.Optional;

import de.cubeisland.engine.module.core.database.Database;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.spongepowered.api.world.World;

import static de.cubeisland.engine.module.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.module.core.world.TableWorld.TABLE_WORLD;
import static java.util.stream.Collectors.toList;

public abstract class AbstractWorldManager implements WorldManager
{
    protected final Map<String, WorldEntity> worlds = new HashMap<>();
    protected final Map<UInteger, World> worldIds = new HashMap<>();
    protected final Set<UUID> worldUUIDs = new HashSet<>();

    protected final Database database;

    public AbstractWorldManager(CoreModule core)
    {
        this.database = core.getModularity().start(Database.class);
    }

    @Override
    public synchronized UInteger getWorldId(World world)
    {
        if (world == null)
        {
            throw new IllegalArgumentException("the world given is null!");
        }
        return this.getWorldEntity(world).getValue(TABLE_WORLD.KEY);
    }

    @Override
    public WorldEntity getWorldEntity(World world)
    {
        DSLContext dsl = this.database.getDSL();
        WorldEntity worldEntity = this.worlds.get(world.getName());
        if (worldEntity == null)
        {
            UUID uid = world.getUniqueId();
            worldEntity = dsl.selectFrom(TABLE_WORLD).where(TABLE_WORLD.LEAST.eq(uid.getLeastSignificantBits()),
                                                            TABLE_WORLD.MOST.eq(uid.getMostSignificantBits())).fetchOne();
            if (worldEntity == null)
            {
                worldEntity = dsl.newRecord(TABLE_WORLD).newWorld(world);
                worldEntity.insertAsync();
            }
            this.worlds.put(world.getName(), worldEntity);
            this.worldIds.put(worldEntity.getValue(TABLE_WORLD.KEY), world);
            this.worldUUIDs.add(world.getUniqueId());
        }
        return worldEntity;
    }

    @Override
    public synchronized UInteger getWorldId(String name)
    {
        WorldEntity entity = this.worlds.get(name);
        if (entity == null)
        {
            Optional<World> world = this.getWorld(name);
            if (!world.isPresent()) return null;
            return this.getWorldId(world.get());
        }
        return entity.getValue(TABLE_WORLD.KEY);
    }

    @Override
    public synchronized Set<UInteger> getAllWorldIds()
    {
        return this.worldIds.keySet();
    }

    @Override
    public Set<UUID> getAllWorldUUIDs()
    {
        return Collections.unmodifiableSet(this.worldUUIDs);
    }

    @Override
    public synchronized World getWorld(UInteger id)
    {
        return this.worldIds.get(id);
    }

    @Override
    public boolean unloadWorld(String worldName)
    {
        return this.unloadWorld(this.getWorld(worldName).get());
    }

    @Override
    public boolean deleteWorld(String worldName) throws IOException
    {
        return this.deleteWorld(this.getWorld(worldName).get());
    }


    @Override
    public synchronized void clean()
    {
        this.worlds.clear();
        this.worldIds.clear();
        this.worldUUIDs.clear();
    }

    @Override
    public List<String> getWorldNames()
    {
        return this.getWorlds().stream().map(World::getName).collect(toList());
    }
}
