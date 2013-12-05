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
package de.cubeisland.engine.core.storage.database;

import java.util.ArrayList;
import java.util.List;

import de.cubeisland.engine.core.storage.database.mysql.Keys;
import de.cubeisland.engine.core.util.Version;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;

public abstract class Table<R extends Record, K extends Number> extends TableImpl<R> implements TableCreator<R>
{
    public Table(String name, Version version)
    {
        super(name);
        this.version = version;
    }

    private final Version version;

    public final Table<R, K> setPrimaryKey(TableField<R, K> field)
    {
        this.identity = Keys.identity(this, field);
        this.primaryKey = Keys.uniqueKey(this, field);
        this.uniqueKeys.add(primaryKey);
        return this;
    }

    public final Table<R, K> addForeignKey(UniqueKey referencedKey, TableField<R, ?>... fields)
    {
        this.foreignKeys.add(Keys.foreignKey(referencedKey, this, fields));
        return this;
    }

    public final Table<R, K> addUniqueKey(TableField<R, ?>... fields)
    {
        this.uniqueKeys.add(Keys.uniqueKey(this, fields));
        return this;
    }

    private Identity<R, K> identity;
    private UniqueKey<R> primaryKey;
    private List<ForeignKey<R, ?>> foreignKeys = new ArrayList<>();
    private List<UniqueKey<R>> uniqueKeys = new ArrayList<>();

    @Override
    public Identity<R, K> getIdentity()
    {
        return identity;
    }

    @Override
    public UniqueKey<R> getPrimaryKey()
    {
        return primaryKey;
    }

    @Override
    public List<UniqueKey<R>> getKeys()
    {
        return uniqueKeys;
    }

    @Override
    public List<ForeignKey<R, ?>> getReferences() {
        return foreignKeys;
    }

    @Override
    public abstract Class<R> getRecordType();


    @Override
    public Version getTableVersion()
    {
        return version;
    }

}
