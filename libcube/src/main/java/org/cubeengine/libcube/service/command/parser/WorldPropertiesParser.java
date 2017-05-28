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
package org.cubeengine.libcube.service.command.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.parameter.argument.Completer;
import org.cubeengine.butler.parameter.TooFewArgumentsException;
import org.cubeengine.butler.parameter.argument.ArgumentParser;
import org.cubeengine.butler.parameter.argument.DefaultValue;
import org.cubeengine.butler.parameter.argument.ParserException;
import org.cubeengine.libcube.service.command.TranslatedParserException;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.storage.WorldProperties;

import static org.cubeengine.libcube.util.StringUtils.startsWithIgnoreCase;
import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;

public class WorldPropertiesParser implements ArgumentParser<WorldProperties>, DefaultValue<WorldProperties>, Completer
{
    private I18n i18n;

    public WorldPropertiesParser(I18n i18n)
    {
        this.i18n = i18n;
    }

    @Override
    public WorldProperties parse(Class type, CommandInvocation invocation) throws ParserException
    {
        String name = invocation.consume(1);
        Optional<WorldProperties> world = Sponge.getServer().getWorldProperties(name);
        if (!world.isPresent())
        {
            throw new TranslatedParserException(i18n.translate(invocation.getContext(Locale.class), NEGATIVE, "World {input} not found!", name));
        }
        return world.get();
    }

    @Override
    public WorldProperties provide(CommandInvocation invocation)
    {
        if (invocation.getCommandSource() instanceof Player)
        {
            return ((Player)invocation.getCommandSource()).getWorld().getProperties();
        }
        throw new TooFewArgumentsException();
    }

    @Override
    public List<String> suggest(Class type, CommandInvocation invocation)
    {
        List<String> offers = new ArrayList<>();
        for (WorldProperties world : Sponge.getServer().getAllWorldProperties())
        {
            final String name = world.getWorldName();
            if (startsWithIgnoreCase(name, invocation.currentToken()))
            {
                offers.add(name);
            }
        }
        return offers;
    }
}
