package de.cubeisland.cubeengine.basics;

import de.cubeisland.cubeengine.basics.storage.BasicUser;
import de.cubeisland.cubeengine.core.user.UserAttachment;

public class BasicsAttachment extends UserAttachment
{
    private long lastAction = 0;
    private BasicUser basicUser = null;
    private boolean afk;

    @Override
    public void onAttach()
    {
        super.onAttach();
        this.lastAction = System.currentTimeMillis();
    }

    public long getLastAction()
    {
        return this.lastAction;
    }

    public long updateLastAction()
    {
        return this.lastAction = System.currentTimeMillis();
    }

    public void setAfk(boolean afk)
    {
        this.afk = afk;
    }

    public boolean isAfk()
    {
        return afk;
    }
}
