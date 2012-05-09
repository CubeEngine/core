package de.cubeisland.cubeengine.core;

import gnu.trove.set.hash.THashSet;
import org.bukkit.permissions.Permission;

/**
 *
 * @author Faithcaio
 */
public class CubeUser {
    
    THashSet<Permission> permissions = new THashSet<Permission>();
    
    public CubeUser() 
    {
    
    }
    
    public void addPermission(Permission perm)
    {
        this.permissions.add(perm);
    }
    
    public void removePermission(Permission perm)
    {
        this.permissions.remove(perm);
    }
    
    public boolean hasPermission(Permission perm)
    {
        return this.permissions.contains(perm);
    }
    
    public boolean hasPermission(String perm)
    {
        for (Permission permission : permissions)
        {
            if (permission.getName().equalsIgnoreCase(perm))
                return true;
        }
        return false;
    }
}
