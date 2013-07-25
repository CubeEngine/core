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

import de.cubeisland.engine.core.storage.database.querybuilder.DeleteBuilder;

import org.apache.commons.lang.Validate;

/**
 * MYSQLQueryBuilder for deleting tables.
 */
public class MySQLDeleteBuilder extends MySQLConditionalBuilder<DeleteBuilder> implements DeleteBuilder
{
    protected MySQLDeleteBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    @Override
    public MySQLDeleteBuilder from(String... tables)
    {
        Validate.notEmpty(tables, "No tables specified");

        this.query = new StringBuilder("DELETE FROM ").append(this.database.prepareTableName(tables[0]));
        for (int i = 1; i < tables.length; ++i)
        {
            this.query.append(',').append(this.database.prepareFieldName(tables[i]));
        }
        return this;
    }
}
