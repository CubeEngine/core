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
package org.cubeengine.libcube.service.i18n.formatter;

import static java.util.Arrays.asList;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

import org.cubeengine.dirigent.context.Arguments;
import org.cubeengine.dirigent.formatter.reflected.Format;
import org.cubeengine.dirigent.formatter.reflected.Names;
import org.cubeengine.dirigent.formatter.reflected.ReflectedFormatter;
import org.cubeengine.dirigent.parser.component.Component;
import org.cubeengine.dirigent.parser.component.ComponentGroup;
import org.cubeengine.libcube.service.i18n.formatter.component.StyledComponent;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

@Names("vector")
public class VectorFormatter extends ReflectedFormatter
{
    @Format
    public Component format(Vector2i v, Arguments args)
    {
        String arg0 = args.get(0);
        String arg1 = args.get(1);
        if (arg0 != null && arg1 != null)
        {
            return new ComponentGroup(asList(new StyledComponent(DARK_AQUA, "["),
                                        new StyledComponent(WHITE, arg0),
                                        new StyledComponent(GOLD, String.valueOf(v.x())),
                                        new StyledComponent(DARK_AQUA, ","),
                                        new StyledComponent(WHITE, arg1),
                                        new StyledComponent(GOLD, String.valueOf(v.y())),
                                        new StyledComponent(DARK_AQUA, "]")));
        }
        return new ComponentGroup(asList(new StyledComponent(DARK_AQUA, "["),
                                    new StyledComponent(GOLD, String.valueOf(v.x())),
                                    new StyledComponent(DARK_AQUA, ","),
                                    new StyledComponent(GOLD, String.valueOf(v.y())),
                                    new StyledComponent(DARK_AQUA, "]")));
    }

    @Format
    public Component format(Vector3i v, Arguments args)
    {
        String arg0 = args.get(0);
        String arg1 = args.get(1);
        String arg2 = args.get(2);
        if (arg0 != null && arg1 != null && arg2 != null)
        {
            return new ComponentGroup(asList(new StyledComponent(DARK_AQUA, "["),
                                        new StyledComponent(WHITE, arg0),
                                        new StyledComponent(GOLD, String.valueOf(v.x())),
                                        new StyledComponent(DARK_AQUA, ","),
                                        new StyledComponent(WHITE, arg1),
                                        new StyledComponent(GOLD, String.valueOf(v.y())),
                                        new StyledComponent(DARK_AQUA, ","),
                                        new StyledComponent(WHITE, arg2),
                                        new StyledComponent(GOLD, String.valueOf(v.z())),
                                        new StyledComponent(DARK_AQUA, "]")));
        }
        return new ComponentGroup(asList(new StyledComponent(DARK_AQUA, "["),
                                    new StyledComponent(GOLD, String.valueOf(v.x())),
                                    new StyledComponent(DARK_AQUA, ","),
                                    new StyledComponent(GOLD, String.valueOf(v.y())),
                                    new StyledComponent(DARK_AQUA, ","),
                                    new StyledComponent(GOLD, String.valueOf(v.z())),
                                    new StyledComponent(DARK_AQUA, "]")));
    }

    @Format
    public Component format(Vector3d v, Arguments args)
    {
        String arg0 = args.get(0);
        String arg1 = args.get(1);
        String arg2 = args.get(2);
        if (arg0 != null && arg1 != null && arg2 != null)
        {
            return new ComponentGroup(asList(new StyledComponent(DARK_AQUA, "["),
                                        new StyledComponent(WHITE, arg0),
                                        new StyledComponent(GOLD, String.valueOf(v.floorX())),
                                        new StyledComponent(DARK_AQUA, ","),
                                        new StyledComponent(WHITE, arg1),
                                        new StyledComponent(GOLD, String.valueOf(v.floorY())),
                                        new StyledComponent(DARK_AQUA, ","),
                                        new StyledComponent(WHITE, arg2),
                                        new StyledComponent(GOLD, String.valueOf(v.floorZ())),
                                        new StyledComponent(DARK_AQUA, "]")));
        }
        return new ComponentGroup(asList(new StyledComponent(DARK_AQUA, "["),
                                    new StyledComponent(GOLD, String.valueOf(v.floorX())),
                                    new StyledComponent(DARK_AQUA, ","),
                                    new StyledComponent(GOLD, String.valueOf(v.floorY())),
                                    new StyledComponent(DARK_AQUA, ","),
                                    new StyledComponent(GOLD, String.valueOf(v.floorZ())),
                                    new StyledComponent(DARK_AQUA, "]")));
    }

}
