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

import net.kyori.adventure.text.format.NamedTextColor;
import org.cubeengine.dirigent.formatter.reflected.Format;
import org.cubeengine.dirigent.formatter.reflected.Names;
import org.cubeengine.dirigent.formatter.reflected.ReflectedFormatter;
import org.cubeengine.dirigent.parser.component.Component;
import org.cubeengine.libcube.service.i18n.formatter.component.HoverComponent;
import org.cubeengine.libcube.service.i18n.formatter.component.StyledComponent;
import org.cubeengine.libcube.service.i18n.formatter.component.TextComponent;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;

@Names("world")
public class WorldFormatter extends ReflectedFormatter
{
    @Format
    public Component format(ServerWorld world)
    {
        final ResourceKey worldKey = world.getKey();
        return HoverComponent.hoverText(
            net.kyori.adventure.text.Component.text(worldKey.asString()).color(NamedTextColor.YELLOW),
            new TextComponent(world.getProperties().displayName().orElse(net.kyori.adventure.text.Component.text(worldKey.getValue()).color(NamedTextColor.GOLD))));
    }

    @Format
    public Component format(ServerWorldProperties world)
    {
        return HoverComponent.hoverText(
            net.kyori.adventure.text.Component.text(world.getUniqueId().toString()).color(NamedTextColor.YELLOW),
            StyledComponent.colored(NamedTextColor.GOLD, world.world().get().getDirectory().getFileName().toString()));
    }
}
