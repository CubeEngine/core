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
package org.cubeengine.libcube.service;

import org.cubeengine.converter.ConverterManager;
import org.cubeengine.libcube.service.config.BlockTypeConverter;
import org.cubeengine.libcube.service.config.ConfigWorld;
import org.cubeengine.libcube.service.config.ConfigWorldConverter;
import org.cubeengine.libcube.service.config.ContextConverter;
import org.cubeengine.libcube.service.config.DataContainerConverter;
import org.cubeengine.libcube.service.config.DataQueryConverter;
import org.cubeengine.libcube.service.config.DurationConverter;
import org.cubeengine.libcube.service.config.EnchantmentConverter;
import org.cubeengine.libcube.service.config.ItemStackConverter;
import org.cubeengine.libcube.service.config.ItemTypeConverter;
import org.cubeengine.libcube.service.config.LocationConverter;
import org.cubeengine.libcube.service.config.TransformConverter;
import org.cubeengine.libcube.service.config.Vector3iConverter;
import org.cubeengine.libcube.service.config.VersionConverter;
import org.cubeengine.libcube.service.config.WorldConverter;
import org.cubeengine.libcube.util.Version;
import org.cubeengine.reflect.Reflector;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.time.Duration;

public class ReflectorProvider
{
    private final Reflector reflector = new Reflector();

    public ReflectorProvider()
    {
        ConverterManager manager = reflector.getDefaultConverterManager();
        manager.registerConverter(new TransformConverter(), Transform.class);
        manager.registerConverter(new Vector3iConverter(), Vector3i.class);
        manager.registerConverter(new DurationConverter(), Duration.class);
        manager.registerConverter(new VersionConverter(), Version.class);
        manager.registerConverter(new WorldConverter(), ServerWorld.class);
        manager.registerConverter(new ConfigWorldConverter(), ConfigWorld.class);
        manager.registerConverter(new LocationConverter(), Location.class);
        manager.registerConverter(new DataContainerConverter(), DataContainer.class);
        manager.registerConverter(new DataQueryConverter(), DataQuery.class);
        manager.registerConverter(new ContextConverter(), Context.class);
        manager.registerConverter(new ItemStackConverter(), ItemStack.class);
        manager.registerConverter(new ItemTypeConverter(), ItemType.class);
        manager.registerConverter(new BlockTypeConverter(), BlockType.class);
        manager.registerConverter(new EnchantmentConverter(), Enchantment.class);

    }

    public Reflector get()
    {
        return reflector;
    }
}
