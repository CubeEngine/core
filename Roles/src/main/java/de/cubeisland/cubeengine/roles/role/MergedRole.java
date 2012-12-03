
package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.util.Pair;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MergedRole extends Role
{
    public final Collection<Role> mergedWith;
    
    public MergedRole(Collection<Role> mergeFrom)
    {
        this.mergedWith = mergeFrom;
        Map<String, Pair<Boolean, Priority>> permissions = new HashMap<String, Pair<Boolean, Priority>>();
        Map<String, Pair<String, Priority>> metaData = new HashMap<String, Pair<String, Priority>>();
        for (Role role : mergeFrom)
        {
            for (Map.Entry<String, Boolean> permission : role.getPermissions().entrySet())
            {
                if (permissions.containsKey(permission.getKey()))
                {
                    if (role.getPriority().value < permissions.get(permission.getKey()).getRight().value)
                    {
                        continue;
                    }
                }
                permissions.put(permission.getKey(), new Pair<Boolean, Priority>(permission.getValue(), role.getPriority()));
            }
            for (Map.Entry<String, String> data : role.getMetaData().entrySet())
            {
                if (metaData.containsKey(data.getKey()))
                {
                    if (role.getPriority().value < metaData.get(data.getKey()).getRight().value)
                    {
                        continue;
                    }
                }
                metaData.put(data.getKey(), new Pair<String, Priority>(data.getValue(), role.getPriority()));
            }
        }
        for (String permission : permissions.keySet())
        {
            this.setPermission(permission, permissions.get(permission).getLeft());
        }
        for (String data : metaData.keySet())
        {
            this.setMetaData(data, metaData.get(data).getLeft());
        }
    }
    
}
