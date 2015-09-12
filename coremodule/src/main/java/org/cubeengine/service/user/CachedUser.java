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
package org.cubeengine.service.user;

import java.net.InetSocketAddress;
import org.spongepowered.api.entity.living.player.User;

public class CachedUser
{
    private UserEntity entity;
    private User spongeUser;

    public CachedUser(UserEntity entity, User spongeUser)
    {
        this.entity = entity;
        this.spongeUser = spongeUser;
    }

    public User getUser()
    {
        return spongeUser;
    }

    public UserEntity getEntity()
    {
        return entity;
    }

    private InetSocketAddress address = null;

    public void refreshIP()
    {
        address = this.getUser().getPlayer().get().getConnection().getAddress();
    }

    public InetSocketAddress getAddress()
    {
        return address;
    }
}
