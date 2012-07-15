package de.cubeisland.cubeengine.core.permission;

/**
 *
 * @author Phillip Schichtel
 */
public interface PermissionRegistration
{
    public void registerPermission(Permission permission);
    public void registerPermissions(Permission[] permissions);
}
