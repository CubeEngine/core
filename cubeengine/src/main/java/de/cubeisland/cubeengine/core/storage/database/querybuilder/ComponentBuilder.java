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

public interface ComponentBuilder<This extends ComponentBuilder>
{
    public static final int EQUAL = 1;
    public static final int NOT_EQUAL = 2;
    public static final int LESS = 3;
    public static final int LESS_OR_EQUAL = 4;
    public static final int GREATER = 5;
    public static final int GREATER_OR_EQUAL = 6;

    /**
     * Adds a function.
     *
     * @param function the name of the function
     * @return fluent interface
     */
    public This function(String function);

    /**
     * Begins a function.
     *
     * @param function the name of the function
     * @return fluent interface
     */
    public This beginFunction(String function);

    /**
     * Ends the current function.
     *
     * @return fluent interface
     */
    public This endFunction();

    /**
     * Adds a quoted field.
     *
     * @param name the name of the field
     * @return fluent interface
     */
    public This field(String name);

    /**
     * Adds a value.
     *
     * @param value the value to insert
     * @return fluent interface
     */
    public This value(Object value);

    /**
     * Adds the VALUE Statement and multiple variables which can be later
     * replaced by values.
     *
     * @param amount the amount of values
     * @return fluent interface
     */
    public This values(int amount);

    /**
     * Adds multiple variables in brackets which can be later replaced by values.
     *
     * @param amount the amount of values
     * @return fluent interface
     */
    public This valuesInBrackets(int amount);

    /**
     * Adds multiple values in brackets separated by comma
     * 
     * @param values the values
     * @return fluent interface 
     */
    public This valuesInBrackets(Object[] values);

    /**
     * Adds a variable which can be later replaced by a value.
     *
     * @return fluent interface
     */
    public This value();

    /**
     * Adds an operation
     *
     * @param operation the operation type
     * @return fluent interface
     */
    public This is(int operation);

    /**
     * Adds an equal-operation
     *
     * @return fluent interface
     */
    public This isEqual();

    /**
     * Adds a wildcard.
     *
     * @return fluent interface
     */
    public This wildcard();

    /**
     * Adds NOT.
     *
     * @return fluent interface
     */
    public This not();

    /**
     * Adds AND.
     *
     * @return fluent interface
     */
    public This and();

    /**
     * Adds OR.
     *
     * @return fluent interface
     */
    public This or();

    /**
     * Adds AS
     *
     * @param field the alias
     * @return fluent interface
     */
    public This as(String field);

    /**
     * Adds grouping by given fields
     *
     * @param fields the fields to group by
     * @return fluent interface
     */
    public This groupBy(String... fields);

    /**
     * Adds HAVING.
     *
     * @return fluent interface
     */
    public This having();

    /**
     * Adds LIKE statement. Don't forget to add a value after this!
     *
     * @return fluent interface
     */
    public This like();

    /**
     * Adds IN statement. Don't forget to add a value after this!
     *
     * @return fluent interface
     */
    public This in();

    /**
     * Adds (
     *
     * @return fluent interface
     */
    public This beginSub();

    /**
     * Adds )
     *
     * @return fluent interface
     */
    public This endSub();

    /**
     * Ends the query part and returns the QueryBuilder
     *
     * @return the QueryBuilder
     */
    public QueryBuilder end();

    /**
     * Adds fields in brackets separated by ,
     * e.g.: (`field1`, `field2`, `field3`)
     *
     * @param fields
     * @return
     */
    public This fieldsInBrackets(String[] fields);

    /**
     * Adds fields separated by ,
     * e.g.: `field1`, `field2`, `field3`
     *
     * @param fields
     * @return
     */
    public This fields(String... fields);
}
