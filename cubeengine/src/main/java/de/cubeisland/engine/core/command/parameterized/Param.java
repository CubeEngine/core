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
package de.cubeisland.engine.core.command.parameterized;

import de.cubeisland.engine.core.permission.PermDefault;

import static de.cubeisland.engine.core.permission.PermDefault.OP;

public @interface Param
{
    String[] names();

    String label() default "";

    Class type() default String.class;

    boolean required() default false;

    Class<? extends Completer> completer() default Completer.class;

    String permission() default "";

    PermDefault permDefault() default OP;
}
