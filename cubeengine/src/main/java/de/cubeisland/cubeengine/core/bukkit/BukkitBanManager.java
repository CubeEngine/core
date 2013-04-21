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
package de.cubeisland.cubeengine.core.bukkit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.v1_5_R2.BanEntry;
import net.minecraft.server.v1_5_R2.BanList;
import net.minecraft.server.v1_5_R2.DedicatedPlayerList;
import org.bukkit.craftbukkit.v1_5_R2.CraftServer;

import de.cubeisland.cubeengine.core.ban.Ban;
import de.cubeisland.cubeengine.core.ban.BanManager;
import de.cubeisland.cubeengine.core.ban.IpBan;
import de.cubeisland.cubeengine.core.ban.UserBan;
import de.cubeisland.cubeengine.core.user.User;

import gnu.trove.set.hash.THashSet;

public class BukkitBanManager implements BanManager
{
    private final BukkitCore core;
    private final BanList nameBans;
    private final BanList ipBans;

    public BukkitBanManager(BukkitCore core)
    {
        this.core = core;
        final DedicatedPlayerList playerList = ((CraftServer)core.getServer()).getHandle();
        this.nameBans = playerList.getNameBans();
        this.ipBans = playerList.getIPBans();
    }

    @Override
    public void addBan(Ban ban)
    {
        assert ban != null: "Ban must not be null!";

        BanEntry entry = new BanEntry(ban.toString());
        entry.setCreated(ban.getCreated());
        entry.setExpires(ban.getExpires());
        entry.setReason(ban.getReason());
        entry.setSource(ban.getSource());
        if (ban instanceof UserBan)
        {
            this.nameBans.add(entry);
        }
        else
        {
            this.ipBans.add(entry);
        }
    }

    @Override
    public UserBan getBan(User user)
    {
        assert user != null: "The user must not be null!";

        BanEntry entry = (BanEntry)this.nameBans.getEntries().get(user.getName());
        if (entry != null)
        {
            return new UserBan(user.getName(), entry.getSource(), entry.getReason(), entry.getCreated(), entry.getExpires());
        }
        return null;
    }

    @Override
    public IpBan getBan(InetAddress address)
    {
        assert address != null: "The address must not be null!";

        BanEntry entry = (BanEntry)this.ipBans.getEntries().get(address.toString());
        if (entry != null)
        {
            return new IpBan(address, entry.getSource(), entry.getReason(), entry.getCreated(), entry.getExpires());
        }
        return null;
    }

    @Override
    public boolean removeBan(User user)
    {
        assert user != null: "The user must not be null!";

        this.nameBans.remove(user.getName());
        return true;
    }

    @Override
    public boolean removeBan(InetAddress address)
    {
        assert address != null: "The address must not be null!";

        this.ipBans.remove(address.toString());
        return true;
    }

    @Override
    public boolean isBanned(User user)
    {
        return this.nameBans.isBanned(user.getName());
    }

    @Override
    public boolean isBanned(InetAddress address)
    {
        return this.ipBans.isBanned(address.toString());
    }

    @SuppressWarnings("unchecked")
    public Set<IpBan> getIpBans()
    {
        Map ipBans = this.ipBans.getEntries();
        Set<IpBan> bans = new THashSet<IpBan>(ipBans.size());

        for (BanEntry entry : (Collection<BanEntry>)ipBans.values())
        {
            try
            {
                bans.add(new IpBan(InetAddress.getByName(entry.getName()), entry.getSource(), entry.getReason(), entry.getCreated(), entry.getExpires()));
            }
            catch (UnknownHostException e)
            {
                this.ipBans.remove(entry.getName());
            }
        }

        return bans;
    }

    @SuppressWarnings("unchecked")
    public Set<UserBan> getUserBans()
    {
        Map nameBans = this.nameBans.getEntries();
        Set<UserBan> bans = new THashSet<UserBan>(nameBans.size());

        for (BanEntry entry : (Collection<BanEntry>)nameBans.values())
        {
            bans.add(new UserBan(entry.getName(), entry.getSource(), entry.getReason(), entry.getCreated(), entry.getExpires()));
        }
        return bans;
    }

    public Set<Ban> getBans()
    {
        Set<Ban> bans = new THashSet<Ban>();
        bans.addAll(this.getIpBans());
        bans.addAll(this.getUserBans());
        return bans;
    }
}
