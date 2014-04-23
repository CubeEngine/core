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
package de.cubeisland.engine.core.command.reflected;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.permission.PermDefault;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command
{
    String[] names() default {};

    String desc();

    boolean checkPerm() default true;

    /**
     * Use this permission node instead of the automatically generated one.
     * 'cubeengine.<module>.command' will be prepended to this.
     *
     * @return
     */
    String permNode() default "";

    PermDefault permDefault() default PermDefault.OP;

    Flag[] flags() default {};

    Param[] params() default {};

    Grouped[] indexed() default {};

    boolean loggable() default true;

    /**
     * If set to true the annotated reflected command will be called asynchronously
     *
     * @return true if the command will be called asynchronously
     */
    boolean async() default false;
}
