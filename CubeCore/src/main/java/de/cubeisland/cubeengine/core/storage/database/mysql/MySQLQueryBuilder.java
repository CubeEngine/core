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
    
    private MySQLInsertBuilder insertBuilder;
    private MySQLSelectBuilder selectBuilder;
    private MySQLUpdateBuilder updateBuilder;
    private MySQLDeleteBuilder deleteBuilder;
    private MySQLTableBuilder tableBuilder;

    public MySQLQueryBuilder(MySQLDatabase database)
    {
        this.database = database;
        
        this.query = null;
        this.insertBuilder = null;
        this.selectBuilder = null;
        this.updateBuilder = null;
        this.deleteBuilder = null;
        this.tableBuilder = null;
    }

    public TableBuilder createTable(String name, boolean ifNoExist)
    {
        if (this.tableBuilder == null)
        {
            this.tableBuilder = new MySQLTableBuilder(this);
        }
        return this.tableBuilder.create(name, ifNoExist ? 1 : 2);
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
        if (this.insertBuilder == null)
        {
            this.insertBuilder = new MySQLInsertBuilder(this);
        }
        this.init();
        return this.insertBuilder;
    }

    public SelectBuilder select(String... cols)
    {
        if (this.selectBuilder == null)
        {
            this.selectBuilder = new MySQLSelectBuilder(this);
        }
        this.init();
        return selectBuilder.cols(cols);
    }

    public UpdateBuilder update(String... tables)
    {
        if (this.updateBuilder == null)
        {
            this.updateBuilder = new MySQLUpdateBuilder(this);
        }
        this.init();
        return this.updateBuilder.tables(tables);
    }
    
    public UpdateBuilder onDuplicateUpdate()
    {
        this.query.append("ON DUPLICATE KEY ");
        return this.update();
    }

    public DeleteBuilder delete()
    {
        if (this.deleteBuilder == null)
        {
            this.deleteBuilder = new MySQLDeleteBuilder(this);
        }
        this.init();
        return this.deleteBuilder;
    }

    public QueryBuilder dropTable(String table)
    {
        this.init();
        this.query.append("DROP TABLE ").append(this.database.quote(table));
        return this;
    }

    public QueryBuilder customSql(String sql)
    {
        this.query.append(sql);
        
        return this;
    }

    private void init()
    {
        this.query = new StringBuilder();
    }
}