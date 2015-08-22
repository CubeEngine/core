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

import java.util.HashMap;
import java.util.Map;
import org.cubeengine.module.core.util.ChatFormat;
import de.cubeisland.engine.reflect.codec.yaml.ReflectedYaml;
import org.spongepowered.api.text.format.TextFormat;

import static org.cubeengine.service.i18n.formatter.MessageType.*;

@SuppressWarnings("all")
public class ColorConfiguration extends ReflectedYaml
{
    public Map<TextFormat, ChatFormat> colorMap = new HashMap<TextFormat, ChatFormat>()
    {
        {
            this.put(POSITIVE, ChatFormat.BRIGHT_GREEN);
            this.put(NEUTRAL, ChatFormat.YELLOW);
            this.put(NEGATIVE, ChatFormat.RED);
            this.put(CRITICAL, ChatFormat.DARK_RED);
            this.put(NONE, null);
        }
    };

    public Map<ChatFormat, ChatFormat> colorRemap = new HashMap<>();
}
