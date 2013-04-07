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

import de.cubeisland.cubeengine.core.storage.database.AttrType;

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

    public AlterTableBuilder addUniques(String... fields);

    public AlterTableBuilder addCheck();

    public AlterTableBuilder setDefault(String field);

    public AlterTableBuilder addForeignKey(String field, String foreignTable, String foreignField);

    public AlterTableBuilder setPrimary(String field);

    public AlterTableBuilder dropUnique(String field);

    public AlterTableBuilder dropPrimary();

    public AlterTableBuilder dropCheck(String field);

    public AlterTableBuilder dropDefault(String field);

    public AlterTableBuilder dropIndex(String field);

    public AlterTableBuilder dropForeignKey(String field);

    public AlterTableBuilder defaultValue(String value);

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
}
