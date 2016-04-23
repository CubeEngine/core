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
package org.cubeengine.service.i18n.formatter;

import com.flowpowered.math.vector.Vector3d;
import org.cubeengine.dirigent.Component;
import org.cubeengine.dirigent.formatter.Context;
import org.cubeengine.dirigent.formatter.reflected.Format;
import org.cubeengine.dirigent.formatter.reflected.Names;
import org.cubeengine.dirigent.formatter.reflected.ReflectedFormatter;
import org.cubeengine.dirigent.parser.component.ChainedComponent;
import org.cubeengine.module.core.util.math.BlockVector2;
import org.cubeengine.module.core.util.math.BlockVector3;
import org.cubeengine.service.i18n.formatter.component.StyledComponent;

import static org.spongepowered.api.text.format.TextColors.*;

@Names("vector")
public class VectorFormatter extends ReflectedFormatter
{
    @Format
    public Component format(BlockVector2 v, Context context)
    {
        String arg0 = context.getFlag(0);
        String arg1 = context.getFlag(1);
        if (arg0 != null && arg1 != null)
        {
            return new ChainedComponent(new StyledComponent(DARK_AQUA, "["),
                                        new StyledComponent(WHITE, arg0),
                                        new StyledComponent(GOLD, String.valueOf(v.x)),
                                        new StyledComponent(DARK_AQUA, ","),
                                        new StyledComponent(WHITE, arg1),
                                        new StyledComponent(GOLD, String.valueOf(v.z)),
                                        new StyledComponent(DARK_AQUA, "]"));
        }
        return new ChainedComponent(new StyledComponent(DARK_AQUA, "["),
                                    new StyledComponent(GOLD, String.valueOf(v.x)),
                                    new StyledComponent(DARK_AQUA, ","),
                                    new StyledComponent(GOLD, String.valueOf(v.z)),
                                    new StyledComponent(DARK_AQUA, "]"));
    }

    @Format
    public Component format(BlockVector3 v, Context context)
    {
        String arg0 = context.getFlag(0);
        String arg1 = context.getFlag(1);
        String arg2 = context.getFlag(2);
        if (arg0 != null && arg1 != null && arg2 != null)
        {
            return new ChainedComponent(new StyledComponent(DARK_AQUA, "["),
                                        new StyledComponent(WHITE, arg0),
                                        new StyledComponent(GOLD, String.valueOf(v.x)),
                                        new StyledComponent(DARK_AQUA, ","),
                                        new StyledComponent(WHITE, arg1),
                                        new StyledComponent(GOLD, String.valueOf(v.y)),
                                        new StyledComponent(DARK_AQUA, ","),
                                        new StyledComponent(WHITE, arg2),
                                        new StyledComponent(GOLD, String.valueOf(v.z)),
                                        new StyledComponent(DARK_AQUA, "]"));
        }
        return new ChainedComponent(new StyledComponent(DARK_AQUA, "["),
                                    new StyledComponent(GOLD, String.valueOf(v.x)),
                                    new StyledComponent(DARK_AQUA, ","),
                                    new StyledComponent(GOLD, String.valueOf(v.y)),
                                    new StyledComponent(DARK_AQUA, ","),
                                    new StyledComponent(GOLD, String.valueOf(v.z)),
                                    new StyledComponent(DARK_AQUA, "]"));
    }

    @Format
    public Component format(Vector3d v, Context context)
    {
        String arg0 = context.getFlag(0);
        String arg1 = context.getFlag(1);
        String arg2 = context.getFlag(2);
        if (arg0 != null && arg1 != null && arg2 != null)
        {
            return new ChainedComponent(new StyledComponent(DARK_AQUA, "["),
                                        new StyledComponent(WHITE, arg0),
                                        new StyledComponent(GOLD, String.valueOf(v.getFloorX())),
                                        new StyledComponent(DARK_AQUA, ","),
                                        new StyledComponent(WHITE, arg1),
                                        new StyledComponent(GOLD, String.valueOf(v.getFloorY())),
                                        new StyledComponent(DARK_AQUA, ","),
                                        new StyledComponent(WHITE, arg2),
                                        new StyledComponent(GOLD, String.valueOf(v.getFloorZ())),
                                        new StyledComponent(DARK_AQUA, "]"));
        }
        return new ChainedComponent(new StyledComponent(DARK_AQUA, "["),
                                    new StyledComponent(GOLD, String.valueOf(v.getFloorX())),
                                    new StyledComponent(DARK_AQUA, ","),
                                    new StyledComponent(GOLD, String.valueOf(v.getFloorY())),
                                    new StyledComponent(DARK_AQUA, ","),
                                    new StyledComponent(GOLD, String.valueOf(v.getFloorZ())),
                                    new StyledComponent(DARK_AQUA, "]"));
    }

}
