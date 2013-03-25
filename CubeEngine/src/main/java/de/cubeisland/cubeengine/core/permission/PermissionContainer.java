package de.cubeisland.cubeengine.core.permission;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import de.cubeisland.cubeengine.core.module.Module;

import gnu.trove.set.hash.THashSet;

public abstract class PermissionContainer
{
    private final PermissionManager permissionManager;
    private final Module module;
    public static final NewPermission BASE = new NewPermission(true,"cubeengine");

    protected PermissionContainer(Module module)
    {
        this.permissionManager = module.getCore().getPermissionManager();
        this.module = module;
    }

    /**
     * Nulls all static fields in this
     */
    public void unload()
    {
        for (Field field : this.getClass().getDeclaredFields())
        {
            int mask = field.getModifiers();
            if ((((mask & Modifier.STATIC) == Modifier.STATIC)))
            {
                if (NewPermission.class.isAssignableFrom(field.getType()))
                {
                    try
                    {
                        field.set(this,null);
                    }
                    catch (IllegalAccessException e)
                    {
                    }
                }
            }
        }
    }

    private Set<NewPermission> getPermissions()
    {
        THashSet<NewPermission> perms = new THashSet<NewPermission>();
        for (Field field : this.getClass().getFields())
        {
            int mask = field.getModifiers();
            if ((((mask & Modifier.STATIC) == Modifier.STATIC)))
            {
                if (NewPermission.class.isAssignableFrom(field.getType()))
                {
                    try
                    {
                        NewPermission perm = (NewPermission)field.get(this);
                        if (perm.canRegister)
                        {
                            perms.add(perm);
                        }
                    }
                    catch (IllegalAccessException e)
                    {
                    }
                }
            }
        }
        return perms;
    }

    public void registerAllPermissions()
    {
        String prefix = "cubeengine." + this.module.getId()+ ".";
        for (NewPermission perm : getPermissions())
        {
            System.out.print(" - " + perm.getPermission());
            if (!perm.getPermission().startsWith(prefix))
            {
                //throw new IllegalArgumentException("Permissions must start with 'cubeengine.<module>' !");
            }


            //TODO registering
        }
    }
}
