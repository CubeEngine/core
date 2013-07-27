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

import de.cubeisland.engine.core.storage.database.querybuilder.ConditionalBuilder;
import de.cubeisland.engine.core.storage.database.querybuilder.QueryBuilder;

import org.apache.commons.lang.Validate;

/**
 * Abstract MYSQLlQueryBuilder used by other builders.
 */
public abstract class MySQLConditionalBuilder<This extends ConditionalBuilder>
        extends MySQLComponentBuilder<This> implements ConditionalBuilder<This>
{
    protected MySQLConditionalBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    private boolean orderBy = false;

    @Override
    @SuppressWarnings("unchecked")
    public This orderBy(String... cols)
    {
        Validate.notEmpty(cols, "No cols specified!");
        if (!orderBy)
        {
            orderBy = true;
            this.query.append(" ORDER BY ");
        }
        else
        {
            this.query.append(",");
        }
        //   this.query.append(this.database.prepareFieldName(cols[0]));
        for (int i = 1; i < cols.length; ++i)
        {
            //  this.query.append(',').append(this.database.prepareFieldName(cols[i]));
        }
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This limit(int n)
    {
        this.query.append(" LIMIT ").append(n);
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This limit()
    {
        this.query.append(" LIMIT ?");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This offset(int n)
    {
        this.query.append(" OFFSET ").append(n);
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This offset()
    {
        this.query.append(" OFFSET ?");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This where()
    {
        this.query.append(" \nWHERE ");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This between()
    {
        this.query.append(" BETWEEN ");
        return (This)this.value().and().value();
    }

    @Override
    public This between(Object val1, Object val2)
    {
        this.query.append(" BETWEEN ");
        return (This)this.value(val1).and().value(val2);
    }

    @Override
    @SuppressWarnings("unchecked")
    public This asc()
    {
        this.query.append(" ASC ");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This desc()
    {
        this.query.append(" DESC ");
        return (This)this;
    }

    @Override
    public QueryBuilder end()
    {
        this.orderBy = false;
        return super.end();
    }
}
