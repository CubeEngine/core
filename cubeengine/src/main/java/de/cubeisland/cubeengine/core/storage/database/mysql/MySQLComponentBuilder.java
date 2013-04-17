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

import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.IndexBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import java.sql.Timestamp;

/**
 * Abstract MYSQLlQueryBuilder used by other builders.
 */
public abstract class MySQLComponentBuilder<This extends ComponentBuilder> implements ComponentBuilder<This>
{
    protected StringBuilder query;
    protected Database database;
    private boolean inFunction = false;
    private boolean deepInFunction = false;
    protected Integer subDepth = 0;
    protected MySQLQueryBuilder parent;

    public MySQLComponentBuilder(MySQLQueryBuilder parent)
    {
        this.parent = parent;
        this.database = parent.database;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This function(String function)
    {
        this.query.append(' ').append(function).append("()");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This beginFunction(String function)
    {
        this.query.append(' ').append(function).append('(');
        this.inFunction = true;
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This endFunction()
    {
        this.deepInFunction = false;
        this.inFunction = false;
        this.query.append(')');
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This field(String name)
    {
        this.query.append(this.database.prepareFieldName(name));
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This values(int amount)
    {
        this.query.append(" \nVALUES");
        return this.valuesInBrackets(amount);
    }

    @Override
    public This valuesInBrackets(int amount)
    {
        this.beginSub().value();
        for (int i = 1; i < amount; ++i)
        {
            this.query.append(",?");
        }
        return (This)this.endSub();
    }

    @Override
    public This valuesInBrackets(Object[] values)
    {
        this.beginSub().value(values[0]);
        for (int i = 1; i < values.length; ++i)
        {
            this.query.append(",");
            this.value(values[i]);
        }
        return (This)this.endSub();
    }

    @Override
    @SuppressWarnings("unchecked")
    public This value(Object value)
    {
        if (inFunction)
        {
            if (deepInFunction)
            {
                query.append(',');
            }
            else
            {
                deepInFunction = true;
            }
        }
        if (value == null)
        {
            this.query.append("NULL");
        }
        else if (value instanceof String || value instanceof Timestamp)
        {
            this.query.append(this.database.prepareString(value.toString()));
        }
        else
        {
            this.query.append(value.toString());
        }
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This is(int operation)
    {
        switch (operation)
        {
            case 1:
                this.query.append('=');
                break;
            case 2:
                this.query.append("!=");
                break;
            case 3:
                this.query.append('<');
                break;
            case 4:
                this.query.append("<=");
                break;
            case 5:
                this.query.append('>');
                break;
            case 6:
                this.query.append(">=");
                break;
            default:
                throw new IllegalStateException("Invalid operation");
        }
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This isEqual()
    {
        this.query.append(" = ");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This not()
    {
        this.query.append(" NOT");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This and()
    {
        this.query.append(" AND ");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This or()
    {
        this.query.append(" OR ");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This beginSub()
    {
        this.query.append("\n(");
        ++this.subDepth;
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This endSub()
    {
        if (this.subDepth > 0)
        {
            this.query.append(")");
            --this.subDepth;
        }
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This wildcard()
    {
        this.query.append('*');
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This value()
    {
        this.query.append('?');
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This like()
    {
        this.query.append(" LIKE ");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This as(String field)
    {
        this.query.append(" AS ").append(this.database.prepareFieldName(field));
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This in()
    {
        this.query.append(" IN ");
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This groupBy(String... fields)
    {
        if (fields.length == 1)
        {
            this.query.append(" GROUP BY ").append(this.database.prepareFieldName(fields[0]));
        }
        else
        {
            this.query.append(" GROUP BY ").append(this.database.prepareFieldName(fields[0]));
            for (int i = 1; i < fields.length; ++i)
            {
                this.query.append(',').append(this.database.prepareFieldName(fields[i]));
            }
        }
        return (This)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public This having()
    {
        this.query.append(" HAVING");
        return (This)this;
    }

    @Override
    public QueryBuilder end()
    {
        if (this.subDepth != 0)
        {
            this.subDepth = 0;
            throw new IllegalStateException("Not all subs where ended!");
        }
        this.parent.query.append(this.query);
        this.query = null;
        return this.parent;
    }

    @Override
    public This fieldsInBrackets(String[] fields)
    {
        if (fields.length == 0)
        {
            throw new IllegalArgumentException("The fields cannot be none!");
        }
        this.beginSub().field(fields[0]);
        for (int i = 1; i < fields.length; ++i)
        {
            this.query.append(", ");
            this.field(fields[i]);
        }
        return this.endSub();
    }
}
