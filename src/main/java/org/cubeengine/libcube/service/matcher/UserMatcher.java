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
package org.cubeengine.libcube.service.matcher;

import static java.util.stream.Collectors.toList;

import com.google.inject.Inject;
import org.cubeengine.libcube.service.config.UserConverter;
import org.cubeengine.reflect.Reflector;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.user.UserManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserMatcher
{
    @Inject private StringMatcher sm;

    @Inject
    public UserMatcher(Reflector reflector)
    {
        reflector.getDefaultConverterManager().registerConverter(new UserConverter(this), User.class);
    }

    public Optional<User> match(String name, boolean searchOffline)
    {
        if (name == null)
        {
            return null;
        }
        // Direct Match Online Players:
        Optional<ServerPlayer> player = Sponge.server().player(name);
        if (player.isPresent())
        {
            return Optional.of(player.get().user());
        }

        // Find Online Players with similar name
        Map<String, ServerPlayer> onlinePlayerMap = new HashMap<>();
        for (ServerPlayer onlineUser : Sponge.server().onlinePlayers())
        {
            onlinePlayerMap.put(onlineUser.name(), onlineUser);
        }
        String foundUser = sm.matchString(name, onlinePlayerMap.keySet());
        if (foundUser != null)
        {
            return Optional.of(onlinePlayerMap.get(foundUser).user());
        }

        final UserManager userManager = Sponge.server().userManager();

        Optional<User> directMatchOffline;
        try
        {
            directMatchOffline = userManager.load(name).join();
        }
        catch (IllegalArgumentException ignore)
        {
            return Optional.empty();
        }
        if (directMatchOffline.isPresent())
        {
            return directMatchOffline;
        }

        if (searchOffline)
        {
            String match = sm.matchString(name, userManager.streamAll().map(GameProfile::name)
                                                       .filter(Optional::isPresent).map(Optional::get).collect(toList()));
            if (match != null)
            {
                return userManager.load(match).join();
            }
        }

        return Optional.empty();
    }
}
