package de.cubeisland.cubeengine.travel.storage;

import de.cubeisland.cubeengine.core.attachment.Attachment;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserAttachment;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WarpAttachment extends UserAttachment
{
    Map<String, Warp> warps;

    public WarpAttachment()
    {
        warps = new HashMap<String, Warp>();
    }

    public Warp getWarp(String name)
    {
        if (name == null)
        {
            return null;
        }

        if (warps.containsKey(name))
        {
            return warps.get(name);
        }
        if (name.contains(":"))
        {
            return warps.get(name.substring(name.lastIndexOf(":") + 1, name.length()));
        }
        return null;
    }

    /**
     * Will check if getWarp(name) is not null
     * @param name
     * @return
     */
    public boolean hasWarp(String name)
    {
        return getWarp(name) != null;
    }

    /**
     * Will find direct matches
     * @param name
     * @return
     */
    public boolean containsWarp(String name)
    {
        return warps.containsKey(name);
    }

    public Map<String, Warp> allWarps()
    {
        return this.warps;
    }

    public void addWarp(String name, Warp warp)
    {
        warps.put(name, warp);
    }

    public void removeWarp(String name)
    {
        warps.remove(name);
    }
}
