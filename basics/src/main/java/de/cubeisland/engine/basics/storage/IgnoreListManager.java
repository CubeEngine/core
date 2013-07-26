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
package de.cubeisland.engine.basics.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.storage.StorageException;
import de.cubeisland.engine.core.storage.TwoKeyStorage;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.Profiler;

import gnu.trove.map.hash.TLongObjectHashMap;

public class IgnoreListManager extends TwoKeyStorage<Long, Long, IgnoreList>
{

    private static final int REVISION = 1;
    private TLongObjectHashMap<List<Long>> ignoreList = new TLongObjectHashMap<List<Long>>();

    public IgnoreListManager(Database database)
    {
        super(database, IgnoreList.class, REVISION);
        CubeEngine.getLog().trace("{} ms - IgnoreList.Manager-super", Profiler.getCurrentDelta("basicsEnable", TimeUnit.MILLISECONDS));
        this.initialize();
        CubeEngine.getLog().trace("{} ms - IgnoreList.Manager-init", Profiler.getCurrentDelta("basicsEnable", TimeUnit.MILLISECONDS));
    }

    @Override
    protected void prepareStatements() throws SQLException
    {
        super.prepareStatements();
        this.database.storeStatement(modelClass, "getAllByUser", this.database.getQueryBuilder()
                                                                              .select().wildcard().from(this.tableName)
                                                                              .where().field("key").isEqual().value().end().end());
    }

    public boolean addIgnore(User user, User toIgnore)
    {
        if (this.checkIgnore(user, toIgnore))
        {
            return false;
        }
        this.store(new IgnoreList(user, toIgnore));
        this.ignoreList.get(user.key).add(toIgnore.key);
        return true;
    }

    public boolean removeIgnore(User user, User toUnIgnore)
    {
        if (!this.checkIgnore(user, toUnIgnore))
        {
            return false;
        }
        this.delete(new IgnoreList(user, toUnIgnore));
        this.ignoreList.get(user.key).remove(toUnIgnore.key);
        return true;
    }

    public boolean checkIgnore(User user, User doesIgnore)
    {
        if (this.ignoreList.containsKey(user.key))
        {
            List<Long> ignored = this.ignoreList.get(user.key);
            return ignored.contains(doesIgnore.key);
        }
        try
        {
            List<Long> ignored = this.ignoreList.get(user.key);
            if (ignored == null)
            {
                ignored = new ArrayList<Long>();
                this.ignoreList.put(user.key, ignored);
            }
            ignored.clear();
            ResultSet result = this.database.preparedQuery(modelClass, "getAllByUser", user.key);
            while (result.next())
            {
                ignored.add(result.getLong("ignore"));
            }
            return ignored.contains(doesIgnore.key);
        }
        catch (SQLException e)
        {
            throw new StorageException("Error while getting models form database", e,this.database.getStoredStatement(modelClass, "getAllByUser"));
        }
    }
}
