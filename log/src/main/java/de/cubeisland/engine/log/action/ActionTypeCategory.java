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

public class ActionTypeCategory
{
    public static final ActionTypeCategory EXPLODE = new ActionTypeCategory("explode");
    public static final ActionTypeCategory ENTITY_ENDERMAN = new ActionTypeCategory("enderman");
    public static final ActionTypeCategory ENTITY = new ActionTypeCategory("entity");
    public static final ActionTypeCategory ENTITY_SHEEP = new ActionTypeCategory("sheep");
    public static final ActionTypeCategory LAVA = new ActionTypeCategory("lava");
    public static final ActionTypeCategory WATER = new ActionTypeCategory("water");
    public static final ActionTypeCategory IGNITE = new ActionTypeCategory("ignite");
    public static final ActionTypeCategory BUCKET = new ActionTypeCategory("bucket");
    public static final ActionTypeCategory BLOCK = new ActionTypeCategory("block");
    public static final ActionTypeCategory USE = new ActionTypeCategory("use");
    public static final ActionTypeCategory SIGN = new ActionTypeCategory("sign");
    public static final ActionTypeCategory DEATH = new ActionTypeCategory("death");
    public static final ActionTypeCategory ITEM = new ActionTypeCategory("item");
    public static final ActionTypeCategory SPAWN = new ActionTypeCategory("spawn");
    public static final ActionTypeCategory ENTITY_HANGING = new ActionTypeCategory("hanging");
    public static final ActionTypeCategory PLAYER = new ActionTypeCategory("player");


    public static final ActionTypeCategory ALL = new ActionTypeCategory("all");



    public static final ActionTypeCategory INVENTORY = new ActionTypeCategory("inventory");

    public static final ActionTypeCategory BLOCK_ENTITY = new ActionTypeCategory("block-entity");
    public static final ActionTypeCategory ENVIRONEMENT = new ActionTypeCategory("environement");
    public static final ActionTypeCategory KILL = new ActionTypeCategory("kill");

    public static final ActionTypeCategory FLOW = new ActionTypeCategory("flow");

    public static final ActionTypeCategory FIRE = new ActionTypeCategory("fire");

    public static final ActionTypeCategory BUCKET_EMPTY = new ActionTypeCategory("bucket-empty");
    public static final ActionTypeCategory VEHICLE = new ActionTypeCategory("vehicle");



    public final String name;

    private ActionTypeCategory(String name)
    {
        this.name = name;
    }

}
