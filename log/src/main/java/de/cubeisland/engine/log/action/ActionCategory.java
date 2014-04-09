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
package de.cubeisland.engine.log.action;

import java.util.HashSet;
import java.util.Set;

public class ActionCategory
{
    public static final ActionCategory EXPLODE = new ActionCategory("EXPLODE");
    public static final ActionCategory ENTITY_ENDERMAN = new ActionCategory("ENDERMAN");
    public static final ActionCategory ENTITY = new ActionCategory("ENTITY");
    public static final ActionCategory LAVA = new ActionCategory("LAVA");
    public static final ActionCategory WATER = new ActionCategory("WATER");
    public static final ActionCategory IGNITE = new ActionCategory("IGNITE");
    public static final ActionCategory BUCKET = new ActionCategory("BUCKET");
    public static final ActionCategory BLOCK = new ActionCategory("BLOCK");
    public static final ActionCategory USE = new ActionCategory("USE");
    public static final ActionCategory SIGN = new ActionCategory("SIGN");
    public static final ActionCategory DEATH = new ActionCategory("DEATH");
    public static final ActionCategory ITEM = new ActionCategory("ITEM");
    public static final ActionCategory SPAWN = new ActionCategory("SPAWN");
    public static final ActionCategory ENTITY_HANGING = new ActionCategory("HANGING");
    public static final ActionCategory PLAYER = new ActionCategory("PLAYER");
    public static final ActionCategory VEHICLE = new ActionCategory("VEHICLE");

    public final String name;
    private ActionCategory(String name)
    {
        this.name = name;
    }

    private Set<Class<? extends BaseAction>> actions = new HashSet<>();

    public void addAction(Class<? extends BaseAction> action)
    {
        this.actions.add(action);
    }

    public Set<Class<? extends BaseAction>> getActions()
    {
        return actions;
    }
}
