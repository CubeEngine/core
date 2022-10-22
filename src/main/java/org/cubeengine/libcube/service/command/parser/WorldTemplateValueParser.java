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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionTypes;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.world.server.DataPackManager;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldTemplate;

public class WorldTemplateValueParser implements ValueParser<WorldTemplate>, ValueCompleter
{

    @Override
    public Optional<? extends WorldTemplate> parseValue(Parameter.Key<? super WorldTemplate> parameterKey, ArgumentReader.Mutable reader,
            CommandContext.Builder context) throws ArgumentParseException
    {
        final ResourceKey key = reader.parseResourceKey("minecraft");
        final Optional<ServerWorld> world = Sponge.server().worldManager().world(key);
        if (world.isPresent())
        {
            throw reader.createException(Component.text("World " + key + " must be unloaded."));
        }
        final DataPackManager dpm = Sponge.server().dataPackManager();
        return dpm.findPack(DataPackTypes.WORLD, key).flatMap(pack -> dpm.load(pack, key).join());
    }

    @Override
    public List<ClientCompletionType> clientCompletionType()
    {
        return Arrays.asList(ClientCompletionTypes.RESOURCE_KEY.get());
    }

    @Override
    public List<CommandCompletion> complete(CommandContext context, String currentInput)
    {
        final List<CommandCompletion> list = new ArrayList<>();
        Sponge.server().worldManager().worldKeys().stream()
              .filter(key -> !Sponge.server().worldManager().world(key).isPresent())
              .forEach(k -> {
                     list.add(CommandCompletion.of(k.formatted()));
                     if (k.namespace().equals("minecraft"))
                     {
                         list.add(CommandCompletion.of(k.value()));
                     }
                 });
        return list;
    }


}
