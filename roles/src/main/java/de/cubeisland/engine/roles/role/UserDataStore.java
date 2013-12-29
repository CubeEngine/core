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
import java.util.Set;

import de.cubeisland.engine.roles.Roles;
import org.jooq.types.UInteger;

public class UserDataStore
{
    protected Set<String> roles;
    protected Map<String,Boolean> permissions;
    protected Map<String,String> metadata;

    protected final RolesAttachment attachment;
    private final long worldID;
    private UInteger mirrorWorld;

    public UserDataStore(RolesAttachment attachment, long worldID, UInteger mirrorWorld)
    {
        this.attachment = attachment;
        this.worldID = worldID;
        this.mirrorWorld = mirrorWorld;

        this.roles = new HashSet<>();
        this.permissions = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    protected UInteger getMirrorWorldId()
    {
        return this.mirrorWorld;
    }

    public long getMainWorldID()
    {
        return ((Roles)this.attachment.getModule()).getRolesManager().getProvider(worldID).getMainWorldId();
    }
}
