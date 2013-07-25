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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cubeisland.engine.core.storage.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a field as a ForeignKey for the database.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Index
{
    public IndexType value();

    public String[] fields();

    /**
     * Needed for FOREIGN_KEY
     */
    public String f_table() default "";

    /**
     * Needed for FOREIGN_KEY
     */
    public String[] f_field() default {};

    /**
     * Needed for FOREIGN_KEY.
     * default: CASCADE. 
     * other: SET NULL; RESTRICT; NO ACTION
     *
     * @return the delete action
     */
    public String onDelete() default "CASCADE";

    public static enum IndexType
    {
        FOREIGN_KEY,
        UNIQUE,
        INDEX
    }
}
