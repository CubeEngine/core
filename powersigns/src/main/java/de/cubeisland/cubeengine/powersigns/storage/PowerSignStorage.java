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
package de.cubeisland.cubeengine.powersigns.storage;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.World;

import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.SelectBuilder;
import de.cubeisland.cubeengine.core.world.WorldManager;
import de.cubeisland.cubeengine.powersigns.Powersigns;

public class PowerSignStorage extends SingleKeyStorage<Long,PowerSignModel>
{
    private WorldManager wm;

    public PowerSignStorage(Powersigns module)
    {
        super(module.getCore().getDB(), PowerSignModel.class, 1);
        this.wm = module.getCore().getWorldManager();
        this.initialize();
    }

    @Override
    protected void prepareStatements() throws SQLException
    {
        super.prepareStatements();
        this.database.storeStatement(this.modelClass,"getFromChunk",this.database.getQueryBuilder().
                                    select().wildcard().from(this.tableName).where().
                                    field("chunkX").isEqual().value().and().
                                    field("chunkZ").isEqual().value().and().
                                    field("world").isEqual().value().end().end());
    }

    public Set<PowerSignModel> loadFromLoadedChunks(Set<World> worlds)
    {
        SelectBuilder builder = this.database.getQueryBuilder().select().wildcard().from(this.tableName).where();
        boolean first = true;
        for (World world : worlds)
        {
            if (world.getLoadedChunks().length == 0)
            {
                continue;
            }
            if (!first)
            {
                builder.or();
            }
            first = false;
            builder.beginSub().field("world").isEqual().value(this.wm.getWorldId(world)).and().beginSub();
            boolean firstChunk = true;
            for (Chunk chunk : world.getLoadedChunks())
            {
                if (!firstChunk)
                {
                    builder.or();
                }
                firstChunk = false;
                builder.beginSub().field("chunkX").isEqual().value(chunk.getX()).and().field("chunkZ").isEqual().value(chunk.getZ()).endSub();
            }
            builder.endSub().endSub();
        }
        String sql = builder.end().end();
        try
        {
            Set<PowerSignModel> models = new HashSet<PowerSignModel>();
            ResultSet resultSet = this.database.query(sql);
            while (resultSet.next())
            {
                PowerSignModel loadedModel = this.modelClass.newInstance();
                for (Field field : this.fieldNames.keySet())
                {
                    field.set(loadedModel, resultSet.getObject(this.fieldNames.get(field)));
                }
                models.add(loadedModel);
            }
            return models;
        }
        catch (InstantiationException e)
        {
            throw new IllegalStateException("Error while creating new PowerSignModel",e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("Error while setting fields of PowerSignModel",e);
        }
        catch (SQLException e)
        {
            throw new StorageException("Error while loading powersigns on startup!",e);
        }
    }

    public Set<PowerSignModel> loadFromChunk(Chunk chunk)
    {
        try
        {
            Set<PowerSignModel> models = new HashSet<PowerSignModel>();
            ResultSet resultSet = this.database.preparedQuery(this.modelClass,"getFromChunk",chunk.getX(),chunk.getZ(),this.wm.getWorldId(chunk.getWorld()));
            while (resultSet.next())
            {
                PowerSignModel loadedModel = this.modelClass.newInstance();
                for (Field field : this.fieldNames.keySet())
                {
                    field.set(loadedModel, resultSet.getObject(this.fieldNames.get(field)));
                }
                models.add(loadedModel);
            }
            return models;
        }
        catch (InstantiationException e)
        {
            throw new IllegalStateException("Error while creating new PowerSignModel",e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("Error while setting fields of PowerSignModel",e);
        }
        catch (SQLException e)
        {
            throw new StorageException("Error while loading powersigns on startup!",e);
        }
    }
}
