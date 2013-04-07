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

import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.user.UserAttachment;
import de.cubeisland.cubeengine.roles.role.RoleMetaData;
import de.cubeisland.cubeengine.roles.role.UserSpecificRole;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.Map;

public class RolesAttachment extends UserAttachment
{
    private TLongObjectHashMap<UserSpecificRole> roleContainer;
    private Map<String, RoleMetaData> metaData;
    private Long currentWorldId;

    public void setRoleContainer(TLongObjectHashMap<UserSpecificRole> roleContainer) {
        this.roleContainer = roleContainer;
        this.getModule().getLog().log(LogLevel.DEBUG, "RoleContainer attached for " + this.getHolder().getName());
    }

    public TLongObjectHashMap<UserSpecificRole> getRoleContainer() {
        return roleContainer;
    }

    public boolean hasRoleContainer() {
        return this.roleContainer != null;
    }

    public void removeRoleContainer() {
        this.roleContainer = null;
        this.getModule().getLog().log(LogLevel.DEBUG, "RoleContainer removed for " + this.getHolder().getName());
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
}
