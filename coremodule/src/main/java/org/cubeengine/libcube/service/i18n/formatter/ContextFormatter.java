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
package org.cubeengine.libcube.service.i18n.formatter;

import org.cubeengine.dirigent.Component;
import org.cubeengine.dirigent.formatter.AbstractFormatter;
import org.cubeengine.dirigent.parser.component.Text;
import org.spongepowered.api.service.context.Context;

public class ContextFormatter extends AbstractFormatter<Context>
{
    public ContextFormatter()
    {
        super("context");
    }

    @Override
    public Component format(Context object, org.cubeengine.dirigent.formatter.Context context)
    {
        return new Text(object.getValue().isEmpty() ? object.getKey() : object.getKey() + "|" + object.getValue());
    }
}
