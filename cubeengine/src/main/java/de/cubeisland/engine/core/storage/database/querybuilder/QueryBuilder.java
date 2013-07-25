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
package de.cubeisland.engine.core.storage.database.querybuilder;

import de.cubeisland.engine.core.storage.database.AttrType;

public interface QueryBuilder
{
    /**
     * Starts an INSERT query.
     *
     * @return the InsertBuilder
     */
    public InsertBuilder insert();

    /**
     * Starts an MERGE query.
     *
     * @return the MergeBuilder
     */
    public MergeBuilder merge();

    /**
     * Starts an SELECT query.
     *
     * @param cols the tables to select from
     * @return the SelectBuilder
     */
    public SelectBuilder select(String... cols);

    /**
     * Starts an UPDATE query.
     *
     * @param tables the tables to update from
     * @return the UpdateBuilder
     */
    public UpdateBuilder update(String... tables);

    /**
     * Starts an DELETE query.
     *
     * @return the DeleteBuilder
     */
    public DeleteBuilder deleteFrom(String table);

    /**
     * STARTS a CREATE TABLE query
     *
     * @param name the table name
     * @param ifNoExist whether to only create the table if it doesn't already exist
     * @return the TableBuilder
     */
    public TableBuilder createTable(String name, boolean ifNoExist);

    /**
     * STARTS a CREATE INDEX query
     *
     * @param name the index name
     * @param unique whether to create a unique index
     * @return the IndexBuilder
     */
    public IndexBuilder createIndex(String name, boolean unique);

    /**
     * Creates a Database with given name
     *
     * @param name
     * @param ifNoExist
     * @return
     */
    public DatabaseBuilder createDatabase(String name, boolean ifNoExist);

    /**
     * Clears the table
     *
     * @param table the table to truncate
     * @return fluent interface
     */
    public QueryBuilder truncateTable(String table);

    /**
     * Drops the table
     *
     * @param tables the tables to drop
     * @return fluent interface
     */
    public QueryBuilder dropTable(String... tables);

    /**
     * Starts a LOCK query
     *
     * @return the LockBuilder
     */
    public LockBuilder lock();

    /**
     * Starts a transaction
     *
     * @return fluent interface
     */
    public QueryBuilder startTransaction();

    /**
     * Commits transactions
     *
     * @return fluent interface
     */
    public QueryBuilder commit();

    /**
     * Rollbacks transactions
     *
     * @return fluent interface
     */
    public QueryBuilder rollback();

    /**
     * Unlocks tables.
     *
     * @return fluent interface
     */
    public QueryBuilder unlockTables();

    /**
     * Starts an other query.
     *
     * @return fluent interface
     */
    public QueryBuilder nextQuery();

    /**
     * Starts an ALTER TABLE query.
     *
     * @return the AlterTableBuilder
     */
    public AlterTableBuilder alterTable(String table);

    /**
     * Returns the finished query.
     *
     * @return the query as String
     */
    public String end();

    /**
     * Returns the String for the implementation of given AttributeType
     *
     * @param attrType the AttributeType
     * @return the String
     */
    String getAttrTypeString(AttrType attrType);
}
