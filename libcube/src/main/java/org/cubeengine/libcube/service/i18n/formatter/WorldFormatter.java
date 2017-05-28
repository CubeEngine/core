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

import org.cubeengine.dirigent.formatter.reflected.Format;
import org.cubeengine.dirigent.formatter.reflected.Names;
import org.cubeengine.dirigent.formatter.reflected.ReflectedFormatter;
import org.cubeengine.dirigent.parser.component.Component;
import org.cubeengine.libcube.service.i18n.formatter.component.HoverComponent;
import org.cubeengine.libcube.service.i18n.formatter.component.StyledComponent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import static org.spongepowered.api.text.format.TextColors.GOLD;
import static org.spongepowered.api.text.format.TextColors.YELLOW;

@Names("world")
public class WorldFormatter extends ReflectedFormatter
{
    @Format
    public Component format(World world)
    {
        return HoverComponent.hoverText(Text.of(YELLOW, world.getUniqueId()),
                    StyledComponent.colored(GOLD, world.getName()));
    }

    @Format
    public Component format(WorldProperties world)
    {
        return HoverComponent.hoverText(Text.of(YELLOW, world.getUniqueId()),
                    StyledComponent.colored(GOLD, world.getWorldName()));
    }
}
