package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.storage.UserMetaData;
import de.cubeisland.cubeengine.roles.storage.UserMetaDataManager;
import de.cubeisland.cubeengine.roles.storage.UserPermission;
import de.cubeisland.cubeengine.roles.storage.UserPermissionsManager;
import gnu.trove.map.hash.THashMap;

public class UserSpecificRole extends MergedRole
{
    public final User user;
    private Roles module;
    private UserMetaDataManager umManager;
    private UserPermissionsManager upManager;
    private long worldId;

    public UserSpecificRole(Roles module, User user, long worldId, THashMap<String, Boolean> perms, THashMap<String, String> meta)
    {
        super(user.getName(), perms, meta);
        this.user = user;
        this.module = module;
        this.worldId = worldId;
        this.umManager = module.getDbUserMeta();
        this.upManager = module.getDbUserPerm();
    }

    @Override
    public void setPermission(String perm, Boolean set)
    {
        if (set == null)
        {
            upManager.deleteByKey(new Triplet<Long, Long, String>(user.key, worldId, perm));
        }
        else
        {
            UserPermission up = new UserPermission(user.key, worldId, perm, set);
            upManager.merge(up);

        }
        this.module.getManager().reloadAllRolesAndApply(user, user.getPlayer());
    }

    @Override
    public void setMetaData(String key, String value)
    {
        if (value == null)
        {
            umManager.deleteByKey(new Triplet<Long, Long, String>(user.key, worldId, key));
        }
        else
        {
            umManager.merge(new UserMetaData(user.key, worldId, key, value));
        }
        this.module.getManager().reloadAllRolesAndApply(user, user.getPlayer());
    }

    @Override
    public void clearMetaData()
    {
        umManager.clearByUser(user.key);
        this.module.getManager().reloadAllRolesAndApply(user, user.getPlayer());
    }
}
