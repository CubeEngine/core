package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.DeleteBuilder;
import de.cubeisland.cubeengine.core.storage.database.InsertBuilder;
import de.cubeisland.cubeengine.core.storage.database.MergeBuilder;
import de.cubeisland.cubeengine.core.storage.database.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.SelectBuilder;
import de.cubeisland.cubeengine.core.storage.database.TableBuilder;
import de.cubeisland.cubeengine.core.storage.database.UpdateBuilder;
import de.cubeisland.cubeengine.core.util.Validate;

/**
 *
 * @author Anselm Brehme
 */
public class MySQLQueryBuilder implements QueryBuilder
{
    protected StringBuilder query;
    protected MySQLDatabase database;
    
    private MySQLInsertBuilder insertBuilder;
    private MySQLMergeBuilder mergeBuilder;
    private MySQLSelectBuilder selectBuilder;
    private MySQLUpdateBuilder updateBuilder;
    private MySQLDeleteBuilder deleteBuilder;
    private MySQLTableBuilder tableBuilder;

    protected MySQLQueryBuilder(MySQLDatabase database)
    {
        this.database = database;
        
        this.query = null;
        this.insertBuilder = null;
        this.selectBuilder = null;
        this.updateBuilder = null;
        this.deleteBuilder = null;
        this.tableBuilder = null;
    }

    private void init()
    {
        this.query = new StringBuilder();
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

    public MergeBuilder merge()
    {
        if (this.mergeBuilder == null)
        {
            this.mergeBuilder = new MySQLMergeBuilder(this);
        }
        this.init();
        return this.mergeBuilder;
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

    public DeleteBuilder delete()
    {
        if (this.deleteBuilder == null)
        {
            this.deleteBuilder = new MySQLDeleteBuilder(this);
        }
        this.init();
        return this.deleteBuilder;
    }

    public TableBuilder createTable(String name, boolean ifNoExist)
    {
        if (this.tableBuilder == null)
        {
            this.tableBuilder = new MySQLTableBuilder(this);
        }
        this.init();
        return this.tableBuilder.create(name, ifNoExist ? 1 : 2);
    }
    
    public QueryBuilder clearTable(String table)
    {
        Validate.notNull(table, "No table specified!");
        
        this.init();
        this.query.append("TRUNCATE TABLE ").append(this.database.prepareName(table, true));
        
        return this;
    }

    public QueryBuilder dropTable(String... tables)
    {
        Validate.notEmpty(tables, "No tables specified!");
        
        this.init();
        this.query.append("DROP TABLE ").append(this.database.prepareName(tables[0], true));
        for (int i = 1; i < tables.length; ++i)
        {
            this.query.append(',').append(this.database.prepareName(tables[i], true));
        }

        return this;
    }

    public QueryBuilder customSql(String sql)
    {
        this.query.append(sql);
        
        return this;
    }

    public String end()
    {
        if (this.query == null)
        {
            return ""; // TODO silently fail or throw IllegalStateException?
        }
        String res = this.query.toString();
        this.query = null;
        return res;
    }
}