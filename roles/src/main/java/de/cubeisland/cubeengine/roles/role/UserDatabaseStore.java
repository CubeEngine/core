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

import java.util.Map;
import java.util.Set;

import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.roles.storage.AssignedRole;
import de.cubeisland.cubeengine.roles.storage.AssignedRoleManager;
import de.cubeisland.cubeengine.roles.storage.UserMetaData;
import de.cubeisland.cubeengine.roles.storage.UserMetaDataManager;
import de.cubeisland.cubeengine.roles.storage.UserPermission;
import de.cubeisland.cubeengine.roles.storage.UserPermissionsManager;

public class UserDatabaseStore extends UserDataStore
{
    private AssignedRoleManager rm;
    private UserMetaDataManager mdm;
    private UserPermissionsManager pm;

    public UserDatabaseStore(RolesAttachment attachment, long worldID, RolesManager manager)
    {
        super(attachment,worldID);
        this.rm = manager.rm;
        this.mdm = manager.mdm;
        this.pm = manager.pm;
        // TODO load from database
    }

    @Override
    public void setPermission(String perm, Boolean set)
    {
        if (set == null)
        {
            pm.deleteByKey(new Triplet<Long, Long, String>(this.getUserID(), this.worldID, perm));
        }
        else
        {
            pm.merge(new UserPermission(this.getUserID(),  this.worldID, perm, set));
        }
        super.setPermission(perm,set);
    }

    @Override
    public void setMetadata(String key, String value)
    {
        if (value == null)
        {
            mdm.deleteByKey(new Triplet<Long, Long, String>(this.getUserID(), this.worldID, key));
        }
        else
        {
            mdm.merge(new UserMetaData(this.getUserID(), this.worldID, key, value));
        }
        super.setMetadata(key,value);
    }

    @Override
    public boolean addParent(Role role)
    {
        if (this.roles.contains(role.getName()))
        {
            return false;
        }
        this.rm.merge(new AssignedRole(this.getUserID(),this.worldID,role.getName()));
        return super.addParent(role);
    }

    @Override
    public boolean removeParent(Role role)
    {
        if (this.roles.contains(role.getName()))
        {
            this.rm.delete(this.getUserID(),role.getName(),this.worldID);
            return super.removeParent(role);
        }
        return false;
    }

    @Override
    public void clearPermissions()
    {
        if (!this.permissions.isEmpty())
        {
            this.pm.removeByUserAndWorld(this.getUserID(),this.worldID);
            super.clearPermissions();
        }
    }

    @Override
    public void clearMetadata()
    {
        mdm.clearByUserAndWorld(this.getUserID(),this.worldID);
        super.clearMetadata();
    }

    @Override
    public void clearParents()
    {
        rm.clearByUserAndWorld(this.getUserID(),this.worldID);
        super.clearParents();
    }

    @Override
    public void setPermissions(Map<String, Boolean> perms)
    {
        this.clearPermissions();
        // TODO batch set perms
        super.setPermissions(perms);
    }

    @Override
    public void setMetadata(Map<String, String> metadata)
    {
        this.clearMetadata();
        //TODO batch set meta
        super.setMetadata(metadata);
    }

    @Override
    public void setParents(Set<Role> roles)
    {
        this.clearParents();
        // TODO batch set parents
        super.setParents(roles);
    }
}
