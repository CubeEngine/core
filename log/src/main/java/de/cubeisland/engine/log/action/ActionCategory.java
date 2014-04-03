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

public class ActionCategory
{
    public static final ActionCategory EXPLODE = new ActionCategory("explode");
    public static final ActionCategory ENTITY_ENDERMAN = new ActionCategory("enderman");
    public static final ActionCategory ENTITY = new ActionCategory("entity");
    public static final ActionCategory LAVA = new ActionCategory("lava");
    public static final ActionCategory WATER = new ActionCategory("water");
    public static final ActionCategory IGNITE = new ActionCategory("ignite");
    public static final ActionCategory BUCKET = new ActionCategory("bucket");
    public static final ActionCategory BLOCK = new ActionCategory("block");
    public static final ActionCategory USE = new ActionCategory("use");
    public static final ActionCategory SIGN = new ActionCategory("sign");
    public static final ActionCategory DEATH = new ActionCategory("death");
    public static final ActionCategory ITEM = new ActionCategory("item");
    public static final ActionCategory SPAWN = new ActionCategory("spawn");
    public static final ActionCategory ENTITY_HANGING = new ActionCategory("hanging");
    public static final ActionCategory PLAYER = new ActionCategory("player");

// TODO Container Category

    public static final ActionCategory ALL = new ActionCategory("all");
    public static final ActionCategory INVENTORY = new ActionCategory("inventory");
    public static final ActionCategory BLOCK_ENTITY = new ActionCategory("block-entity");
    public static final ActionCategory ENVIRONEMENT = new ActionCategory("environement");
    public static final ActionCategory KILL = new ActionCategory("kill");
    public static final ActionCategory FLOW = new ActionCategory("flow");
    public static final ActionCategory FIRE = new ActionCategory("fire");
    public static final ActionCategory BUCKET_EMPTY = new ActionCategory("bucket-empty");
    public static final ActionCategory VEHICLE = new ActionCategory("vehicle");

    public final String name;
    private ActionCategory(String name)
    {
        this.name = name;
    }

}
