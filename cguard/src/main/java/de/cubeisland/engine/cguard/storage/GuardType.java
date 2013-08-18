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
package de.cubeisland.engine.cguard.storage;

import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;

public enum GuardType
{
    PRIVATE(1),
    PUBLIC(2),
    GUARDED(3),
    DONATION(4),
    FREE(5);

    public final byte id;

    private static TByteObjectMap<GuardType> guardTypes = new TByteObjectHashMap<>();

    static
    {
        for (GuardType guardType : GuardType.values())
        {
            guardTypes.put(guardType.id, guardType);
        }
    }

    private GuardType(int id)
    {
        this.id = (byte)id;
    }

    public static GuardType forByte(Byte guardType)
    {
        return guardTypes.get(guardType);
    }
}
