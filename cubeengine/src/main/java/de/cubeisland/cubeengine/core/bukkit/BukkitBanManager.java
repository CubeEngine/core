package de.cubeisland.cubeengine.core.bukkit;

import java.net.InetAddress;

import net.minecraft.server.v1_5_R2.BanEntry;
import net.minecraft.server.v1_5_R2.BanList;
import net.minecraft.server.v1_5_R2.DedicatedPlayerList;
import org.bukkit.craftbukkit.v1_5_R2.CraftServer;

import de.cubeisland.cubeengine.core.ban.Ban;
import de.cubeisland.cubeengine.core.ban.BanManager;
import de.cubeisland.cubeengine.core.ban.IpBan;
import de.cubeisland.cubeengine.core.ban.UserBan;
import de.cubeisland.cubeengine.core.user.User;

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
            return new UserBan(entry.getSource(), entry.getReason(), entry.getCreated(), entry.getExpires(), user);
        }
        return null;
    }

    @Override
    public IpBan getBan(InetAddress address)
    {
        assert address != null: "The address must not be null!";

        BanEntry entry = (BanEntry)this.nameBans.getEntries().get(address.toString());
        if (entry != null)
        {
            return new IpBan(entry.getSource(), entry.getReason(), entry.getCreated(), entry.getExpires(), address);
        }
        return null;
    }

    @Override
    public boolean removeBan(User user)
    {
        assert user != null: "The user must not be null!";

        return (this.nameBans.getEntries().get(user.getName()) != null);
    }

    @Override
    public boolean removeBan(InetAddress address)
    {
        assert address != null: "The address must not be null!";

        return (this.ipBans.getEntries().get(address.toString()) != null);
    }

    @Override
    public boolean isBanned(User user)
    {
        BanEntry entry = (BanEntry)this.nameBans.getEntries().get(user.getName());
        if (entry == null)
        {
            return false;
        }
        return !entry.hasExpired();
    }

    @Override
    public boolean isBanned(InetAddress address)
    {
        BanEntry entry = (BanEntry)this.ipBans.getEntries().get(address.toString());
        if (entry == null)
        {
            return false;
        }
        return !entry.hasExpired();
    }
}
