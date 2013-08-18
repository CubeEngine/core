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
package de.cubeisland.engine.baumguard.storage;

import java.util.Arrays;
import java.util.Collection;

import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;

import static de.cubeisland.engine.baumguard.storage.GuardType.PRIVATE;
import static de.cubeisland.engine.baumguard.storage.GuardType.PUBLIC;

public enum ProtectedType
{
    CONTAINER(1), // supports ALL
    DOOR(2, PRIVATE, PUBLIC),
    BLOCK(3, PRIVATE, PUBLIC),
    ENTITY_CONTAINER(4),
    ENTITY_LIVING(5, PRIVATE, PUBLIC),
    ENTITY_VEHICLE(6, PRIVATE, PUBLIC),
    ENTITY(7, PRIVATE, PUBLIC);

    public final byte id;

    private static TByteObjectMap<ProtectedType> protectedTypes = new TByteObjectHashMap<>();
    public final Collection<GuardType> supportedTypes;

    private ProtectedType(int id, GuardType... supportedTypes)
    {
        this.supportedTypes = Arrays.asList(supportedTypes);
        this.id = (byte)id;
    }

    private ProtectedType(int id)
    {
        this(id, GuardType.values());
    }

    static
    {
        for (ProtectedType protectedType : ProtectedType.values())
        {
            protectedTypes.put(protectedType.id, protectedType);
        }
    }

    public static ProtectedType forByte(Byte protectedType)
    {
        return protectedTypes.get(protectedType);
    }
}
