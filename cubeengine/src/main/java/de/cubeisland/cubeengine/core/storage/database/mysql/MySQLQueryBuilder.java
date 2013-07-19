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

import java.util.EnumMap;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.AlterTableBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.DatabaseBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.DeleteBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.IndexBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.InsertBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.LockBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.MergeBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.SelectBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.TableBuilder;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.UpdateBuilder;

import org.apache.commons.lang.Validate;

import static de.cubeisland.cubeengine.core.storage.database.AttrType.*;

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
    private MySQLIndexBuilder indexBuilder;
    private MySQLDatabaseBuilder databaseBuilder;

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
        selectBuilder.select();
        if (cols.length > 0)
        {
            return selectBuilder.cols(cols);
        }
        return selectBuilder;
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
    public DeleteBuilder deleteFrom(String table)
    {
        if (this.deleteBuilder == null)
        {
            this.deleteBuilder = new MySQLDeleteBuilder(this);
        }
        this.init();
        return this.deleteBuilder.from(table);
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
    public IndexBuilder createIndex(String name, boolean unique)
    {
        if (this.indexBuilder == null)
        {
            this.indexBuilder = new MySQLIndexBuilder(this);
        }
        this.init();
        return this.indexBuilder.createIndex(name,unique);
    }

    @Override
    public DatabaseBuilder createDatabase(String name, boolean ifNoExist)
    {
        if (this.databaseBuilder == null)
        {
            this.databaseBuilder = new MySQLDatabaseBuilder(this);
        }
        this.init();
        return this.databaseBuilder.createDatabase(name,ifNoExist);
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
    public MySQLQueryBuilder truncateTable(String table)
    {
        assert table != null: "No table specified!";

        this.init();
        this.query.append("TRUNCATE TABLE ").append(this.database.prepareTableName(table));
        return this;
    }

    @Override
    public MySQLQueryBuilder dropTable(String... tables)
    {
        Validate.notEmpty(tables, "No tables specified!");

        this.init();
        this.query.append("DROP TABLE ").append(this.database.prepareTableName(tables[0]));
        for (int i = 1; i < tables.length; ++i)
        {
            this.query.append(',').append(this.database.prepareTableName(tables[i]));
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

    private EnumMap<AttrType, String> attributeMap = new EnumMap<AttrType, String>(AttrType.class)
    {
        {
            this.put(INT, "INT");
            this.put(TINYINT, "TINYINT");
            this.put(SMALLINT, "SMALLINT");
            this.put(MEDIUMINT, "MEDIUMINT");
            this.put(BIGINT, "BIGINT");
            this.put(DECIMAL, "DECIMAL");
            this.put(FLOAT, "FLOAT");
            this.put(DOUBLE, "DOUBLE");
            this.put(REAL, "REAL");
            this.put(BIT, "BIT");
            this.put(BOOLEAN, "BOOLEAN");
            this.put(SERIAL, "SERIAL");
            this.put(DATE, "DATE");
            this.put(DATETIME, "DATETIME");
            this.put(TIMESTAMP, "TIMESTAMP");
            this.put(YEAR, "YEAR");
            this.put(CHAR, "CHAR");
            this.put(VARCHAR, "VARCHAR");
            this.put(TEXT, "TEXT");
            this.put(TINYTEXT, "TINYTEXT");
            this.put(MEDIUMTEXT, "MEDIUMTEXT");
            this.put(BINARY, "BINARY");
            this.put(VARBINARY, "VARBINARY");
            this.put(TINYBLOB, "TINYBLOB");
            this.put(MEDIUMBLOB, "MEDIUMBLOB");
            this.put(BLOB, "BLOB");
            this.put(ENUM, "ENUM");
            this.put(SET, "SET");
        }
    };

    @Override
    public String getAttrTypeString(AttrType attrType)
    {
        String s = this.attributeMap.get(attrType);
        if (s == null) throw new UnsupportedOperationException("The AttributeType " + attrType.name() + " is not supported by this QueryBuilder! " + this.getClass());
        return s;
    }
}
