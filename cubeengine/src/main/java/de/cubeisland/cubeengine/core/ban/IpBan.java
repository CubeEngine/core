package de.cubeisland.cubeengine.core.ban;

import java.net.InetAddress;
import java.util.Date;

public class IpBan extends Ban
{
    private InetAddress address;

    public IpBan(String source, String reason, InetAddress address)
    {
        this(source, reason, new Date(System.currentTimeMillis()), null, address);
    }

    public IpBan(String source, String reason, Date expires, InetAddress address)
    {
        this(source, reason, new Date(System.currentTimeMillis()), expires, address);
    }

    public IpBan(String source, String reason, Date created, Date expires, InetAddress address)
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
    public String toString()
    {
        return this.getAddress().toString();
    }
}
