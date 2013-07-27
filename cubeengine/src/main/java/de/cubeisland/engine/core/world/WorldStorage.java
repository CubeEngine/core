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
package de.cubeisland.engine.core.world;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.World;

import de.cubeisland.engine.core.storage.SingleKeyStorage;
import de.cubeisland.engine.core.storage.StorageException;
import de.cubeisland.engine.core.storage.database.Database;

public class WorldStorage extends SingleKeyStorage<Long, WorldModel>
{
    private static final int REVISION = 3;

    public WorldStorage(Database database)
    {
        super(database, WorldModel.class, REVISION);
        this.initialize();
    }

    @Override
    protected void prepareStatements() throws SQLException
    {
        super.prepareStatements();
        //TODO DATABASE
        /*
        QueryBuilder builder = this.database.getQueryBuilder();
        this.database.storeStatement(this.modelClass,"getByUUID",
                 builder.select(allFields).from(this.tableName)
                     .where().field("worldUUID").isEqual().value().end().end());
                     */
    }

    public WorldModel get(World world)
    {
        String uuid = world.getUID().toString();
        try
        {
            ResultSet query = this.database.preparedQuery(this.modelClass, "getByUUID", uuid);
            if (query.next())
            {
                WorldModel model = new WorldModel(query.getLong("key"), world.getName(), uuid);
                this.update(model);
                return model;
            }
            return null;
        }
        catch (SQLException e)
        {
            throw new StorageException("Could not get world by UUID!", e, this.database.getStoredStatement(this.modelClass, "getByUUID"));
        }
    }
}
