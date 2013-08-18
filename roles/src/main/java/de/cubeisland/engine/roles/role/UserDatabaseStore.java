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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.cubeisland.engine.roles.storage.AssignedRole;
import de.cubeisland.engine.roles.storage.UserMetaData;
import de.cubeisland.engine.roles.storage.UserPermission;
import gnu.trove.map.hash.THashMap;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.roles.storage.TableData.TABLE_META;
import static de.cubeisland.engine.roles.storage.TablePerm.TABLE_PERM;
import static de.cubeisland.engine.roles.storage.TableRole.TABLE_ROLE;

public class UserDatabaseStore extends UserDataStore
{
    private RolesManager manager;

    public UserDatabaseStore(RolesAttachment attachment, long worldID, RolesManager manager)
    {
        super(attachment, worldID);
        this.manager = manager;
        this.loadFromDatabase();
    }

    protected void loadFromDatabase()
    {
        UInteger assignedRolesMirror = UInteger.valueOf(this.manager.assignedRolesMirrors.get(this.getMainWorldID()));
        Result<Record1<String>> roleFetch = manager.dsl.select(TABLE_ROLE.ROLENAME).from(TABLE_ROLE)
                    .where(TABLE_ROLE.USERID.eq(this.getUserID()),
                           TABLE_ROLE.WORLDID.eq(assignedRolesMirror)).fetch();
        this.roles = new HashSet<>(roleFetch.getValues(TABLE_ROLE.ROLENAME, String.class));
        UInteger userDataMirror = UInteger.valueOf(this.manager.assignedUserDataMirrors.get(this.getMainWorldID()));

        Result<Record2<String,Byte>> permFetch = manager.dsl.select(TABLE_PERM.PERM, TABLE_PERM.ISSET).from(TABLE_PERM)
               .where(TABLE_PERM.USERID.eq(this.getUserID()),
                      TABLE_PERM.WORLDID.eq(userDataMirror)).fetch();
        this.permissions = new HashMap<>();
        for (Record2<String, Byte> record2 : permFetch)
        {
            this.permissions.put(record2.value1(), record2.value2() == 1);
        }
        Result<Record2<String,String>> metaFetch = manager.dsl.select(TABLE_META.KEY, TABLE_META.VALUE).from(TABLE_META)
                                                        .where(TABLE_META.USERID.eq(this.getUserID()),
                                                               TABLE_META.WORLDID.eq(userDataMirror)).fetch();
        this.metadata = new HashMap<>();
        for (Record2<String, String> record2 : metaFetch)
        {
            this.metadata.put(record2.value1(), record2.value2());
        }
    }

    @Override
    public void setPermission(String perm, Boolean set)
    {
        if (set == null)
        {
            manager.dsl.delete(TABLE_PERM).where(TABLE_PERM.USERID.eq(this.getUserID()),
                                                 TABLE_PERM.WORLDID.eq(UInteger.valueOf(this.worldID)),
                                                 TABLE_PERM.PERM.eq(perm)).execute();
        }
        else
        {
            UserPermission userPerm = manager.dsl.newRecord(TABLE_PERM).newPerm(this.getUserID(), this.worldID, perm, set);
            manager.dsl.insertInto(TABLE_PERM).set(userPerm).onDuplicateKeyUpdate().set(userPerm).execute();
        }
        super.setPermission(perm,set);
    }

    @Override
    public void setMetadata(String key, String value)
    {
        if (value == null)
        {
            manager.dsl.delete(TABLE_META).where(TABLE_META.USERID.eq(this.getUserID()),
                                                 TABLE_META.WORLDID.eq(UInteger.valueOf(this.worldID)),
                                                 TABLE_META.KEY.eq(key)).execute();
        }
        else
        {
            UserMetaData userMeta = manager.dsl.newRecord(TABLE_META).newMeta(this.getUserID(), this.worldID, key, value);
            manager.dsl.insertInto(TABLE_META).set(userMeta).onDuplicateKeyUpdate().set(userMeta).execute();
        }
        super.setMetadata(key,value);
    }

    @Override
    public boolean assignRole(Role role)
    {
        String roleName = role.getName();
        if (role.isGlobal())
        {
            roleName = "g:" + roleName;
        }
        if (this.roles.contains(roleName))
        {
            return false;
        }
        manager.dsl.newRecord(TABLE_ROLE).newAssignedRole(this.getUserID(), this.worldID, roleName).insert();
        this.removeAssignedRoles(role.getAssignedRoles());
        if (this.roles.isEmpty())
        {
            attachment.removeDefaultRoles(this.worldID);
        }
        return super.assignRole(role);
    }


    private void removeAssignedRoles(Set<Role> roles)
    {
        for (Role role : roles)
        {
            this.removeAssignedRoles(role.getAssignedRoles());
            this.removeRole(role);
        }
    }

    @Override
    public boolean removeRole(Role role)
    {
        String roleName = role.getName();
        if (role.isGlobal())
        {
            roleName = "g:" + roleName;
        }
        if (this.roles.contains(roleName))
        {
            manager.dsl.delete(TABLE_ROLE).where(TABLE_ROLE.USERID.eq(this.getUserID()),
                                                 TABLE_ROLE.WORLDID.eq(UInteger.valueOf(this.worldID)),
                                                 TABLE_ROLE.ROLENAME.eq(roleName)).execute();
            return super.removeRole(role);
        }
        return false;
    }

    @Override
    public void clearPermissions()
    {
        if (!this.permissions.isEmpty())
        {
            manager.dsl.delete(TABLE_PERM).where(TABLE_PERM.USERID.eq(this.getUserID()),
                                 TABLE_PERM.WORLDID.eq(UInteger.valueOf(this.worldID))).execute();
            super.clearPermissions();
        }
    }

    @Override
    public void clearMetadata()
    {
        manager.dsl.delete(TABLE_META).where(TABLE_META.USERID.eq(this.getUserID()),
                             TABLE_META.WORLDID.eq(UInteger.valueOf(this.worldID))).execute();
        super.clearMetadata();
    }

    @Override
    public void clearAssignedRoles()
    {
        manager.dsl.delete(TABLE_ROLE).where(TABLE_ROLE.USERID.eq(this.getUserID()),
                                             TABLE_ROLE.WORLDID.eq(UInteger.valueOf(this.worldID))).execute();
        super.clearAssignedRoles();
    }

    @Override
    public void setPermissions(Map<String, Boolean> perms)
    {
        this.clearPermissions();
        Set<UserPermission> toInsert = new HashSet<>();
        for (Entry<String, Boolean> entry : perms.entrySet())
        {
            toInsert.add(manager.dsl.newRecord(TABLE_PERM)
                                .newPerm(this.getUserID(), this.worldID, entry.getKey(), entry.getValue()));
        }
        manager.dsl.batchInsert(toInsert).execute();
        super.setPermissions(perms);
    }

    @Override
    public void setMetadata(Map<String, String> metadata)
    {
        this.clearMetadata();
        Set<UserMetaData> toInsert = new HashSet<>();
        for (Entry<String, String> entry : metadata.entrySet())
        {
            toInsert.add(manager.dsl.newRecord(TABLE_META).newMeta(this.getUserID(), this.worldID, entry.getKey(), entry
                .getValue()));
        }
        manager.dsl.batchInsert(toInsert).execute();
        super.setMetadata(metadata);
    }

    @Override
    public void setAssignedRoles(Set<Role> roles)
    {
        this.clearAssignedRoles();
        Set<AssignedRole> toInsert = new HashSet<>();
        for (Role role : roles)
        {
            toInsert.add(manager.dsl.newRecord(TABLE_ROLE).newAssignedRole(this.getUserID(), this.worldID, role.getName()));
        }
        manager.dsl.batchInsert(toInsert).execute();
        super.setAssignedRoles(roles);
    }

    @Override
    public Map<String, Boolean> getAllRawPermissions()
    {
        Map<String,Boolean> result = new THashMap<>();
        for (Role assignedRole : this.attachment.getResolvedData(this.worldID).assignedRoles)
        {
            result.putAll(assignedRole.getAllRawPermissions());
        }
        result.putAll(this.getRawPermissions());
        return result;
    }

    @Override
    public Map<String, String> getAllRawMetadata()
    {
        Map<String,String> result = new THashMap<>();
        for (Role assignedRole : this.attachment.getResolvedData(this.worldID).assignedRoles)
        {
            result.putAll(assignedRole.getAllRawMetadata());
        }
        result.putAll(this.getRawMetadata());
        return result;
    }
}
