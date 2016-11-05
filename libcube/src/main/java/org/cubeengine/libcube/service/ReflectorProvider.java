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
package org.cubeengine.libcube.service;

import javax.inject.Provider;
import de.cubeisland.engine.converter.ConverterManager;
import de.cubeisland.engine.logscribe.LogLevel;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import de.cubeisland.engine.reflect.Reflector;
import org.cubeengine.libcube.util.Version;
import org.cubeengine.libcube.util.math.BlockVector3;
import org.cubeengine.libcube.service.config.BlockVector3Converter;
import org.cubeengine.libcube.service.config.DataContainerConverter;
import org.cubeengine.libcube.service.config.DurationConverter;
import org.cubeengine.libcube.service.config.LevelConverter;
import org.cubeengine.libcube.service.config.LocationConverter;
import org.cubeengine.libcube.service.config.VersionConverter;
import org.cubeengine.libcube.service.config.WorldConverter;
import org.cubeengine.libcube.service.config.WorldTransformConverter;
import org.cubeengine.libcube.service.config.ConfigWorld;
import org.cubeengine.libcube.service.config.ConfigWorldConverter;
import org.cubeengine.libcube.service.config.WorldTransform;
import org.joda.time.Duration;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@ServiceProvider(Reflector.class)
public class ReflectorProvider implements Provider<Reflector>
{
    private final Reflector reflector = new Reflector();

    public ReflectorProvider()
    {
        ConverterManager manager = reflector.getDefaultConverterManager();
        manager.registerConverter(new WorldTransformConverter(), WorldTransform.class);
        manager.registerConverter(new BlockVector3Converter(), BlockVector3.class);
        manager.registerConverter(new DurationConverter(), Duration.class);
        manager.registerConverter(new VersionConverter(), Version.class);
        manager.registerConverter(new LevelConverter(), LogLevel.class);
        manager.registerConverter(new WorldConverter(), World.class);
        manager.registerConverter(new ConfigWorldConverter(), ConfigWorld.class);
        manager.registerConverter(new LocationConverter(), Location.class);
        manager.registerConverter(new DataContainerConverter(), DataContainer.class);
    }

    @Override
    public Reflector get()
    {
        return reflector;
    }
}
