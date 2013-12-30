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

import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.cubeisland.engine.roles.role.resolved.ResolvedMetadata;
import de.cubeisland.engine.roles.role.resolved.ResolvedPermission;

public interface DataStore
{
    String getName();

    Map<String,Boolean> getRawPermissions();
    Map<String,String> getRawMetadata();
    Set<String> getRawRoles();

    // TODO setRawPerms(Map) etc.

    PermissionValue setPermission(String perm, PermissionValue set);
    String setMetadata(String key, String value);
    boolean removeMetadata(String key);
    boolean assignRole(Role role);
    boolean removeRole(Role role);

    void clearPermissions();
    void clearMetadata();
    void clearRoles();

    Map<String, Boolean> getRawTempPermissions();
    Map<String, String> getRawTempMetaData();
    Set<String> getRawTempRoles();

    PermissionValue setTempPermission(String perm, PermissionValue set);
    String setTempMetadata(String key, String value);
    boolean removeTempMetadata(String key);
    boolean assignTempRole(Role role);
    boolean removeTempRole(Role role);

    void clearTempPermissions();
    void clearTempMetadata();
    void clearTempRoles();

    Map<String, Boolean> getAllRawPermissions();
    Map<String, String> getAllRawMetadata();

    void calculate(Stack<String> roleStack);
    void makeDirty();

    Map<String, ResolvedPermission> getPermissions();
    Map<String, ResolvedMetadata> getMetadata();
    Set<Role> getRoles();

    boolean inheritsFrom(Role other);

    public enum PermissionValue
    {
        TRUE, FALSE,
        RESET;

        public static PermissionValue of(Boolean set)
        {
            return set == null ? RESET : set ? TRUE : FALSE;
        }
    }
}
