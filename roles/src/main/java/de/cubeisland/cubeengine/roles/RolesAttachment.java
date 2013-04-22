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
package de.cubeisland.cubeengine.roles;

import java.util.Map;
import java.util.Set;

import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.user.UserAttachment;
import de.cubeisland.cubeengine.roles.role.UserSpecificRole;

import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;
import gnu.trove.set.hash.TLinkedHashSet;

public class RolesAttachment extends UserAttachment
{
    private TLongObjectHashMap<UserSpecificRole> roleContainer;
    private Map<String, RoleMetaData> metaData;
    private Long currentWorldId;
    private TLongObjectHashMap<TLinkedHashSet<ConfigRole>> temporaryRoles;

    public void setRoleContainer(TLongObjectHashMap<UserSpecificRole> roleContainer) {
        this.roleContainer = roleContainer;
        this.getModule().getLog().log(LogLevel.DEBUG, this.getHolder().getName() + ": RoleContainer attached!");
    }

    public TLongObjectHashMap<UserSpecificRole> getRoleContainer() {
        return roleContainer;
    }

    public boolean hasRoleContainer() {
        return this.roleContainer != null;
    }

    public void removeRoleContainer() {
        if (roleContainer != null)
        {
            this.roleContainer = null;
            this.getModule().getLog().log(LogLevel.DEBUG, this.getHolder().getName() + ": RoleContainer removed!");
        }
    }

    public void setMetaData(Map<String, RoleMetaData> metaData) {
        this.metaData = metaData;
    }

    public Map<String, RoleMetaData> getMetaData() {
        return metaData;
    }

    public Long getCurrentWorldId() {
        return currentWorldId;
    }

    public void setCurrentWorldId(Long currentWorldId) {
        this.currentWorldId = currentWorldId;
    }

    public boolean hasTemporaryRoles(long worldId)
    {
        if (this.temporaryRoles == null || this.temporaryRoles.isEmpty()) return false;
        return this.temporaryRoles.get(worldId) != null && !this.temporaryRoles.get(worldId).isEmpty();
    }

    public Set<ConfigRole> getTemporaryRoles(long worldId)
    {
        return temporaryRoles.get(worldId);
    }

    public void addTemporaryRole(long worldID, ConfigRole role)
    {
        if (this.temporaryRoles == null)
        {
            this.temporaryRoles = new TLongObjectHashMap<TLinkedHashSet<ConfigRole>>();
        }
        TLinkedHashSet<ConfigRole> configRoles = this.temporaryRoles.get(worldID);
        if (configRoles == null)
        {
            configRoles = new TLinkedHashSet<ConfigRole>();
            this.temporaryRoles.put(worldID,configRoles);
        }
        configRoles.add(role);
    }

    public void replaceDirtyTemporaryRoles(final RoleManager manager)
    {
        if (this.temporaryRoles != null)
        {
            this.temporaryRoles.forEachEntry(new TLongObjectProcedure<TLinkedHashSet<ConfigRole>>()
            {
                @Override
                public boolean execute(long a, TLinkedHashSet<ConfigRole> b)
                {
                    TObjectHashIterator<ConfigRole> iterator = b.iterator();
                    while (iterator.hasNext())
                    {
                        ConfigRole next = iterator.next();
                        if (next.isDirty())
                        {
                            b.remove(next);
                            next = manager.getRoleInWorld(a,next.getName());
                            if (next != null)
                            {
                                b.add(next);
                            }
                            // else that role got removed!
                        }
                    }
                    return true;
                }
            });
        }
    }
}
