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

import java.util.Locale;

import org.bukkit.World;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.command.ArgumentReader;
import de.cubeisland.engine.core.command.exception.ReaderException;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;

public class WorldReader extends ArgumentReader
{
    private final Core core;

    public WorldReader(Core core)
    {
        this.core = core;
    }

    @Override
    public World read(String arg, Locale locale) throws ReaderException
    {
        World world = this.core.getWorldManager().getWorld(arg);
        if (world == null)
        {
            throw new ReaderException(CubeEngine.getI18n().translate(locale, NEGATIVE, "World {input} not found!", arg));
        }
        return world;
    }
}
