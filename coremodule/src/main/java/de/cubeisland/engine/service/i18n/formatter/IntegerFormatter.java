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

import de.cubeisland.engine.messagecompositor.parser.component.MessageComponent;
import de.cubeisland.engine.messagecompositor.parser.formatter.Context;
import de.cubeisland.engine.messagecompositor.parser.formatter.reflected.Format;
import de.cubeisland.engine.messagecompositor.parser.formatter.reflected.Names;
import de.cubeisland.engine.messagecompositor.parser.formatter.reflected.ReflectedFormatter;
import de.cubeisland.engine.service.i18n.StyledComponent;
import org.spongepowered.api.text.format.TextColors;

@Names({"amount", "integer", "long", "short"})
public class IntegerFormatter extends ReflectedFormatter
{
    @Format
    public MessageComponent format(Integer i, Context context)
    {
        return new StyledComponent(TextColors.GOLD, String.valueOf(i));
    }

    @Format
    public MessageComponent format(Long l, Context context)
    {
        return new StyledComponent(TextColors.GOLD, String.valueOf(l));
    }

    @Format
    public MessageComponent format(Short s, Context context)
    {
        return new StyledComponent(TextColors.GOLD, String.valueOf(s));
    }
}
