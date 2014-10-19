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
package de.cubeisland.engine.core.command.readers;

import org.bukkit.World;

import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.parameter.reader.ArgumentReader;
import de.cubeisland.engine.command.parameter.reader.ReaderException;
import de.cubeisland.engine.command.parameter.reader.ReaderManager;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;

public class WorldReader implements ArgumentReader<World>
{
    private final Core core;

    public WorldReader(Core core)
    {
        this.core = core;
    }

    @Override
    public World read(ReaderManager manager, Class type, CommandInvocation invocation) throws ReaderException
    {
        String name = invocation.consume(1);
        World world = this.core.getWorldManager().getWorld(name);
        if (world == null)
        {
            throw new ReaderException(CubeEngine.getI18n().translate(invocation.getLocale(), NEGATIVE, "World {input} not found!", name));
        }
        return world;
    }
}
