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

import de.cubeisland.engine.core.storage.database.querybuilder.IndexBuilder;

public class MySQLIndexBuilder extends MySQLComponentBuilder<IndexBuilder> implements IndexBuilder
{
    public MySQLIndexBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    @Override
    public IndexBuilder createIndex(String name)
    {
        return this.createIndex(name,false);
    }

    @Override
    public IndexBuilder createIndex(String name, boolean unique)
    {
        this.query.append("CREATE ");
        if (unique)
        {
            this.query.append(" UNIQUE ");
        }
        this.query.append("INDEX ");
        return this;
    }

    @Override
    public IndexBuilder on(String table, String... fields)
    {
        //   this.query.append(" ON ").append(this.database.prepareTableName(table));
        return this.fieldsInBrackets(fields);
    }
}
