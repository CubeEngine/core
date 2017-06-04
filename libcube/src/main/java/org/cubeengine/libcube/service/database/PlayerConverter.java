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
package org.cubeengine.libcube.service.database;

import org.jooq.impl.AbstractConverter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

public class PlayerConverter extends AbstractConverter<String, Player>
{
    public PlayerConverter()
    {
        super(String.class, Player.class);
    }

    @Override
    public Player from(String databaseObject)
    {
        if (databaseObject == null)
        {
            return null;
        }
        return Sponge.getServer().getPlayer(UUID.fromString(databaseObject)).orElse(null);
    }

    @Override
    public String to(Player userObject)
    {
        if (userObject == null)
        {
            return null;
        }
        return userObject.getUniqueId().toString();
    }
}
