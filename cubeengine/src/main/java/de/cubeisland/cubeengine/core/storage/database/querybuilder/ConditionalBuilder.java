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

public interface ConditionalBuilder<This extends ConditionalBuilder> extends
    ComponentBuilder<This>
{
    /**
     * Adds ordering by column
     *
     * @param cols
     * @return fluent interface
     */
    public This orderBy(String... cols);

    /**
     * Limits the output to n
     *
     * @param n
     * @return fluent interface
     */
    public This limit(int n);

    /**
     * Adds LIMIT ?
     *
     * @return fluent interface
     */
    public This limit();

    /**
     * Sets the offset.
     *
     * @param n
     * @return fluent interface
     */
    public This offset(int n);

    /**
     * Adds OFFSET ?
     *
     * @return fluent interface
     */
    public This offset();

    /**
     * Starts a WHERE condition.
     *
     * @return fluent interface
     */
    public This where();

    /**
     * Adds a "BETWEEN ? AND ?" statement.
     *
     * @return fluent interface
     */
    public This between();

    /**
     * Adds a "BETWEEN val1 AND val2" statement.
     * 
     * @param val1 the first value
     * @param val2 the second value
     * @return fluent interface
     */
    public This between(Object val1, Object val2);

    /**
     * Adds ASCending keyword
     *
     * @return fluent interface
     */
    public This asc();

    /**
     * Adds DESCending keyword
     *
     * @return fluent interface
     */
    public This desc();
}
