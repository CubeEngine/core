/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.roles.role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import gnu.trove.map.hash.THashMap;

public class MergedRole extends Role
{
    private Collection<ConfigRole> mergedWith;

    public MergedRole(Collection<ConfigRole> roleToMerge)
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
            mergedWith = new ArrayList<ConfigRole>();
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
                Map<String, Boolean> map = new HashMap<String, Boolean>();
                this.resolveBukkitPermission(keyPerm, perms.get(keyPerm), map);
                for (String subPermKey : map.keySet())
                {
                    this.perms.put(subPermKey, new RolePermission(subPermKey, map.get(subPermKey), this));
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

    public Collection<ConfigRole> getMergedWith()
    {
        return mergedWith;
    }
}
