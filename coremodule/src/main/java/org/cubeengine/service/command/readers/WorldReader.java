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
package org.cubeengine.service.command.readers;

import java.util.Locale;
import com.google.common.base.Optional;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parameter.TooFewArgumentsException;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.DefaultValue;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;

import org.cubeengine.service.command.TranslatedReaderException;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.service.user.MultilingualPlayer;
import org.cubeengine.service.world.WorldManager;
import org.spongepowered.api.world.World;

import static org.cubeengine.service.i18n.formatter.MessageType.NEGATIVE;

public class WorldReader implements ArgumentReader<World>, DefaultValue<World>
{
    private final WorldManager wm;
    private final I18n i18n;

    public WorldReader(WorldManager wm, I18n i18n)
    {
        this.wm = wm;
        this.i18n = i18n;
    }

    @Override
    public World read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String name = invocation.consume(1);
        Optional<World> world = wm.getWorld(name);
        if (!world.isPresent())
        {
            throw new TranslatedReaderException(i18n.translate(invocation.getContext(Locale.class), NEGATIVE, "World {input} not found!", name));
        }
        return world.get();
    }

    @Override
    public World getDefault(CommandInvocation invocation)
    {
        if (invocation.getCommandSource() instanceof MultilingualPlayer)
        {
            return ((MultilingualPlayer)invocation.getCommandSource()).original().getWorld();
        }
        throw new TooFewArgumentsException();
    }
}
