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
package de.cubeisland.engine.locker.storage;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import de.cubeisland.engine.core.util.matcher.Match;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;

import static de.cubeisland.engine.locker.storage.ProtectionFlag.*;

public enum ProtectedType
{
    CONTAINER(1, HOPPER_IN, HOPPER_MINECART_IN, HOPPER_MINECART_OUT, HOPPER_OUT),
    DOOR(2, BLOCK_REDSTONE, AUTOCLOSE),
    BLOCK(3, BLOCK_REDSTONE),
    ENTITY_CONTAINER(4, HOPPER_IN, HOPPER_MINECART_IN, HOPPER_MINECART_OUT, HOPPER_OUT),
    ENTITY_LIVING(5),
    ENTITY_VEHICLE(6),
    ENTITY(7),
    ENTITY_CONTAINER_LIVING(8),
    ;

    private final static TByteObjectMap<ProtectedType> protectedTypes = new TByteObjectHashMap<>();

    public final byte id;
    public final Collection<ProtectionFlag> supportedFlags;

    static
    {
        for (ProtectedType protectedType : ProtectedType.values())
        {
            protectedTypes.put(protectedType.id, protectedType);
        }
    }

    private ProtectedType(int id, ProtectionFlag... supportedFlags)
    {
        this.supportedFlags = Arrays.asList(supportedFlags);
        this.id = (byte)id;
    }

    public static ProtectedType forByte(Byte protectedType)
    {
        return protectedTypes.get(protectedType);
    }


    public static ProtectedType getProtectedType(Material material)
    {
        switch (material)
        {
            case CHEST:
            case TRAPPED_CHEST:
            case DISPENSER:
            case DROPPER:
            case FURNACE:
            case BURNING_FURNACE:
            case BREWING_STAND:
            case BEACON:
            case HOPPER:
                return CONTAINER;
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case FENCE_GATE:
            case TRAP_DOOR:
                return DOOR;
        default:
            if (material.getId() < 256) return BLOCK;
        }
        throw new IllegalStateException("Material of block is an item!?");
    }

    public static ProtectedType getProtectedType(EntityType type)
    {
        switch (type)
        {
        case MINECART_CHEST:
        case MINECART_HOPPER:
            return ENTITY_CONTAINER;
        case HORSE:
            return ENTITY_CONTAINER_LIVING;
        case LEASH_HITCH:
        case PAINTING:
        case ITEM_FRAME:
        case MINECART_FURNACE:
        case MINECART_TNT:
        case MINECART_MOB_SPAWNER:
            return ENTITY;
        case BOAT:
        case MINECART:
            return ENTITY_VEHICLE;
        default:
            if (!Match.entity().isMonster(type) && type.isAlive())
            {
                return ENTITY_LIVING;
            }
            throw new IllegalArgumentException(type.name() + " is not allowed!");
        }
    }
}
