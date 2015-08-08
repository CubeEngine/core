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
package de.cubeisland.engine.bootstrap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import de.cubeisland.engine.modularity.core.LifeCycle;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.ValueProvider;
import de.cubeisland.engine.modularity.core.graph.meta.ModuleMetadata;

public class ModulePathProvider implements ValueProvider<Path>
{
    private final Path path;

    public ModulePathProvider(File dataFolder)
    {
        this.path = dataFolder.toPath().resolve("modules");
    }

    @Override
    public Path get(LifeCycle lifeCycle, Modularity modularity)
    {
        if (lifeCycle.getInformation() instanceof ModuleMetadata)
        {
            try
            {
                return Files.createDirectories(path.resolve(((ModuleMetadata)lifeCycle.getInformation()).getName()));
            }
            catch (IOException e)
            {
                throw new IllegalStateException("Could not create module folder" + e);
            }
        }
        throw new IllegalArgumentException(lifeCycle.getInformation().getIdentifier().name() + " is not a Module");
    }
}
