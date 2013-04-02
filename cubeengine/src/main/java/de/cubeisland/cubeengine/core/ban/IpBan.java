package de.cubeisland.cubeengine.core.ban;

import java.net.InetAddress;
import java.util.Date;

public class IpBan extends Ban
{
    private InetAddress address;

    public IpBan(InetAddress address, String source, String reason)
    {
        this(address, source, reason, new Date(System.currentTimeMillis()), null);
    }

    public IpBan(InetAddress address, String source, String reason, Date expires)
    {
        this(address, source, reason, new Date(System.currentTimeMillis()), expires);
    }

    public IpBan(InetAddress address, String source, String reason, Date created, Date expires)
    {
        super(source, reason, created, expires);
        assert address != null: "The address must not be null!";
        this.address = address;
    }

    public InetAddress getAddress()
    {
        return address;
    }

    @Override
    public String getTarget()
    {
        return this.address.toString();
    }
}
