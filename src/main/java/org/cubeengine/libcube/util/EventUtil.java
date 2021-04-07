/*
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
package org.cubeengine.libcube.util;

import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;

public final class EventUtil
{
    public static boolean isMainHand(EventContext context)
    {
        return context.get(EventContextKeys.USED_HAND).map(hand -> hand.equals(HandTypes.MAIN_HAND.get())).orElse(false);
    }

    private EventUtil()
    {
    }
}
