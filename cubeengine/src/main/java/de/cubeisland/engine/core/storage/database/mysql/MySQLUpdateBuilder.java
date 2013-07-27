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

import de.cubeisland.engine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.engine.core.storage.database.querybuilder.UpdateBuilder;

import org.apache.commons.lang.Validate;

/**
 * MYSQLQueryBuilder for updating tables.
 */
public class MySQLUpdateBuilder extends MySQLConditionalBuilder<UpdateBuilder>
    implements UpdateBuilder
{
    private boolean hasCols;

    protected MySQLUpdateBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    @Override
    public MySQLUpdateBuilder tables(String... tables)
    {
        Validate.notEmpty(tables, "No tables specified!");

        this.hasCols = false;
        this.query = new StringBuilder("UPDATE ");
        //this.query.append(this.database.prepareTableName(tables[0]));
        for (int i = 1; i < tables.length; ++i)
        {
          //  this.query.append(',').append(this.database.prepareTableName(tables[i]));
        }
        return this;
    }

    @Override
    public MySQLUpdateBuilder set(String... cols)
    {
        Validate.notEmpty(cols, "No cols specified!");

        this.query.append(" SET ");

        return this.cols(cols);
    }

    @Override
    public MySQLUpdateBuilder cols(String... cols)
    {
        if (cols.length > 0)
        {
       //     this.query.append(this.database.prepareFieldName(cols[0])).append("=? ");
            for (int i = 1; i < cols.length; ++i)
            {
         //       this.query.append(',').append(this.database.prepareFieldName(cols[i])).append("=? ");
            }
            this.hasCols = true;
        }
        return this;
    }

    @Override
    public QueryBuilder end()
    {
        if (!this.hasCols)
        {
            throw new IllegalStateException("No cols where specified!");
        }
        this.hasCols = false;
        return super.end();
    }
}
