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

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Location;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.travel.storage.TeleportPointModel;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.travel.storage.TableTeleportPoint.TABLE_TP_POINT;

public abstract class TelePointManager<T extends TeleportPoint>
{
    protected final Travel module;
    protected final DSLContext dsl;
    protected final InviteManager iManager;
    protected final Map<String, Map<String, T>> points = new HashMap<>();

    public TelePointManager(Travel module, InviteManager iManager)
    {
        this.dsl = module.getCore().getDB().getDSL();
        this.module = module;
        this.iManager = iManager;
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
        map.put(point.getOwnerName().toLowerCase(), point);
    }

    public TeleportPointModel get(Long key)
    {
        return this.dsl.selectFrom(TABLE_TP_POINT).where(TABLE_TP_POINT.KEY.eq(UInteger.valueOf(key))).fetchOne();
    }

    /**
     * Returns a TeleportPoint given user has access to
     * <p>Favouring owned over public over invited TeleportPoints
     *
     * @param user
     * @param name
     * @return
     */
    public T findOne(User user, String name)
    {
        Map<String, T> map = this.points.get(name);
        if (map == null)
        {
            for (String found : findIn(name, this.points.keySet()))
            {
                map = this.points.get(found);
                break;
            }
            if (map == null)
            {
                return null;
            }
        }
        for (T point : map.values())
        {
            if (point.isOwner(user))
            {
                return point;
            }
        }
        for (T point : map.values())
        {
            if (point.isPublic())
            {
                return point;
            }
        }
        T match = null;
        for (T point : map.values())
        {
            if (point.isInvited(user))
            {
                return point;
            }
            match = point;
        }
        return match;
    }

    public T getExact(User user, String name)
    {
        Map<String, T> map = this.points.get(name);
        return map == null ? null : map.get(user.getName().toLowerCase());
    }

    public boolean has(User user, String name)
    {
        Map<String, T> map = this.points.get(name);
        if (map != null)
        {
            for (T value : map.values())
            {
                if (value.isOwner(user))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public int getCount(User user)
    {
        return this.dsl.selectFrom(TABLE_TP_POINT).where(TABLE_TP_POINT.OWNER.eq(user.getEntity().getKey())).fetchCount();
    }

    public abstract T create(User owner, String name, Location location, boolean publicVisiblity);

    public void delete(T point)
    {
        point.getModel().delete();
        this.removePoint(point);
    }

    private void removePoint(T point)
    {
        this.points.get(point.getName()).remove(point.getOwnerName().toLowerCase());
    }

    public Set<T> list(boolean privates, boolean publics)
    {
        if (!privates && !publics)
        {
            privates = true;
            publics = true;
        }
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

    public Set<T> list(User user, boolean owned, boolean publics, boolean invited)
    {
        if (!owned && !publics && !invited)
        {
            owned = true;
            publics = true;
            invited = true;
        }
        Set<T> set = new HashSet<>();
        for (Map<String, T> map : this.points.values())
        {
            set.addAll(findIn(user, owned, publics, invited, map));
        }
        return set;
    }

    public Set<T> find(User user, String token, boolean owned, boolean publics, boolean invited)
    {
        if (!owned && !publics && !invited)
        {
            owned = true;
            publics = true;
            invited = true;
        }
        Set<T> set = new LinkedHashSet<>();
        for (String name : findIn(token, this.points.keySet()))
        {
            set.addAll(findIn(user, owned, publics, invited, this.points.get(name)));
        }
        return set;
    }

    private Set<T> findIn(User user, boolean owned, boolean publics, boolean invited, Map<String, T> map)
    {
        Set<T> set = new LinkedHashSet<>();
        for (T value : map.values())
        {
            if (owned && value.isOwner(user))
            {
                set.add(value);
            }
            if (publics && value.isPublic() && !value.isOwner(user))
            {
                set.add(value);
            }
            if (invited && value.isInvited(user))
            {
                set.add(value);
            }
        }
        return set;
    }

    public void massDelete(boolean privates, boolean publics)
    {
        for (T point : this.list(privates, publics))
        {
            this.delete(point);
        }
    }

    public void massDelete(User user, boolean privates, boolean publics)
    {
        if (!privates && !publics)
        {
            privates = true;
            publics = true;
        }
        for (T point : this.list(user, true, false, false))
        {
            if (privates && !point.isPublic())
            {
                this.delete(point);
            }
            if (publics && point.isPublic())
            {
                this.delete(point);
            }
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

    public int getCount()
    {
        return this.list(true, true).size();
    }

    public static TreeSet<String> findIn(String pName, Set<String> strings)
    {
        TreeSet<String> result = new TreeSet<>(new StringLengthComparator());
        for (String s : strings)
        {
            if (StringUtils.startsWithIgnoreCase(s, pName))
            {
                result.add(s);
            }
        }
        return result;
    }

    private static class StringLengthComparator implements Comparator<String>
    {
        @Override
        public int compare(String o1, String o2)
        {
            int dif = o1.length() - o2.length();
            if (dif == 0)
            {
                dif = 1;
            }
            return dif;
        }
    }
}
