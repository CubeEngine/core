package de.cubeisland.engine.module.core.sponge;

import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.ValueProvider;
import de.cubeisland.engine.modularity.core.graph.DependencyInformation;
import de.cubeisland.engine.modularity.core.graph.meta.ModuleMetadata;
import de.cubeisland.engine.module.core.permission.Permission;

public class BasePermissionProvider implements ValueProvider<Permission>
{
    private Permission base;

    public BasePermissionProvider(Permission base)
    {
        this.base = base;
    }

    @Override
    public Permission get(DependencyInformation info, Modularity modularity)
    {
        if (info instanceof ModuleMetadata)
        {
            base.childWildcard(((ModuleMetadata)info).getName());
        }
        throw new IllegalArgumentException(info.getIdentifier() + " is not a Module");
    }
}
