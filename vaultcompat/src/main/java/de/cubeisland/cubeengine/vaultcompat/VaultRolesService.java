package de.cubeisland.cubeengine.vaultcompat;

import de.cubeisland.cubeengine.roles.Roles;

import net.milkbowl.vault.permission.Permission;

public class VaultRolesService extends Permission
{
    private final Vaultcompat compat;
    private final Roles roles;

    public VaultRolesService(Vaultcompat compat, Roles roles)
    {
        this.compat = compat;
        this.roles = roles;
    }

    public Roles getRoles()
    {
        return roles;
    }

    @Override
    public String getName()
    {
        return "CubeEngine:Roles";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isEnabled()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasSuperPermsCompat()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean playerHas(String s, String s2, String s3)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean playerAdd(String s, String s2, String s3)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean playerRemove(String s, String s2, String s3)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean groupHas(String s, String s2, String s3)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean groupAdd(String s, String s2, String s3)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean groupRemove(String s, String s2, String s3)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean playerInGroup(String s, String s2, String s3)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean playerAddGroup(String s, String s2, String s3)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean playerRemoveGroup(String s, String s2, String s3)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String[] getPlayerGroups(String s, String s2)
    {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getPrimaryGroup(String s, String s2)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String[] getGroups()
    {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
