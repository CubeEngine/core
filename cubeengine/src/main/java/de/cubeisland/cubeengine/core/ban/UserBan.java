package de.cubeisland.cubeengine.core.ban;

import java.util.Date;

public class UserBan extends Ban
{
    private final String target;

    public UserBan(String target, String source, String reason)
    {
        this(target, source, reason, new Date(System.currentTimeMillis()), null);
    }

    public UserBan(String target, String source, String reason, Date expires)
    {
        this(target, source, reason, new Date(System.currentTimeMillis()), expires);
    }

    public UserBan(String target, String source, String reason, Date created, Date expires)
    {
        super(source, reason, created, expires);
        assert target != null: "The user must not be null!";
        this.target = target;
    }

    @Override
    public String getTarget()
    {
        return this.target;
    }
}
