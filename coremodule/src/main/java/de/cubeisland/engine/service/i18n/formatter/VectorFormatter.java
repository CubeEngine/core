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
package de.cubeisland.engine.service.i18n.formatter;

import de.cubeisland.engine.messagecompositor.parser.component.ChainedComponent;
import de.cubeisland.engine.messagecompositor.parser.component.MessageComponent;
import de.cubeisland.engine.messagecompositor.parser.formatter.Context;
import de.cubeisland.engine.messagecompositor.parser.formatter.reflected.Format;
import de.cubeisland.engine.messagecompositor.parser.formatter.reflected.Names;
import de.cubeisland.engine.messagecompositor.parser.formatter.reflected.ReflectedFormatter;
import de.cubeisland.engine.module.core.util.math.BlockVector2;
import de.cubeisland.engine.module.core.util.math.BlockVector3;
import de.cubeisland.engine.service.i18n.StyledComponent;

import static org.spongepowered.api.text.format.TextColors.*;

@Names("vector")
public class VectorFormatter extends ReflectedFormatter
{
    @Format
    public MessageComponent format(BlockVector2 v, Context context)
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
    public MessageComponent format(BlockVector3 v, Context context)
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

}
