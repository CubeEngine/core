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
package de.cubeisland.engine.service.command.readers;

import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;

import de.cubeisland.engine.module.core.sponge.CoreModule;
import org.spongepowered.api.entity.player.User;

public class OfflinePlayerReader implements ArgumentReader<User>
{
    private final CoreModule core;

    public OfflinePlayerReader(CoreModule core)
    {
        this.core = core;
    }

    @Override
    public User read(Class type, CommandInvocation invocation) throws ReaderException
    {
        if (invocation.currentToken().startsWith("-"))
        {
            throw new ReaderException("Players do not start with -");
        }
        return null;
        // TODO return this.core.getGame().getOfflinePlayer(invocation.consume(1));
    }
}
