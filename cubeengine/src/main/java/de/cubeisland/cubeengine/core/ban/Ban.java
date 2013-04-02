package de.cubeisland.cubeengine.core.ban;

import java.util.Date;

public abstract class Ban
{
    private String source;
    private String reason;
    private Date created;
    private Date expires;

    protected Ban(String source, String reason, Date expires)
    {
        this(source, reason, new Date(System.currentTimeMillis()), expires);
    }

    protected Ban(String source, String reason, Date created, Date expires)
    {
        assert source != null: "The source must not be null";
        assert reason != null: "The reason must not be null";
        assert created != null: "The created must not be null";
        this.source = source;
        this.reason = reason;
        this.created = created;
        this.expires = expires;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        assert source != null: "The source must not be null";
        this.source = source;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        assert reason != null: "The reason must not be null";
        this.reason = reason;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated(Date created)
    {
        assert created != null: "The created must not be null";
        this.created = created;
    }

    public Date getExpires()
    {
        return expires;
    }

    public void setExpires(Date expires)
    {
        this.expires = expires;
    }

    public boolean isExpired()
    {
        Date expires = this.getExpires();
        if (expires != null)
        {
            return expires.getTime() <= System.currentTimeMillis();
        }
        return false;
    }

    @Override
    public String toString()
    {
        throw new UnsupportedOperationException();
    }
}
