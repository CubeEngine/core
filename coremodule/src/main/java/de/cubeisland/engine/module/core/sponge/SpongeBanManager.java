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
package de.cubeisland.engine.module.core.sponge;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import de.cubeisland.engine.module.core.ban.Ban;
import de.cubeisland.engine.module.core.ban.BanManager;
import de.cubeisland.engine.module.core.ban.IpBan;
import de.cubeisland.engine.module.core.ban.UserBan;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.entity.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.util.ban.Ban.Ip;
import org.spongepowered.api.util.ban.BanBuilder;

import static de.cubeisland.engine.module.core.CubeEngine.isMainThread;
import static de.cubeisland.engine.module.core.contract.Contract.expect;
import static de.cubeisland.engine.module.core.contract.Contract.expectNotNull;
import static java.util.stream.Collectors.toSet;

public class SpongeBanManager implements BanManager
{
    private final BanService manager;
    private final BanBuilder banBuilder;

    public SpongeBanManager(SpongeCore core)
    {
        manager = core.getGame().getServiceManager().provide(BanService.class).get();
        banBuilder = core.getGame().getRegistry().getBuilderOf(BanBuilder.class).get();
    }

    @Override
    public void addBan(Ban ban)
    {
        expectNotNull(ban, "Ban must not be null!");
        expect(isMainThread());

        if (ban instanceof UserBan)
        {
            manager.ban(banBuilder.user(((UserBan)ban).getTarget()).reason(ban.getReason()).expirationDate(
                ban.getExpires()).source(ban.getSource()).build());
        }
        else if (ban instanceof IpBan)
        {
            manager.ban(banBuilder.address(((IpBan)ban).getTarget()).reason(ban.getReason()).expirationDate(
                ban.getExpires()).source(ban.getSource()).build());
        }
    }

    @Override
    public UserBan getUserBan(UUID uuid)
    {
        expect(isMainThread());
        User user = getUserByUUID(uuid);
        org.spongepowered.api.util.ban.Ban.User last = null;
        for (org.spongepowered.api.util.ban.Ban.User ban : manager.getBansFor(user))
        {
            if (ban.isIndefinite())
            {
                return new UserBan(user, ban.getSource().orNull(), ban.getReason(), ban.getStartDate(), ban.getExpirationDate().orNull());
            }
            if (last == null || last.getStartDate().after(ban.getStartDate()))
            {
                last = ban;
            }
        }
        if (last == null)
        {
            return null;
        }
        return new UserBan(user, last.getSource().orNull(), last.getReason(), last.getStartDate(), last.getExpirationDate().orNull());
    }

    @Override
    public IpBan getIpBan(InetAddress address)
    {
        expectNotNull(address, "The address must not be null!");
        expect(isMainThread());

        Ip last = null;
        for (Ip ban : manager.getBansFor(address))
        {
            if (ban.isIndefinite())
            {
                return new IpBan(address, ban.getSource().orNull(), ban.getReason(), ban.getStartDate(), ban.getExpirationDate().orNull());
            }
            if (last == null || last.getStartDate().after(ban.getStartDate()))
            {
                last = ban;
            }
        }
        if (last == null)
        {
            return null;
        }
        return new IpBan(address, last.getSource().orNull(), last.getReason(), last.getStartDate(), last.getExpirationDate().orNull());
    }

    @Override
    public boolean removeUserBan(UUID uuid)
    {
        expect(isMainThread());
        if (!this.isUserBanned(uuid))
        {
            return false;
        }
        User user = getUserByUUID(uuid);
        manager.pardon(user);
        return true;
    }

    @Override
    public boolean removeIpBan(InetAddress address)
    {
        expectNotNull(address, "The address must not be null!");
        expect(isMainThread());
        if (!this.isIpBanned(address))
        {
            return false;
        }
        manager.pardon(address);
        return true;
    }

    @Override
    public boolean isUserBanned(UUID uuid)
    {
        expect(isMainThread());
        User user = getUserByUUID(uuid);
        return manager.isBanned(user);
    }

    @Override
    public boolean isIpBanned(InetAddress address)
    {
        expect(isMainThread());
        return manager.isBanned(address);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<IpBan> getIpBans()
    {
        expect(isMainThread());
        return manager.getIpBans().stream()
                      .map(ban -> new IpBan(ban.getAddress(), ban.getSource().orNull(), ban.getReason(),
                                            ban.getStartDate(), ban.getExpirationDate().orNull()))
                      .collect(toSet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<UserBan> getUserBans()
    {
        expect(isMainThread());
        return manager.getUserBans().stream()
                      .map(ban -> new UserBan((User)ban.getUser(), ban.getSource().orNull(), ban.getReason(), // TODO remove cast
                                              ban.getStartDate(), ban.getExpirationDate().orNull()))
                      .collect(toSet());
    }

    @Override
    public Set<Ban<?>> getBans()
    {
        expect(isMainThread());
        
        Set<Ban<?>> bans = new HashSet<>();
        bans.addAll(this.getIpBans());
        bans.addAll(this.getUserBans());
        return bans;
    }

    @Override
    public synchronized void reloadBans()
    {
        expect(isMainThread());
        try
        {
            this.profileBan.load();
            this.ipBans.load();
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
