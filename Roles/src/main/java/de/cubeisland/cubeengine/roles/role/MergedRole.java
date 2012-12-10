package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.roles.role.config.Priority;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MergedRole extends Role
{

    private Collection<Role> mergedWith;

    public MergedRole(Collection<Role> roleToMerge)
    {
        if (roleToMerge != null && !roleToMerge.isEmpty())
        {
            this.mergedWith = roleToMerge;
            Map<String, Pair<String, Priority>> tempMeta = new HashMap<String, Pair<String, Priority>>();
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
                Map<String, String> parentData = toMerge.getMetaData();
                for (String dataKey : parentData.keySet())
                {
                    if (tempMeta.containsKey(dataKey))
                    {
                        if (toMerge.getPriority().value < tempMeta.get(dataKey).getRight().value)
                        {
                            continue;
                        }
                    }
                    tempMeta.put(dataKey, new Pair<String, Priority>(parentData.get(dataKey), toMerge.getPriority()));
                }
                for (String data : tempMeta.keySet())
                {
                    this.metaData.put(data, tempMeta.get(data).getLeft());
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
    public MergedRole(String username, THashMap<String, Boolean> perms, THashMap<String, String> meta)
    {
        this.name = username;
        this.perms = new HashMap<String, RolePermission>();
        if (perms != null)
        {
            for (String keyPerm : perms.keySet())
            {
                if (keyPerm.endsWith("*"))
                {
                    Map<String, Boolean> map = new HashMap<String, Boolean>();
                    this.resolveBukkitPermission(keyPerm, map);
                    for (String subPermKey : map.keySet())
                    {
                        this.perms.put(subPermKey, new RolePermission(subPermKey, map.get(subPermKey), this));
                    }
                }
                this.perms.put(keyPerm, new RolePermission(keyPerm, perms.get(keyPerm), this));
            }
        }
        if (meta == null)
        {
            this.metaData = new HashMap<String, String>();
        }
        else
        {
            this.metaData = meta;
        }
    }

    public Collection<Role> getMergedWith()
    {
        return mergedWith;
    }
}
