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
package de.cubeisland.engine.core.storage.database.mysql;

import de.cubeisland.engine.core.storage.database.querybuilder.SelectBuilder;

import org.apache.commons.lang.Validate;

/**
 * MYSQLQueryBuilder for selecting from tables.
 */
public class MySQLSelectBuilder extends MySQLConditionalBuilder<SelectBuilder> implements SelectBuilder
{
    protected MySQLSelectBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    @Override
    public MySQLSelectBuilder select()
    {
        if (this.query != null)
        {
            throw new IllegalStateException("Cannot create a nested SELECT query!");
        }
        this.query = new StringBuilder("SELECT ");
        return this;
    }

    @Override
    public MySQLSelectBuilder cols(String... cols)
    {
        if (cols.length > 0)
        {
            this.query.append(this.database.prepareFieldName(cols[0]));
            for (int i = 1; i < cols.length; ++i)
            {
                this.query.append(',').append(this.database.prepareFieldName(cols[i]));
            }
        }
        return this;
    }

    @Override
    public MySQLSelectBuilder from(String... tables)
    {
        Validate.notEmpty(tables, "No tables specified!");
        this.query.append(" \nFROM ").append(this.database.prepareTableName(tables[0]));
        for (int i = 1; i < tables.length; ++i)
        {
            this.query.append(',').append(this.database.prepareTableName(tables[i]));
        }
        return this;
    }

    @Override
    public MySQLSelectBuilder distinct()
    {
        this.query.append(" DISTINCT");
        return this;
    }

    @Override
    public MySQLSelectBuilder union(boolean all)
    {
        this.query.append(" UNION ");
        return this;
    }

    @Override
    public SelectBuilder into(String table)
    {
        this.query.append(" \nINTO ").append(this.database.prepareTableName(table));
        return this;
    }

    @Override
    public SelectBuilder in(String database)
    {
        this.query.append(" IN ").append(this.database.prepareFieldName(database));
        return this;
    }

    @Override
    public SelectBuilder leftJoinOnEqual(String table, String key, String otherTable, String otherKey)
    {
        this.query.append(" \nLEFT JOIN ").append(this.database.prepareTableName(table));
        this.onEqual(table, key, otherTable, otherKey);
        return this;
    }

    private void onEqual(String table, String key, String otherTable, String otherKey)
    {
        this.query.append(" \nON ").append(this.database.prepareFieldName(table + "." + key))
                .append(" = ").append(this.database.prepareFieldName(otherTable + "." + otherKey));
    }

    @Override
    public SelectBuilder rightJoinOnEqual(String table, String key, String otherTable, String otherKey)
    {
        this.query.append(" \nRIGHT JOIN ").append(this.database.prepareTableName(table));
        this.onEqual(table, key, otherTable, otherKey);
        return this;
    }

    @Override
    public SelectBuilder joinOnEqual(String table, String key, String otherTable, String otherKey)
    {
        this.query.append(" \nJOIN ").append(this.database.prepareTableName(table));
        this.onEqual(table, key, otherTable, otherKey);
        return this;
    }
}
