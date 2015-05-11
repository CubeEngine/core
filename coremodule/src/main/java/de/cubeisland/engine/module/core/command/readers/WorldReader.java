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

import com.google.common.base.Optional;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parameter.TooFewArgumentsException;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.DefaultValue;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;

import de.cubeisland.engine.module.core.i18n.I18n;
import de.cubeisland.engine.module.core.sponge.SpongeCore;
import de.cubeisland.engine.module.core.user.User;
import de.cubeisland.engine.module.core.world.WorldManager;
import org.spongepowered.api.world.World;

import static de.cubeisland.engine.module.core.util.formatter.MessageType.NEGATIVE;

public class WorldReader implements ArgumentReader<World>, DefaultValue<World>
{
    private final SpongeCore core;

    public WorldReader(SpongeCore core)
    {
        this.core = core;
    }

    @Override
    public World read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String name = invocation.consume(1);
        Optional<World> world = this.core.getModularity().start(WorldManager.class).getWorld(name);
        if (!world.isPresent())
        {
            throw new ReaderException(core.getModularity().start(I18n.class).translate(invocation.getLocale(), NEGATIVE,
                                                                                 "World {input} not found!", name));
        }
        return world.get();
    }

    @Override
    public World getDefault(CommandInvocation invocation)
    {
        if (invocation.getCommandSource() instanceof User)
        {
            return ((User)invocation.getCommandSource()).getWorld();
        }
        throw new TooFewArgumentsException();
    }
}
