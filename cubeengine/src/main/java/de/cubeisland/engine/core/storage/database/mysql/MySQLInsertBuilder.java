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

import de.cubeisland.engine.core.storage.database.querybuilder.InsertBuilder;
import de.cubeisland.engine.core.storage.database.querybuilder.QueryBuilder;

import org.apache.commons.lang.Validate;

/**
 * MYSQLQueryBuilder for inserting into tables.
 */
public class MySQLInsertBuilder extends MySQLComponentBuilder<InsertBuilder>
    implements InsertBuilder
{
    protected MySQLInsertBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    private boolean colsSet;
    private boolean valuesSet;

    @Override
    public MySQLInsertBuilder into(String table)
    {
        assert table != null: "The table name must not be null!";

        this.query = new StringBuilder("INSERT INTO ").append(this.database.prepareTableName(table)).append(' ');
        return this;
    }

    @Override
    public MySQLInsertBuilder cols(String... cols)
    {
        Validate.notEmpty(cols, "You have to specify at least one col to insert");
        Validate.noNullElements(cols, "Column names must not be null!");

        this.query.append('(').append(this.database.prepareFieldName(cols[0]));
        for (int i = 1; i < cols.length; ++i)
        {
            this.query.append(',').append(this.database.prepareFieldName(cols[i]));
        }
        this.query.append(")");
        this.colsSet = true;
        return this.values(cols.length);
    }

    @Override
    public MySQLInsertBuilder allCols()
    {
        this.colsSet = true;
        return this;
    }

    @Override
    public MySQLInsertBuilder values(int amount)
    {
        if (amount <= 0)
        {
            throw new IllegalStateException("Cannot add less than one value!");
        }
        super.values(amount);
        this.valuesSet = true;
        return this;
    }

    @Override
    public QueryBuilder end()
    {
        if (!(this.colsSet && this.valuesSet))
        {
            throw new IllegalStateException("Cols and/or amount of values not defined!");
        }
        this.colsSet = false;
        this.valuesSet = false;
        return super.end();
    }
}
