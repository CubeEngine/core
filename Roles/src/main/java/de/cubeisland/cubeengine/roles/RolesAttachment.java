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
