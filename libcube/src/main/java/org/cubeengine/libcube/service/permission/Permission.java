package org.cubeengine.libcube.service.permission;

import java.util.Collections;
import java.util.Set;

public class Permission
{
    private final String id;
    private final String desc;
    private final Set<String> explicitParents;

    public Permission(String id, String desc, Set<String> explicitParents)
    {
        this.id = id;
        this.desc = desc;
        this.explicitParents = Collections.unmodifiableSet(explicitParents);
    }

    public String getId()
    {
        return id;
    }

    public String getDesc()
    {
        return desc;
    }

    public Set<String> getExplicitParents()
    {
        return explicitParents;
    }
}
