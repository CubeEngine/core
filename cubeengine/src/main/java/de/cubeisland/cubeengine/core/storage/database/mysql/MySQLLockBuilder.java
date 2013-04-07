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
package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.querybuilder.LockBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;

/**
 * MYSQLQueryBuilder for locking tables.
 */
public class MySQLLockBuilder extends MySQLComponentBuilder<LockBuilder>
    implements LockBuilder
{
    private boolean multiple = false;

    public MySQLLockBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    @Override
    public LockBuilder lock()
    {
        this.query.append("LOCK TABLES ");
        return this;
    }

    @Override
    public LockBuilder table(String table)
    {
        if (multiple)
        {
            this.query.append(", ");

        }
        multiple = true;
        this.query.append(this.database.prepareTableName(table));
        return this;
    }

    @Override
    public LockBuilder read()
    {
        this.query.append(" READ");
        return this;
    }

    @Override
    public LockBuilder write()
    {
        this.query.append(" WRITE");
        return this;
    }

    @Override
    public QueryBuilder end()
    {
        this.multiple = false;
        return super.end();
    }
}
