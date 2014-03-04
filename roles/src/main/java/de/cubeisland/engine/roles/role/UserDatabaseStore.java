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
package de.cubeisland.engine.roles.role;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.World;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.roles.RoleAppliedEvent;
import de.cubeisland.engine.roles.storage.UserMetaData;
import de.cubeisland.engine.roles.storage.UserPermission;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.roles.storage.TableData.TABLE_META;
import static de.cubeisland.engine.roles.storage.TablePerm.TABLE_PERM;
import static de.cubeisland.engine.roles.storage.TableRole.TABLE_ROLE;

public class UserDatabaseStore extends ResolvedDataHolder
{
    private final World world;
    private final RolesManager manager;
    private final RolesAttachment attachment;

    public UserDatabaseStore(RolesAttachment attachment, WorldRoleProvider provider, RolesManager manager, World world)
    {
        super(manager, provider);
        this.attachment = attachment;
        this.manager = manager;
        this.world = world;

        this.loadFromDatabase();
    }

    private HashSet<String> roles;
    private Map<String, Boolean> permissions;
    private Map<String, String> metadata;

    @Override
    public String getName()
    {
        return this.attachment.getHolder().getName();
    }

    protected void loadFromDatabase()
    {
        UInteger assignedRolesMirror = getDBWorldId(this.manager.assignedRolesMirrors.get(this.world));
        Result<Record1<String>> roleFetch = manager.dsl.select(TABLE_ROLE.ROLENAME).
            from(TABLE_ROLE).where(TABLE_ROLE.USERID.eq(this.getUserID()), TABLE_ROLE.WORLDID.eq(assignedRolesMirror)).fetch();
        this.roles = new HashSet<>(roleFetch.getValues(TABLE_ROLE.ROLENAME, String.class));
        UInteger userDataMirror = getDBWorldId(this.manager.assignedUserDataMirrors.get(this.world));
        Result<Record2<String,Boolean>> permFetch = manager.dsl.select(TABLE_PERM.PERM, TABLE_PERM.ISSET).
            from(TABLE_PERM).where(TABLE_PERM.USERID.eq(this.getUserID()), TABLE_PERM.WORLDID.eq(userDataMirror)).fetch();
        this.permissions = new HashMap<>();
        for (Record2<String, Boolean> perm : permFetch)
        {
            this.permissions.put(perm.value1(), perm.value2());
        }
        Result<Record2<String,String>> metaFetch = manager.dsl.select(TABLE_META.KEY, TABLE_META.VALUE).
           from(TABLE_META).where(TABLE_META.USERID.eq(this.getUserID()), TABLE_META.WORLDID.eq(userDataMirror)).fetch();
        this.metadata = new HashMap<>();
        for (Record2<String, String> meta : metaFetch)
        {
            this.metadata.put(meta.value1(), meta.value2());
        }

        this.clearTempMetadata();
        this.clearTempPermissions();
        this.clearTempRoles();

        this.makeDirty();
    }

    private UInteger getUserID()
    {
        return this.attachment.getHolder().getEntity().getKey();
    }

    @Override
    public PermissionValue setPermission(String perm, PermissionValue set)
    {
        if (set == PermissionValue.RESET)
        {
            manager.dsl.delete(TABLE_PERM).where(TABLE_PERM.USERID.eq(this.getUserID()),
                                                 TABLE_PERM.WORLDID.eq(getDBWorldId(this.manager.assignedUserDataMirrors.get(world))),
                                                 TABLE_PERM.PERM.eq(perm)).execute();
        }
        else
        {
            UserPermission userPerm = manager.dsl.newRecord(TABLE_PERM).newPerm(this.getUserID(),
                getDBWorldId(this.manager.assignedUserDataMirrors.get(world)), perm, set);
            manager.dsl.insertInto(TABLE_PERM).set(userPerm).onDuplicateKeyUpdate().set(userPerm).execute();
        }
        this.makeDirty();
        if (set == PermissionValue.RESET)
        {
            return PermissionValue.of(this.permissions.remove(perm));
        }
        return PermissionValue.of(this.permissions.put(perm, set == PermissionValue.TRUE));
    }

    @Override
    public String setMetadata(String key, String value)
    {
        UserMetaData userMeta = manager.dsl.newRecord(TABLE_META).newMeta(this.getUserID(),
            getDBWorldId(this.manager.assignedUserDataMirrors.get(world)), key, value);
        manager.dsl.insertInto(TABLE_META).set(userMeta).onDuplicateKeyUpdate().set(userMeta).execute();
        this.makeDirty();
        return this.metadata.put(key, value);
    }

    @Override
    public boolean removeMetadata(String key)
    {
        manager.dsl.delete(TABLE_META).where(TABLE_META.USERID.eq(this.getUserID()),
                                             TABLE_META.WORLDID.eq(getDBWorldId(this.manager.assignedUserDataMirrors.get(world))),
                                             TABLE_META.KEY.eq(key)).execute();
        this.makeDirty();
        return this.metadata.remove(key) != null;
    }

    @Override
    public boolean assignRole(Role role)
    {
        if (this.roles.contains(role.getName()))
        {
            return false;
        }
        manager.dsl.newRecord(TABLE_ROLE).newAssignedRole(this.getUserID(),
            getDBWorldId(this.manager.assignedUserDataMirrors.get(world)), role.getName()).insert();
        this.removeRoles(role.getRoles());
        if (this.roles.isEmpty())
        {
            for (Role defRole : ((WorldRoleProvider)provider).getDefaultRoles())
            {
                this.removeTempRole(defRole);
            }
        }
        this.makeDirty();
        return this.roles.add(role.getName());
    }

    private void removeRoles(Set<Role> parentRoles)
    {
        for (Role role : parentRoles)
        {
            this.removeRoles(role.getRoles());
            this.removeRole(role);
        }
    }

    @Override
    public boolean removeRole(Role role)
    {
        if (this.roles.contains(role.getName()))
        {
            manager.dsl.delete(TABLE_ROLE).where(TABLE_ROLE.USERID.eq(this.getUserID()),
                                                 TABLE_ROLE.WORLDID.eq(getDBWorldId(this.manager.assignedRolesMirrors.get(world))),
                                                 TABLE_ROLE.ROLENAME.eq(role.getName())).execute();
            this.makeDirty();
            return this.roles.remove(role.getName());
        }
        return false;
    }

    @Override
    public void clearPermissions()
    {
        if (!this.permissions.isEmpty())
        {
            manager.dsl.delete(TABLE_PERM).where(TABLE_PERM.USERID.eq(this.getUserID()),
                TABLE_PERM.WORLDID.eq(getDBWorldId(this.manager.assignedUserDataMirrors.get(world)))).execute();
            this.makeDirty();
            this.permissions.clear();
        }
    }

    @Override
    public void clearMetadata()
    {
        if (!this.metadata.isEmpty())
        {
            manager.dsl.delete(TABLE_META).where(TABLE_META.USERID.eq(this.getUserID()),
                TABLE_META.WORLDID.eq(getDBWorldId(this.manager.assignedUserDataMirrors.get(world)))).execute();
            this.makeDirty();
            this.metadata.clear();
        }
    }

    @Override
    public void clearRoles()
    {
        if (!this.roles.isEmpty())
        {
            manager.dsl.delete(TABLE_ROLE).where(TABLE_ROLE.USERID.eq(this.getUserID()),
                TABLE_ROLE.WORLDID.eq(getDBWorldId(this.manager.assignedUserDataMirrors.get(world)))).execute();
            this.makeDirty();
            this.roles.clear();
        }
    }

    @Override
    public Map<String, Boolean> getRawPermissions()
    {
        return Collections.unmodifiableMap(this.permissions);
    }

    @Override
    public Map<String, String> getRawMetadata()
    {
        return Collections.unmodifiableMap(this.metadata);
    }

    @Override
    public Set<String> getRawRoles()
    {
        return Collections.unmodifiableSet(this.roles);
    }

    private UInteger getDBWorldId(World world)
    {
        return UInteger.valueOf(this.module.getCore().getWorldManager().getWorldId(world));
    }

    /**
     * Sets all permissions metadata and roles for the players world
     */
    public void apply()
    {
        User user = this.attachment.getHolder();
        if (user.isOnline())
        {
            this.calculate(new Stack<String>());
            if (!Bukkit.getServer().getOnlineMode() && this.module.getConfiguration().doNotAssignPermIfOffline && !user.isLoggedIn())
            {
                if (!attachment.isOfflineMsgReceived())
                {
                    user.sendTranslated("&cThe server is currently running in offline-mode. Permissions will not be applied until logging in! Contact an Administrator if you think this is an error.");
                    attachment.setOfflineMsgReceived(true);
                }
                this.module.getLog().warn("Role-permissions not applied! Server is running in unsecured offline-mode!");
                return;
            }
            user.setPermission(this.getResolvedPermissions());
            for (Role assignedRole : this.getRoles())
            {
                this.module.getLog().debug(" - {}", assignedRole.getName());
            }
        }
        this.module.getCore().getEventManager().fireEvent(new RoleAppliedEvent(module, user, this.attachment));
        // else user is offline ignore
    }

    @Override
    public void calculate(Stack<String> roleStack)
    {
        if (this.isDirty())
        {
            if (this.getRawRoles().isEmpty() && this.getRawTempRoles().isEmpty())
            {
                this.module.getLog().debug("{} had no roles applying default-roles", this.attachment.getHolder().getName());

                for (Role role : ((WorldRoleProvider)this.provider).getDefaultRoles())
                {
                    this.assignTempRole(role);
                }
            }
            super.calculate(roleStack);
            this.module.getLog().debug("Role for {} calculated", this.attachment.getHolder().getName());
        }
    }

    @Override
    protected void roleMissing(String roleName, boolean temp)
    {
        super.roleMissing(roleName, temp);
        if (temp)
        {
            this.attachment.getHolder().sendTranslated("&cYour temporary role &6%s&c is not available in &6%s", roleName, provider.getMainWorld());
            this.attachment.getHolder().sendTranslated("&4You should report this to an administrator!");
        }
        else
        {
            this.attachment.getHolder().sendTranslated("&cYour role &6%s&c is not available in &6%s", roleName, provider.getMainWorld());
            this.attachment.getHolder().sendTranslated("&4You should report this to an administrator!");
        }
    }

    /* TODO mass set
    @Override
    public void setRawPermissions(Map<String, Boolean> perms)
    {
        this.clearPermissions();
        Set<UserPermission> toInsert = new HashSet<>();
        for (Entry<String, Boolean> entry : perms.entrySet())
        {
            toInsert.add(manager.dsl.newRecord(TABLE_PERM)
                                .newPerm(this.getUserID(), this.getMirrorWorldId(), entry.getKey(), entry.getValue()));
        }
        manager.dsl.batchInsert(toInsert).execute();
        super.setRawPermissions(perms);
    }

    @Override
    public void setRawMetadata(Map<String, String> metadata)
    {
        this.clearMetadata();
        Set<UserMetaData> toInsert = new HashSet<>();
        for (Entry<String, String> entry : metadata.entrySet())
        {
            toInsert.add(manager.dsl.newRecord(TABLE_META).newMeta(this.getUserID(), this.getMirrorWorldId(), entry.getKey(), entry
                .getValue()));
        }
        manager.dsl.batchInsert(toInsert).execute();
        super.setRawMetadata(metadata);
    }

    @Override
    public void setRawRoles(Set<Role_old> roles)
    {
        this.clearRoles();
        Set<AssignedRole> toInsert = new HashSet<>();
        for (Role_old role : roles)
        {
            toInsert.add(manager.dsl.newRecord(TABLE_ROLE).newAssignedRole(this.getUserID(), this.getMirrorWorldId(), role.getName()));
        }
        manager.dsl.batchInsert(toInsert).execute();
        super.setRawRoles(roles);
    }
*/
}
