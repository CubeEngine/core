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
package de.cubeisland.engine.core.bukkit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.v1_7_R1.BanEntry;
import net.minecraft.server.v1_7_R1.BanList;
import net.minecraft.server.v1_7_R1.DedicatedPlayerList;
import org.bukkit.craftbukkit.v1_7_R1.CraftServer;

import de.cubeisland.engine.core.ban.Ban;
import de.cubeisland.engine.core.ban.BanManager;
import de.cubeisland.engine.core.ban.IpBan;
import de.cubeisland.engine.core.ban.UserBan;
import de.cubeisland.engine.core.user.User;

import gnu.trove.set.hash.THashSet;

public class BukkitBanManager implements BanManager
{
    private final BanList nameBans;
    private final BanList ipBans;

    public BukkitBanManager(BukkitCore core)
    {
        final DedicatedPlayerList playerList = ((CraftServer)core.getServer()).getHandle();
        this.nameBans = playerList.getNameBans();
        this.ipBans = playerList.getIPBans();
    }

    @Override
    public void addBan(Ban ban)
    {
        assert ban != null: "Ban must not be null!";

        if (ban.getReason().contains("\n") || ban.getReason().contains("\r"))
        {
            throw new IllegalArgumentException("The ban reason my not contain line breaks (LF or CR)!");
        }

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
    public UserBan getUserBan(User user)
    {
        assert user != null: "The user must not be null!";

        return this.getUserBan(user.getName());
    }

    @Override
    public UserBan getUserBan(String name)
    {

        BanEntry entry = (BanEntry)this.nameBans.getEntries().get(name);
        if (entry != null)
        {
            return new UserBan(name, entry.getSource(), entry.getReason(), entry.getCreated(), entry.getExpires());
        }
        return null;
    }

    @Override
    public IpBan getIpBan(InetAddress address)
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
    public boolean removeUserBan(User user)
    {
        assert user != null: "The user must not be null!";

        return this.removeUserBan(user.getName());
    }

    @Override
    public boolean removeUserBan(String name)
    {
        this.nameBans.remove(name);
        return true;
    }

    @Override
    public boolean removeIpBan(InetAddress address)
    {
        assert address != null: "The address must not be null!";

        this.ipBans.remove(address.getHostAddress());
        return true;
    }

    @Override
    public boolean isUserBanned(User user)
    {
        assert user != null: "The user must not be null!";

        return this.isUserBanned(user.getName());
    }

    @Override
    public boolean isUserBanned(String name)
    {
        return this.nameBans.isBanned(name);
    }

    @Override
    public boolean isIpBanned(InetAddress address)
    {
        return this.ipBans.isBanned(address.toString());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<IpBan> getIpBans()
    {
        Map ipBans = this.ipBans.getEntries();
        Set<IpBan> bans = new THashSet<>(ipBans.size());

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

    @Override
    @SuppressWarnings("unchecked")
    public Set<UserBan> getUserBans()
    {
        Map nameBans = this.nameBans.getEntries();
        Set<UserBan> bans = new THashSet<>(nameBans.size());

        for (BanEntry entry : (Collection<BanEntry>)nameBans.values())
        {
            bans.add(new UserBan(entry.getName(), entry.getSource(), entry.getReason(), entry.getCreated(), entry.getExpires()));
        }
        return bans;
    }

    @Override
    public Set<Ban> getBans()
    {
        Set<Ban> bans = new THashSet<>();
        bans.addAll(this.getIpBans());
        bans.addAll(this.getUserBans());
        return bans;
    }

    @Override
    public synchronized void reloadBans()
    {
        this.nameBans.getEntries().clear();
        this.nameBans.load();

        this.ipBans.getEntries().clear();
        this.ipBans.load();
    }
}
