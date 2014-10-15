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
package de.cubeisland.engine.core.permission;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.permissions.Permissible;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.core.permission.PermDefault.FALSE;
import static de.cubeisland.engine.core.permission.PermDefault.OP;

public class Permission
{
    private final String baseName;
    private final PermDefault def;

    private Permission nameParent = null;
    private final Set<Permission> parents = new HashSet<>(); // bound as children or attached
    private final Set<Permission> children = new HashSet<>(); // bound onto name.*
    private final Set<Permission> attached = new HashSet<>(); // bound onto name

    private final boolean wildcard;

    private String name = null;

    /**
     * The Base Permission for ALL CubeEngine Permissions
     */
    public static final Permission BASE = new Permission("cubeengine", FALSE, true); // cubeengine.*

    private Permission(String name, PermDefault def)
    {
       this(name, def, false);
    }

    private Permission(String name, PermDefault def, boolean wildcard)
    {
        this(null, name, def, wildcard);
    }

    private Permission(Permission permission, String name, PermDefault def)
    {
        this(permission, name, def, false);
    }

    private Permission(Permission permission, String name, PermDefault def, boolean wildcard)
    {
        this.nameParent = permission;
        this.baseName = name;
        this.def = def;
        this.wildcard = wildcard;

        this.generateName();
    }

    /**
     * Creates a new detached permission with this permission as nameParent
     *
     * @param name the name of the permission
     * @return the new permission
     */
    public Permission newPerm(String name)
    {
        return this.newPerm(name, OP);
    }

    /**
     * Creates a new detached permission with this permission as nameParent
     *
     * @param name the name of the permission
     * @param def the default
     * @return the new permission
     */
    public Permission newPerm(String name, PermDefault def)
    {
        return new Permission(this, name, def);
    }

    /**
     * Creates a new detached wildcard permission with this permission as nameParent
     *
     * @param name the name of the permission
     * @return the new permission
     */
    public Permission newWildcard(String name)
    {
        return this.newWildcard(name, FALSE);
    }

    /**
     * Creates a new detached wildcard permission with this permission as nameParent
     *
     * @param name the name of the permission
     * @param def the default
     * @return the new permission
     */
    public Permission newWildcard(String name, PermDefault def)
    {
        return new Permission(this, name, def, true);
    }

    /**
     * Creates a new permission with this permission as parent
     *
     * @param name the name of the permission
     * @return the new permission
     */
    public Permission child(String name)
    {
        return this.child(name, OP);
    }

    /**
     * Creates a new permission with this permission as parent
     *
     * @param name the name of the permission
     * @param def the default
     * @return the new permission
     */
    public Permission child(String name, PermDefault def)
    {
        return this.getChild(name, def, false);
    }

    /**
     * Creates a new wildcard permission with this permission as parent
     *
     * @param name the name of the permission
     * @return the new permission
     */
    public Permission childWildcard(String name)
    {
        return this.childWildcard(name, FALSE);
    }

    /**
     * Creates a new wildcard permission with this permission as parent
     *
     * @param name the name of the permission
     * @param def the default
     * @return the new permission
     */
    public Permission childWildcard(String name, PermDefault def)
    {
        return this.getChild(name, def, true);
    }

    private Permission getChild(String name, PermDefault def, boolean wildcard)
    {
        for (Permission child : children)
        {
            if (child.baseName.equals(name) && child.wildcard == wildcard)
            {
                if (child.def != def)
                {
                    throw new IllegalArgumentException("Duplicate Permission with different PermDefault: " + name);
                }
                return child;
            }
        }
        Permission perm;
        if (wildcard)
        {
            perm = this.newWildcard(name, def);
        }
        else
        {
            perm = this.newPerm(name, def);
        }
        checkForCircularDependency(perm);
        this.children.add(perm);
        perm.parents.add(this);
        return perm;
    }

    private void checkForCircularDependency(Permission perm)
    {
        if (this.parents.isEmpty())
        {
            return;
        }
        if (this.parents.contains(perm))
        {
            throw new IllegalStateException("Circular PermissionDependency!");
        }
        for (Permission parent : this.parents)
        {
            parent.checkForCircularDependency(perm);
        }
    }

    public void attach(Permission... toAttach)
    {
        for (Permission perm : toAttach)
        {
            checkForCircularDependency(perm);
            this.attached.add(perm);
            perm.parents.add(this);
        }
    }

    public void setParent(Permission permission)
    {
        if (nameParent != null)
        {
            nameParent.children.remove(this);
            this.parents.remove(nameParent);
        }

        permission.checkForCircularDependency(this);

        this.nameParent = permission;
        permission.children.add(this);
        this.parents.add(permission);

        this.generateName();
    }

    public boolean isAuthorized(Permissible permissible)
    {
        expectNotNull(permissible, "The permissible may not be null!");

        return permissible.hasPermission(this.getFullName());
    }

    public String getName()
    {
        if (this.name == null)
        {
            this.generateName();
        }
        return name;
    }

    protected String generateName()
    {
        String base = "";
        if (this.nameParent != null)
        {
            base = this.nameParent.generateName() + ".";
        }
        this.name = base + this.baseName;
        return this.name;
    }

    public String getFullName()
    {
        return this.getName() + (this.isWildcard() ? ".*" : "");
    }

    public String getBaseName()
    {
        return this.baseName;
    }

    public PermDefault getDefault()
    {
        return def;
    }

    public Set<Permission> getParents()
    {
        return parents;
    }

    public Set<Permission> getChildren()
    {
        return children;
    }

    public Set<Permission> getAttached()
    {
        return attached;
    }

    public boolean isWildcard()
    {
        return wildcard;
    }

    public boolean hasChildren()
    {
        return !this.children.isEmpty();
    }

    public boolean hasAttached()
    {
        return !this.attached.isEmpty();
    }

    public void detach(Permission perm)
    {
        this.children.remove(perm);
        this.attached.remove(perm);
        perm.parents.remove(this);
    }

    public Permission detachFromParents()
    {
        this.nameParent = null;
        for (Permission parent : this.parents)
        {
            parent.children.remove(this);
            parent.attached.remove(this);
        }
        this.parents.clear();
        return this;
    }

    public static Permission detachedPermission(String permission, PermDefault permDefault)
    {
        return new Permission(null, permission, permDefault);
    }
}
