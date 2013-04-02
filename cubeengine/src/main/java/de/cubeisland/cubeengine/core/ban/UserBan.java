package de.cubeisland.cubeengine.core.ban;

import java.util.Date;

import de.cubeisland.cubeengine.core.user.User;

public class UserBan extends Ban
{
    private final User user;

    public UserBan(String source, String reason, Date expires, User user)
    {
        this(source, reason, new Date(System.currentTimeMillis()), expires, user);
    }

    public UserBan(String source, String reason, Date created, Date expires, User user)
    {
        super(source, reason, created, expires);
        assert user != null: "The user must not be null!";
        this.user = user;
    }

    public User getUser()
    {
        return user;
    }

    @Override
    public String toString()
    {
        return this.getUser().getName();
    }
}
