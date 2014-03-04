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
    /**
     * All actions with a possible player involved
     */
    public static final ActionTypeCategory PLAYER = new ActionTypeCategory("player");
    /**
     * All actions with a block involved
     */
    public static final ActionTypeCategory BLOCK = new ActionTypeCategory("block");
    /**
     * All actions with ItemStacks involved
     */
    public static final ActionTypeCategory ITEM = new ActionTypeCategory("item");
    /**
     * All actions with inventories involved
     */
    public static final ActionTypeCategory INVENTORY = new ActionTypeCategory("inventory");
    /**
     * All actions with entities excluding block changes by an entity
     */
    public static final ActionTypeCategory ENTITY = new ActionTypeCategory("entity");
    /**
     * All block actions with a possible living entity as causer
     */
    public static final ActionTypeCategory BLOCK_ENTITY = new ActionTypeCategory("block-entity");
    /**
     * possibly environmental actions such as grass growing naturally etc.
     */
    public static final ActionTypeCategory ENVIRONEMENT = new ActionTypeCategory("environement");
    /**
     * All actions of the death of an living entity or player
     */
    public static final ActionTypeCategory KILL = new ActionTypeCategory("kill");
    /**
     * All block-actions involving an explosion (tnt-prime too)
     */
    public static final ActionTypeCategory EXPLOSION = new ActionTypeCategory("explosion");
    /**
     * All actions involving lava or water flows
     */
    public static final ActionTypeCategory FLOW = new ActionTypeCategory("flow");
    /**
     * All actions involving fire ignition/spread
     */
    public static final ActionTypeCategory IGNITE = new ActionTypeCategory("ignite");
    /**
     * All actions involving fire excluding blocks indirectly broken by block-burns
     */
    public static final ActionTypeCategory FIRE = new ActionTypeCategory("fire");
    /**
     * All actions involving buckets
     */
    public static final ActionTypeCategory BUCKET = new ActionTypeCategory("bucket");
    /**
     * lava-bucket AND water-bucket
     */
    public static final ActionTypeCategory BUCKET_EMPTY = new ActionTypeCategory("bucket-empty");
    /**
     * All actions involving vehicles
     */
    public static final ActionTypeCategory VEHICLE = new ActionTypeCategory("vehicle");
    /**
     * All actions involving spawning
     */
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
