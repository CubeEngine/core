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

public interface AlterTableBuilder extends ComponentBuilder<AlterTableBuilder>
{
    /**
     * Alter the table.
     *
     * @param table
     * @return fluent interface
     */
    public AlterTableBuilder alterTable(String table);

    /**
     * Add a field to the table
     *
     * @param field
     * @param type
     * @return fluent interface
     */
    public AlterTableBuilder add(String field, AttrType type);

    /**
     * Adds a UNIQUE CONSTRAINT for given fields
     *
     * @param fields the unique fields
     * @return
     */
    public AlterTableBuilder addUniques(String... fields);

    /**
     * Starts a CHECK CONSTRAINT
     *
     * @return
     */
    public AlterTableBuilder addCheck();

    /**
     * Starts a DEFAULT statements
     *
     * @param field
     * @return
     */
    public AlterTableBuilder setDefault(String field);

    /**
     * Adds a FOREIGN KEY
     *
     * @param field
     * @param foreignTable
     * @param foreignField
     * @return
     */
    public AlterTableBuilder addForeignKey(String field, String foreignTable, String foreignField);

    /**
     * Sets the PRIMARY KEY
     *
     * @param field
     * @return
     */
    public AlterTableBuilder setPrimary(String field);

    /**
     * Drops a UNIQUE field
     *
     * @param field
     * @return
     */
    public AlterTableBuilder dropUnique(String field);

    /**
     * Drops a PRIMARY KEY
     *
     * @return
     */
    public AlterTableBuilder dropPrimary();

    /**
     * Drops a CHECK
     *
     * @param field
     * @return
     */
    public AlterTableBuilder dropCheck(String field);

    /**
     * Drops a DEFAULT value
     *
     * @param field
     * @return
     */
    public AlterTableBuilder dropDefault(String field);

    /**
     * Drops an INDEX or KEY
     *
     * @param field
     * @return
     */
    public AlterTableBuilder dropIndex(String field);

    /**
     * Drops a FOREIGN KEY
     *
     * @param field
     * @return
     */
    public AlterTableBuilder dropForeignKey(String field);

    /**
     * Sets the default value
     *
     * @param value
     * @return
     */
    public AlterTableBuilder defaultValue(String value);

    /**
     * Sets a placeholder ? for the default value
     *
     * @return
     */
    public AlterTableBuilder defaultValue();

    /**
     * Drop a field from the table
     *
     * @param field
     * @return fluent interface
     */
    public AlterTableBuilder drop(String field);

    /**
     * Modify a field from the table
     *
     * @param field
     * @param type
     * @return fluent interface
     */
    public AlterTableBuilder modify(String field, AttrType type);

    /**
     * Changed a field from the table
     *
     * @param field the current Name
     * @param newName the new Name
     * @param type the new type (ignored if null)
     * @return fluent interface
     */
    public AlterTableBuilder change(String field, String newName, AttrType type);
}
