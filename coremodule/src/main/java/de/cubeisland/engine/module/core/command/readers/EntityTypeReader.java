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
package de.cubeisland.engine.module.core.command.readers;

import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import de.cubeisland.engine.module.core.sponge.SpongeCore;
import de.cubeisland.engine.module.core.util.matcher.EntityMatcher;
import de.cubeisland.engine.module.core.util.matcher.Match;
import org.spongepowered.api.entity.EntityType;

public class EntityTypeReader implements ArgumentReader<EntityType>
{

    private SpongeCore core;

    public EntityTypeReader(SpongeCore core)
    {

        this.core = core;
    }

    @Override
    public EntityType read(Class type, CommandInvocation invocation) throws ReaderException
    {
        return core.getModularity().start(EntityMatcher.class).any(invocation.consume(1));
    }
}
