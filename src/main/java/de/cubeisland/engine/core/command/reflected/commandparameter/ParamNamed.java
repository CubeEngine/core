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
package de.cubeisland.engine.core.command.reflected.commandparameter;

/**
 * Annotates a named Parameter.
 * <p>Named Parameters are by default optional use {@link Required} to make it required
 */
public @interface ParamNamed
{
    /**
     * By default the parameters name will be fields name but can be overridden here
     *
     * @return the parameters name
     */
    String value() default "";

    /**
     * Optional possible parameter names
     *
     * @return the aliases
     */
    String[] alias() default {};
}
