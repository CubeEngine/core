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

import java.util.Optional;
import org.cubeengine.libcube.service.command.DefaultParameterProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader.Mutable;
import org.spongepowered.api.command.parameter.CommandContext.Builder;
import org.spongepowered.api.command.parameter.Parameter.Key;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.profile.GameProfile;

public class UserDefaultParameterProvider implements DefaultParameterProvider<User>, ValueParser<User>
{
    @Override
    public User apply(CommandCause commandCause)
    {
        if (commandCause.subject() instanceof ServerPlayer) {
            return ((ServerPlayer)commandCause.subject()).user();
        }
        return null;
    }

    @Override
    public Optional<? extends User> parseValue(Key<? super User> parameterKey, Mutable reader, Builder context) throws ArgumentParseException
    {
        final String name = reader.parseString();
        final Optional<User> user = Sponge.server().userManager().load(name).join();
        if (user.isPresent())
        {
            return user;
        }
        final GameProfile profile = Sponge.server().gameProfileManager().basicProfile(name).join();
        return Optional.of(Sponge.server().userManager().loadOrCreate(profile.uuid()).join());
    }
}
