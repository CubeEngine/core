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

public interface TableBuilder extends ComponentBuilder<TableBuilder>
{
    /**
     * Starts accepting fields.
     *
     * @return fluent interface
     */
    public TableBuilder beginFields();

    /**
     * Adds a field.
     *
     * @param name the name of the field
     * @param type the type of the field
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type);

    /**
     * Adds a field.
     *
     * @param name the name
     * @param type the type
     * @param unsigned whether the field is unsigned
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, boolean unsigned);

    /**
     * Adds a field.
     *
     * @param name the name
     * @param type the type
     * @param unsigned whether the field is unsigned
     * @param notnull whether the type may be null
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, boolean unsigned, boolean notnull);

    /**
     * Adds a field.
     *
     * @param name the name
     * @param type the type
     * @param length the length of the type
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, int length);

    /**
     * Adds a field.
     *
     * @param name the name
     * @param type the type
     * @param length the length of the type
     * @param notnull whether the type may be null
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, int length, boolean notnull);

    /**
     * Adds a field.
     *
     * @param name the name
     * @param type the type
     * @param length the length of the type
     * @param notnull whether the field is unsigned
     * @param unsigned whether the type may be null
     * @return fluent interface
     */
    public TableBuilder field(String name, AttrType type, boolean unsigned, int length, boolean notnull);

    /**
     * Adds a field.
     * 
     * @param name the name
     * @param enumValues the enum values
     * @param notnull whether the field may be null
     * @return fluent interface
     */
    public TableBuilder enumField(String name, String[] enumValues, boolean notnull);

    /**
     * Adds a DEFAULT value
     *
     * @return fluent interface
     */
    public TableBuilder defaultValue(String sql);

    /**
     * Adds AUTO_INCREMENT to the current field
     *
     * @return fluent interface
     */
    public TableBuilder autoIncrement();

    /**
     * Adds an INDEX to given fields
     *
     * @return fluent interface
     */
    public TableBuilder index(String... fields);

    /**
     * Sets the primary Key.
     *
     * @param fields the fields to use as the primary key
     * @return fluent interface
     */
    public TableBuilder primaryKey(String... fields);

    /**
     * Sets given fields to be unique
     *
     * @param fields the fields to make unique
     * @return fluent interface
     */
    public TableBuilder unique(String... fields);

    /**
     * Starts a CHECK statement. Don't forget to use beginSub and endSub.
     *
     * @return fluent interface
     */
    public TableBuilder check();

    /**
     * Sets a foreign key.
     *
     * @param keys the name
     * @return fluent interface
     */
    public TableBuilder foreignKey(String... keys);

    /**
     * Sets the reference for the foreign key
     *
     * @param table the table to reference
     * @param fields the field of the references table
     * @return fluent interface
     */
    public TableBuilder references(String table, String... fields);

    /**
     * Sets what should be done when trying to remove a key.
     *
     * @param doThis what to do
     * @return fluent interface
     */
    public TableBuilder onDelete(String doThis);

    /**
     * Finish accepting the fields
     *
     * @return fluent interface
     */
    public TableBuilder endFields();

    /**
     * Sets the engine
     *
     * @param engine the name of the engine
     * @return fluent interface
     */
    public TableBuilder engine(String engine);

    /**
     * Sets the default Charset
     *
     * @param charset the charset
     * @return fluent interface
     */
    public TableBuilder defaultcharset(String charset);

    /**
     * Sets the autoincrement.
     *
     * @param n the auto-increment start value
     * @return fluent interface
     */
    public TableBuilder autoIncrement(int n);
}
