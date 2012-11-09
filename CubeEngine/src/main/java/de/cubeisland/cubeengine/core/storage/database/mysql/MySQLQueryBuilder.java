package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.AlterTableBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.DeleteBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.InsertBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.LockBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.MergeBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.SelectBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.TableBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.UpdateBuilder;
import org.apache.commons.lang.Validate;

/**
 * QueryBuilder implementation for MYSQL.
 */
public class MySQLQueryBuilder implements QueryBuilder
{
    private MySQLInsertBuilder insertBuilder;
    private MySQLMergeBuilder mergeBuilder;
    private MySQLSelectBuilder selectBuilder;
    private MySQLUpdateBuilder updateBuilder;
    private MySQLDeleteBuilder deleteBuilder;
    private MySQLTableBuilder tableBuilder;
    private MySQLLockBuilder lockBuilder;
    private MySQLAlterTableBuilder alterTableBuilder;
    protected Database database;
    protected StringBuilder query;
    private boolean nextQuery = false;

    protected MySQLQueryBuilder(MySQLDatabase database)
    {
        this.insertBuilder = null;
        this.selectBuilder = null;
        this.updateBuilder = null;
        this.deleteBuilder = null;
        this.tableBuilder = null;

        this.database = database;
    }

    private void init()
    {
        if (!this.nextQuery)//If is next query do not clear StringBuilder
        {
            this.query = new StringBuilder();
        }
        this.nextQuery = false;
    }

    public QueryBuilder clear()
    {
        this.query = new StringBuilder();
        return this;
    }

    @Override
    public InsertBuilder insert()
    {
        if (this.insertBuilder == null)
        {
            this.insertBuilder = new MySQLInsertBuilder(this);
        }
        this.init();
        return this.insertBuilder;
    }

    @Override
    public MergeBuilder merge()
    {
        if (this.mergeBuilder == null)
        {
            this.mergeBuilder = new MySQLMergeBuilder(this);
        }
        this.init();
        return this.mergeBuilder;
    }

    @Override
    public SelectBuilder select(String... cols)
    {
        if (this.selectBuilder == null)
        {
            this.selectBuilder = new MySQLSelectBuilder(this);
        }
        this.init();
        return selectBuilder.cols(cols);
    }

    @Override
    public UpdateBuilder update(String... tables)
    {
        if (this.updateBuilder == null)
        {
            this.updateBuilder = new MySQLUpdateBuilder(this);
        }
        this.init();
        return this.updateBuilder.tables(tables);
    }

    @Override
    public DeleteBuilder delete()
    {
        if (this.deleteBuilder == null)
        {
            this.deleteBuilder = new MySQLDeleteBuilder(this);
        }
        this.init();
        return this.deleteBuilder;
    }

    @Override
    public TableBuilder createTable(String name, boolean ifNoExist)
    {
        if (this.tableBuilder == null)
        {
            this.tableBuilder = new MySQLTableBuilder(this);
        }
        this.init();
        return this.tableBuilder.create(name, ifNoExist ? 1 : 2);
    }

    @Override
    public LockBuilder lock()
    {
        if (this.lockBuilder == null)
        {
            this.lockBuilder = new MySQLLockBuilder(this);
        }
        this.init();
        return this.lockBuilder.lock();
    }

    @Override
    public MySQLQueryBuilder clearTable(String table)
    {
        Validate.notNull(table, "No table specified!");

        this.init();
        this.query.append("TRUNCATE TABLE ").append(this.database.prepareName(table));
        return this;
    }

    @Override
    public MySQLQueryBuilder dropTable(String... tables)
    {
        Validate.notEmpty(tables, "No tables specified!");

        this.init();
        this.query.append("DROP TABLE ").append(this.database.prepareName(tables[0]));
        for (int i = 1; i < tables.length; ++i)
        {
            this.query.append(',').append(this.database.prepareName(tables[i]));
        }
        return this;
    }

    @Override
    public AlterTableBuilder alterTable(String table)
    {
        if (this.alterTableBuilder == null)
        {
            this.alterTableBuilder = new MySQLAlterTableBuilder(this);
        }
        this.init();
        return this.alterTableBuilder.alterTable(table);
    }

    @Override
    public QueryBuilder startTransaction()
    {
        this.init();
        this.query.append("START TRANSACTION");
        return this;
    }

    @Override
    public QueryBuilder commit()
    {
        this.init();
        this.query.append("COMMIT");
        return this;
    }

    @Override
    public QueryBuilder rollback()
    {
        this.init();
        this.query.append("ROLLBACK");
        return this;
    }

    @Override
    public QueryBuilder unlockTables()
    {
        this.init();
        this.query.append("UNLOCK TABLES");
        return this;
    }

    @Deprecated
    /**
     * Database wont understand multiple queries
     */
    @Override
    public QueryBuilder nextQuery()
    {
        this.query.append(";\n");
        this.nextQuery = true;
        return this;
    }

    @Override
    public String end()
    {
        if (this.query == null)
        {
            throw new IllegalStateException("Query was null!");
        }
        String res = this.query.toString();
        this.query = null;
        this.nextQuery = false;
        return res;
    }
}