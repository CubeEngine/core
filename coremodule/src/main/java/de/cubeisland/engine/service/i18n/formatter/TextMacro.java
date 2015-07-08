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

import java.util.Collections;
import java.util.Set;
import de.cubeisland.engine.messagecompositor.parser.component.MessageComponent;
import de.cubeisland.engine.messagecompositor.parser.component.Text;
import de.cubeisland.engine.messagecompositor.parser.formatter.ConstantFormatter;
import de.cubeisland.engine.messagecompositor.parser.formatter.Context;

public class TextMacro extends ConstantFormatter
{
    private final Set<String> names= Collections.singleton("text");// new HashSet<>(Arrays.asList("text"));

    @Override
    public MessageComponent format(Context context)
    {
        return new Text(context.getFlag(0));
    }

    @Override
    public Set<String> names()
    {
        return this.names;
    }
}
