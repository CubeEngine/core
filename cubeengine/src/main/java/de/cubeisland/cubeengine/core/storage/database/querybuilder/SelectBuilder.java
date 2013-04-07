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
package de.cubeisland.cubeengine.core.storage.database.querybuilder;

public interface SelectBuilder extends ConditionalBuilder<SelectBuilder>
{
    /**
     * Adds the SELECT statement
     *
     * @return fluent interface
     */
    public SelectBuilder select();

    /**
     * Adds the cols to select.
     *
     * @param cols
     * @return fluent interface
     */
    public SelectBuilder cols(String... cols);

    /**
     * Adds the INTO statement. This will create a new table containing the
     * values selected in the query.
     *
     * @param table the table-name
     * @return fluent interface
     */
    public SelectBuilder into(String table);

    /**
     * Adds the IN statement to specify in which database a table is. Use after
     * INTO e.g. into("backup_table").in("Backup_database")
     *
     * @param database
     * @return fluent interface
     */
    public SelectBuilder in(String database);

    /**
     * Adds the tables to select from.
     *
     * @param tables
     * @return fluent interface
     */
    public SelectBuilder from(String... tables);

    /**
     * Adds the distinct statement
     *
     * @return fluent interface
     */
    public SelectBuilder distinct();

    /**
     * Creates a UNION of two SELECT statements
     *
     * @param all if false returns distinct values else all
     * @return fluent interface
     */
    public SelectBuilder union(boolean all);

    /**
     * Creates a LEFT JOIN table ON table.key = otherTable.otherKey statement
     *
     * @param table the table to join with
     * @param key the key to join on
     * @param otherTable the other table to join on
     * @param otherKey the other key to join on
     * @return fluent interface
     */
    public SelectBuilder leftJoinOnEqual(String table, String key, String otherTable, String otherKey);

    /**
     * Creates a RIGHT JOIN table ON table.key = otherTable.otherKey statement
     *
     * @param table the table to join with
     * @param key the key to join on
     * @param otherTable the other table to join on
     * @param otherKey the other key to join on
     * @return fluent interface
     */
    public SelectBuilder rightJoinOnEqual(String table, String key, String otherTable, String otherKey);

    /**
     * Creates a JOIN table ON table.key = otherTable.otherKey statement
     *
     * @param table the table to join with
     * @param key the key to join on
     * @param otherTable the other table to join on
     * @param otherKey the other key to join on
     * @return fluent interface
     */
    public SelectBuilder joinOnEqual(String table, String key, String otherTable, String otherKey);
}
