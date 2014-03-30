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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ActionTypeCategory
{
    public static final ActionTypeCategory ALL = new ActionTypeCategory("all");
    public static final ActionTypeCategory PLAYER = new ActionTypeCategory("player");
    public static final ActionTypeCategory BLOCK = new ActionTypeCategory("block");
    public static final ActionTypeCategory ITEM = new ActionTypeCategory("item");
    public static final ActionTypeCategory INVENTORY = new ActionTypeCategory("inventory");
    public static final ActionTypeCategory ENTITY = new ActionTypeCategory("entity");
    public static final ActionTypeCategory BLOCK_ENTITY = new ActionTypeCategory("block-entity");
    public static final ActionTypeCategory ENVIRONEMENT = new ActionTypeCategory("environement");
    public static final ActionTypeCategory KILL = new ActionTypeCategory("kill");
    public static final ActionTypeCategory EXPLOSION = new ActionTypeCategory("explosion");
    public static final ActionTypeCategory FLOW = new ActionTypeCategory("flow");
    public static final ActionTypeCategory IGNITE = new ActionTypeCategory("ignite");
    public static final ActionTypeCategory FIRE = new ActionTypeCategory("fire");
    public static final ActionTypeCategory BUCKET = new ActionTypeCategory("bucket");
    public static final ActionTypeCategory BUCKET_EMPTY = new ActionTypeCategory("bucket-empty");
    public static final ActionTypeCategory VEHICLE = new ActionTypeCategory("vehicle");
    public static final ActionTypeCategory SPAWN = new ActionTypeCategory("spawn");

    private final HashSet<ActionType> actionTypes = new HashSet<>();

    public final String name;

    private ActionTypeCategory(String name)
    {
        this.name = name;
    }

    public void registerActionType(ActionType actionType)
    {
        this.actionTypes.add(actionType);
    }

    public Set<ActionType> getActionTypes()
    {
        return Collections.unmodifiableSet(actionTypes);
    }
}
