package de.cubeisland.cubeengine.core.permission;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author Phillip Schichtel
 */
public interface Permission
{
    public boolean isAuthorized(Player player);
    public String getPermission();
    public PermissionDefault getPermissionDefault();
}
