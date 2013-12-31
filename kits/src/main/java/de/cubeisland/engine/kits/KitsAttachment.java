package de.cubeisland.engine.kits;

import java.util.HashMap;
import java.util.Map;

import de.cubeisland.engine.core.user.UserAttachment;

public class KitsAttachment extends UserAttachment
{
    private Map<String, Long> kitUsages = new HashMap<>();

    public void setKitUsage(String name)
    {
        this.kitUsages.put(name, System.currentTimeMillis());
    }

    public Long getKitUsage(String name)
    {
        return this.kitUsages.get(name);
    }
}
