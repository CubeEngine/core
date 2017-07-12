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

import java.util.Collections;

import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.parameter.argument.DefaultValue;
import org.spongepowered.api.entity.living.player.Player;

public class UserListInSight implements DefaultValue<PlayerList>
{
    @Override
    public PlayerList provide(CommandInvocation invocation)
    {
        if (invocation.getCommandSource() instanceof Player)
        {
            Player player = getFirstPlayerInSight(((Player)invocation.getCommandSource()));
            if (player != null)
            {
                return new PlayerList(Collections.singletonList(player));
            }
        }
        return null;
    }

    private Player getFirstPlayerInSight(Player source)
    {
        throw new UnsupportedOperationException("not implemented yet");
        // TODO https://github.com/SpongePowered/SpongeAPI/issues/797 is done use it!
    }
}
