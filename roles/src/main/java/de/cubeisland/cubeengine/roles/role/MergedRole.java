package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.roles.config.Priority;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MergedRole extends Role
{
    private Collection<Role> mergedWith;

    public MergedRole(Collection<Role> roleToMerge)
    {
        if (roleToMerge != null && !roleToMerge.isEmpty())
        {
            this.mergedWith = roleToMerge;
            Map<String, RoleMetaData> tempMeta = new HashMap<String, RoleMetaData>();
            for (Role toMerge : roleToMerge)
            {
                Map<String, RolePermission> parentPerms = toMerge.getPerms();
                for (String permKey : parentPerms.keySet())
                {
                    if (this.perms.containsKey(permKey))
                    {
                        if (this.perms.get(permKey).getPriorityValue() >= parentPerms.get(permKey).getPriorityValue())
                        {
                            continue;
                        }
                    }
                    this.perms.put(permKey, parentPerms.get(permKey));
                }
                Map<String, RoleMetaData> parentData = toMerge.getMetaData();
                if (parentData != null)
                {
                    for (String dataKey : parentData.keySet())
                    {
                        if (tempMeta.containsKey(dataKey))
                        {
                            if (toMerge.getPriority().value < tempMeta.get(dataKey).getPriorityValue())
                            {
                                continue;
                            }
                        }
                        tempMeta.put(dataKey, parentData.get(dataKey));
                    }
                }
                for (String data : tempMeta.keySet())
                {
                    this.metaData.put(data, tempMeta.get(data));
                }
            }
        }
        else
        {
            mergedWith = new ArrayList<Role>();
        }
    }

    /**
     * Constructor fo userspecific-role
     *
     * @param perms
     * @param meta
     */
    MergedRole(String username, THashMap<String, Boolean> perms, THashMap<String, String> meta)
    {
        this.name = username;
        this.perms = new HashMap<String, RolePermission>();
        if (perms != null)
        {
            this.litaralPerms = perms;
            for (String keyPerm : perms.keySet())
            {
                if (keyPerm.endsWith("*"))
                {
                    Map<String, Boolean> map = new HashMap<String, Boolean>();
                    this.resolveBukkitPermission(keyPerm, perms.get(keyPerm), map);
                    for (String subPermKey : map.keySet())
                    {
                        this.perms.put(subPermKey, new RolePermission(subPermKey, map.get(subPermKey), this));
                    }
                }
                this.perms.put(keyPerm, new RolePermission(keyPerm, perms.get(keyPerm), this));
            }
        }
        this.metaData = new HashMap<String, RoleMetaData>();
        if (meta != null)
        {
            this.metaData = new HashMap<String, RoleMetaData>();
            for (Entry<String, String> entry : meta.entrySet())
            {
                this.metaData.put(entry.getKey(), new RoleMetaData(entry.getKey(), entry.getValue(), this));
            }
        }
    }

    public Collection<Role> getMergedWith()
    {
        return mergedWith;
    }

    @Override
    public void setPermission(String perm, Boolean set)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setMetaData(String key, String value)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void clearMetaData()
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean setParentRole(String pRole)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean removeParentRole(String pRole)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void clearParentRoles()
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setPriority(Priority priority)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void rename(String newName)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
}
