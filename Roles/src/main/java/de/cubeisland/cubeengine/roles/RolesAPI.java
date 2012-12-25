package de.cubeisland.cubeengine.roles;

import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.role.RoleManager;
import de.cubeisland.cubeengine.roles.role.RoleMetaData;
import de.cubeisland.cubeengine.roles.role.UserSpecificRole;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.bukkit.World;

public class RolesAPI
{
    private Roles module;
    private WorldManager worldManager;
    private RoleManager manager;

    public RolesAPI(Roles module)
    {
        this.module = module;
        this.worldManager = module.getCore().getWorldManager();


    }

    public String getMetaData(User user, World world, String metaKey)
    {
        if (user == null || world == null || metaKey == null)
        {
            return null;
        }
        TLongObjectHashMap<UserSpecificRole> roleContainer = user.getAttribute(this.module, "roleContainer");
        if (roleContainer == null)
        {
            if (user.isOnline())
            {
                throw new IllegalStateException("User has no rolecontainer!");
            }
            else
            {
                this.manager.preCalculateRoles(user.getName(), true);
                roleContainer = user.getAttribute(this.module, "roleContainer");
            }
        }
        UserSpecificRole role = roleContainer.get(this.worldManager.getWorldId(world));
        RoleMetaData data = role.getMetaData().get(metaKey);
        if (data == null)
        {
            return null;
        }
        return data.getValue();
    }
}
