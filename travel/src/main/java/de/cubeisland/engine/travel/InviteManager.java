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
package de.cubeisland.engine.travel;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.storage.TeleportInvite;
import de.cubeisland.engine.travel.storage.TeleportPointModel;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.travel.storage.TableInvite.TABLE_INVITE;

public class InviteManager
{
    private final Travel module;
    private final DSLContext dsl;
    private final Collection<TeleportInvite> invites;
    private final Map<TeleportPointModel, Set<UInteger>> cachedInvites;

    public InviteManager(Database database, Travel module)
    {
        this.dsl = database.getDSL();
        this.module = module;
        this.cachedInvites = new HashMap<>();
        this.invites = this.dsl.selectFrom(TABLE_INVITE).fetch(); // TODO this can be a big query :S
    }

    public void invite(TeleportPointModel tPP, User user)
    {
        TeleportInvite invite = this.dsl.newRecord(TABLE_INVITE).newInvite(tPP.getKey(), user.getEntity().getKey());
        this.invites.add(invite);
        invite.insert();
    }

    /**
     * All users invited to a teleport point.
     *
     * @return A set of User names invited to the home
     */
    public Set<UInteger> getInvited(TeleportPointModel tPP)
    {
        if (this.cachedInvites.containsKey(tPP))
        {
            return this.cachedInvites.get(tPP);
        }
        Set<UInteger> keys = new HashSet<>();
        for (TeleportInvite tpI : getInvites(tPP))
        {
            keys.add(tpI.getUserkey());
        }
        this.cachedInvites.put(tPP, keys);
        return keys;
    }

    /**
     * All teleport invites that contains the user.
     * This can be used to get all teleport points an user is invited to
     *
     * @return A set of TeleportInvites
     */
    public Set<TeleportInvite> getInvites(User user)
    {
        Set<TeleportInvite> invites = new HashSet<>();
        for (TeleportInvite invite : this.invites)
        {
            if (invite.getUserkey().equals(user.getEntity().getKey()))
            {
                invites.add(invite);
            }
        }
        return invites;
    }

    /**
     * All teleport invites that contains the teleport point
     * This can be used to get all users that is invited to a teleport point
     *
     * @return A set of TeleportInvites
     */
    public Set<TeleportInvite> getInvites(TeleportPointModel tPP)
    {
        Set<TeleportInvite> invites = new HashSet<>();
        for (TeleportInvite invite : this.invites)
        {
            if (invite.getTeleportpoint().equals(tPP.getKey()))
            {
                invites.add(invite);
            }
        }
        return invites;
    }

    /**
     * Update the local changes to the database
     *
     * @param tPP        The local teleport point
     * @param newInvited The users that is currently invited to the teleportpoint locally
     */
    public void updateInvited(TeleportPointModel tPP, Set<UInteger> newInvited)
    {
        Set<TeleportInvite> invites = getInvites(tPP);
        Set<UInteger> invitedUsers = new HashSet<>();
        for (UInteger uid : newInvited)
        {
            invitedUsers.add(this.module.getCore().getUserManager().getUser(uid).getEntity().getKey());
        }
        for (TeleportInvite invite : invites)
        {
            if (invitedUsers.contains(invite.getUserkey()))
            {
                invitedUsers.remove(invite.getUserkey()); // already invited
            }
            else
            {
                invite.delete(); // no longer invited
            }
        }
        for (UInteger invitedUser : invitedUsers)
        {
            this.dsl.newRecord(TABLE_INVITE).newInvite(tPP.getKey(), invitedUser).insert(); // not yet invited
        }
    }

    public void removeInvites(TeleportPoint tPP)
    {
        this.updateInvited(tPP.getModel(), new HashSet<UInteger>());
    }
}
