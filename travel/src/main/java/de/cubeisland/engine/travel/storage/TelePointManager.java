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
package de.cubeisland.engine.travel.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.Travel;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.travel.storage.TableTeleportPoint.TABLE_TP_POINT;

public abstract class TelePointManager<T extends TeleportPoint>
{
    protected final Travel module;
    protected final DSLContext dsl;
    protected final InviteManager iManager;
    protected final Map<String, Map<String, T>> points = new HashMap<>();

    private final Class<? extends TeleportPointAttachment<T>> attachmentClass;

    public TelePointManager(Travel module, InviteManager iManager, Class<? extends TeleportPointAttachment<T>> clazz)
    {
        this.dsl = module.getCore().getDB().getDSL();
        this.module = module;
        this.iManager = iManager;
        this.attachmentClass = clazz;
    }

    public abstract void load();

    protected void addPoint(T point)
    {
        Map<String, T> map = points.get(point.getName());
        if (map == null)
        {
            map = new HashMap<>();
            points.put(point.getName(), map);
        }
        map.put(point.getOwnerName(), point);

        for (User user : point.getInvitedUsers())
        {
            this.assignTeleportPoint(point, user);
        }
    }

    public TeleportPointModel get(Long key)
    {
        return this.dsl.selectFrom(TABLE_TP_POINT).where(TABLE_TP_POINT.KEY.eq(UInteger.valueOf(key))).fetchOne();
    }

    public T find(User user, String name)
    {
        return user.attachOrGet(this.attachmentClass, this.module).findOne(name);
    }

    public boolean has(String name, User user)
    {
        return user.attachOrGet(HomeAttachment.class, this.module).getOwned(name) != null;
    }

    public int getCount(User user)
    {
        return this.dsl.selectFrom(TABLE_TP_POINT).where(TABLE_TP_POINT.OWNER.eq(user.getEntity().getKey())).fetchCount();
    }

    @SuppressWarnings("unchecked")
    public void assignTeleportPoint(T point, User user)
    {
        user.attachOrGet(this.attachmentClass, this.module).add(point);
    }

    @SuppressWarnings("unchecked")
    public void unassignTeleportPoint(T point, User user)
    {
        user.attachOrGet(this.attachmentClass, this.module).remove(point);
    }

    public abstract T create(Location location, String name, User owner, short visibility);

    public void delete(T point)
    {
        point.getModel().delete();
        this.removePoint(point);
    }

    private void removePoint(T point)
    {
        this.points.get(point.getName()).remove(point.getOwnerName());
        for (User user : CubeEngine.getUserManager().getLoadedUsers())
        {
            this.unassignTeleportPoint(point, user);
        }
    }

    public Set<T> list(boolean privates, boolean publics)
    {
        HashSet<T> set = new HashSet<>();
        for (Map<String, T> map : this.points.values())
        {
            for (T point : map.values())
            {
                if (publics && point.isPublic())
                {
                    set.add(point);
                }
                if (privates && !point.isPublic())
                {
                    set.add(point);
                }
            }
        }
        return set;
    }

    public Set<T> list(User user, boolean showOwned, boolean showPublic, boolean showInvited)
    {
        return user.attachOrGet(attachmentClass, module).list(showOwned, showPublic, showInvited);
    }

    public void massDelete(boolean privates, boolean publics)
    {
        for (T point : this.list(privates, publics))
        {
            this.delete(point);
        }
    }

    public void massDelete(User user, boolean showOwned, boolean showPublic, boolean showInvited)
    {
        for (T point : this.list(user, showOwned, showPublic, showInvited))
        {
            this.delete(point);
        }
    }

    public boolean rename(T point, String name)
    {
        Map<String, T> map = this.points.get(name);
        if (map != null && map.containsKey(point.getOwnerName()))
        {
            return false;
        }
        this.removePoint(point);

        point.setName(name);
        point.model.update();

        this.addPoint(point);
        return true;
        // There already is a point named like that for this user!
    }
}
