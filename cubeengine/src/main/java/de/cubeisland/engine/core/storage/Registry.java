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
package de.cubeisland.engine.core.storage;

import java.sql.ResultSet;
import java.sql.SQLException;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.storage.database.AttrType;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.querybuilder.QueryBuilder;

import gnu.trove.map.hash.THashMap;

public class Registry
{
    private String TABLENAME = "registry";
    private THashMap<String, THashMap<String, String>> data = new THashMap<>();
    private final Database database;

    public Registry(Database database)
    {
        this.database = database;
        try
        {
            QueryBuilder builder = database.getQueryBuilder();
            String sql = builder.createTable(TABLENAME, true).beginFields()
                    .field("key", AttrType.VARCHAR, 16)
                    .field("module", AttrType.VARCHAR, 16)
                    .field("value", AttrType.VARCHAR, 256)
                    .primaryKey("key", "module")
                    // TODO module thingy... foreignKey("module").references("modules", "key")
                    .endFields()
                    .engine("InnoDB").defaultcharset("utf8").end().end();
            database.execute(sql);
            database.storeStatement(this.getClass(), "getAllByModule", builder.select("key", "value").from(TABLENAME).where().field("module").isEqual().value().end().end());
            database.storeStatement(this.getClass(), "merge", builder.merge().into(TABLENAME).cols("key", "module", "value").updateCols("value").end().end());
            database.storeStatement(this.getClass(), "delete", builder.deleteFrom(TABLENAME).where().field("key").isEqual().value().and().field("module").isEqual().value().end().end());
            database.storeStatement(this.getClass(), "clear", builder.deleteFrom(TABLENAME).where().field("module").isEqual().value().end().end());

        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while creating Registry-Statements");
        }
    }

    public void merge(Module module, String key, String value)
    {
        try
        {
            this.loadForModule(module);
            this.database.preparedExecute(this.getClass(), "merge", key, module.getId(), value);
            this.data.get(module.getId()).put(key, value);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while merging Registry");
        }
    }

    public String delete(Module module, String key)
    {
        try
        {
            this.loadForModule(module);
            this.database.preparedExecute(this.getClass(), "delete", key, module.getId());
            return this.data.get(module.getId()).remove(key);
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while deleting Registry");
        }
    }

    public void loadForModule(Module module)
    {
        if (this.data.get(module.getId()) == null)
        {
            try
            {
                ResultSet result = this.database.preparedQuery(this.getClass(), "getAllByModule", module.getId());
                THashMap<String, String> map = this.data.get(module.getId());
                if (map == null)
                {
                    map = new THashMap<>();
                    this.data.put(module.getId(), map);
                }
                map.clear();
                while (result.next())
                {
                    map.put(result.getString("key"), result.getString("value"));
                }
            }
            catch (SQLException ex)
            {
                throw new StorageException("Error while loading Registry");
            }
        }
    }

    public String getValue(String key, Module module)
    {
        this.loadForModule(module);
        return this.data.get(module.getId()).get(key);
    }

    public void clear(Module module)
    {
        try
        {
            this.database.preparedExecute(this.getClass(), "clear", module.getId());
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while clearing Registry");
        }
    }
}
