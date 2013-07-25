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

public interface RawDataStore
{

    public Map<String,Boolean> getRawPermissions();
    public Map<String,String> getRawMetadata();
    public Set<String> getRawAssignedRoles();

    /**
     * Returns an unmodifiable Set of the Roles assigned to this DataStore
     *
     * @return
     */
    public Set<Role> getAssignedRoles();

    public String getName();

    public void setPermission(String perm, Boolean set);
    public void setMetadata(String key, String value);
    public boolean assignRole(Role role);
    public boolean removeRole(Role role);

    public void clearPermissions();
    public void clearMetadata();
    public void clearAssignedRoles();

    public void setPermissions(Map<String,Boolean> perms);
    public void setMetadata(Map<String,String> metadata);
    public void setAssignedRoles(Set<Role> roles);

    public long getWorldID();

    public Map<String,Boolean> getAllRawPermissions();
    public Map<String,String> getAllRawMetadata();
}
