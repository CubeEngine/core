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

public interface InsertBuilder extends ComponentBuilder<InsertBuilder>
{
    /**
     * Adds where to insert.
     *
     * @param table the table to insert into
     * @return fluent interface
     */
    public InsertBuilder into(String table);

    /**
     * Adds which cols to insert into and values to be inserted later.
     *
     * @param cols the columns
     * @return fluent interface
     */
    public InsertBuilder cols(String... cols);

    /**
     * Signals to insert into all col.
     * Don't forget to call values(...) with the correct amount of cols
     *
     * @return fluent interface
     */
    public InsertBuilder allCols();

    /**
     * Adds the VALUES statement with the amount of values
     *
     * @param amount the amount of values
     * @return fluent interface
     */
    public InsertBuilder values(int amount);
}
