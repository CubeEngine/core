package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.DeleteBuilder;
import de.cubeisland.cubeengine.core.storage.database.InsertBuilder;
import de.cubeisland.cubeengine.core.storage.database.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.SelectBuilder;
import de.cubeisland.cubeengine.core.storage.database.TableBuilder;
import de.cubeisland.cubeengine.core.storage.database.UpdateBuilder;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLQueryBuilder implements QueryBuilder
{
    protected StringBuilder query;
    protected MySQLDatabase database;

    public MySQLQueryBuilder(MySQLDatabase database)
    {
        this.query = null;
        this.database = database;
    }

    public TableBuilder createTable(String name, boolean ifNoExist)
    {
        return new MySQLTableBuilder(this, name, ifNoExist ? 1 : 2);
    }

    public String end()
    {
        String res = this.query.toString();
        this.query = null;
        return res;
    }

    public QueryBuilder clear()
    {
        this.query = new StringBuilder();
        return this;
    }

    public InsertBuilder insert()
    {
        return new MySQLInsertBuilder(this);
    }

    public SelectBuilder select()
    {
        return new MySQLSelectBuilder(this);
    }

    public UpdateBuilder update()
    {
        return new MySQLUpdateBuilder(this);
    }
    
    public UpdateBuilder onDuplicateUpdate()
    {
        query.append("ON DUPLICATE KEY ");
        return new MySQLUpdateBuilder(this);
    }

    public DeleteBuilder delete()
    {
        return new MySQLDeleteBuilder(this);
    }

    public QueryBuilder dropTable(String table)
    {
        query.append("DROP TABLE ").append(database.quote(table));
        return this;
    }

    public QueryBuilder initialize()
    {
        this.query = new StringBuilder();
        return this;
    }
}