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
