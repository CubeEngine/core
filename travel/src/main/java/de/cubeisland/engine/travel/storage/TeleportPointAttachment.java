package de.cubeisland.engine.travel.storage;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.cubeisland.engine.core.user.UserAttachment;
import de.cubeisland.engine.core.util.StringUtils;

public class TeleportPointAttachment<T extends TeleportPoint> extends UserAttachment
{
    protected final Map<String, T> owned = new HashMap<>();
    protected final Map<String, TreeMap<String, T>> publics = new HashMap<>();
    protected final Map<String, TreeMap<String, T>> invited = new HashMap<>();

    public void add(T point)
    {
        if (point.isOwner(getHolder()))
        {
            this.owned.put(point.getName(), point);
            return;
        }
        if (point.isPublic())
        {
            this.add0(point, this.publics);
            return;
        }
        this.add0(point, this.invited);
    }

    private void add0(T point, Map<String, TreeMap<String, T>> points)
    {
        TreeMap<String, T> map = points.get(point.getName());
        if (map == null)
        {
            map = new TreeMap<>(new StringLengthComparator());
            points.put(point.getName(), map);
        }
        map.put(point.getOwnerName(), point);
    }

    public void remove(T point)
    {
        this.owned.remove(point.getName());
        TreeMap<String, T> map = this.publics.get(point.getName());
        if (map != null)
        {
            map.remove(point.getOwnerName());
        }
        map = this.invited.get(point.getName());
        if (map != null)
        {
            map.remove(point.getOwnerName());
        }
    }

    public T getOwned(String name)
    {
        return owned.get(name);
    }

    public T get(String name, String owner)
    {
        if (owner.equals(getHolder().getName()))
        {
            return this.getOwned(name);
        }
        T result = this.get0(name, owner, this.publics);
        if (result == null)
        {
            result = this.get0(name, owner, this.invited);
        }
        return result;
    }

    private T get0(String name, String owner, Map<String, TreeMap<String, T>> points)
    {
        Map<String, T> map = points.get(name);
        if (map == null)
        {
            return null;
        }
        return map.get(owner);
    }

    public T findOne(String name)
    {
        String pName = name;
        String oName = null;
        // Split from ownername and get direct match
        if (name.contains(":"))
        {
            pName = name.substring(name.lastIndexOf(":") + 1);
            oName = name.substring(0, name.lastIndexOf(":"));
            T match = this.get(pName, oName);
            if (match != null)
            {
                return match;
            }
        }
        else
        {
            T match = this.getOwned(pName);
            if (match != null)
            {
                return match;
            }
        }
        // Find owned
        if (oName == null || oName.equals(getHolder().getName()))
        {
            for (String result : findIn(pName, this.owned.keySet()))
            {
                return this.owned.get(result);
            }
        }
        // Find public
        T result = this.findOne(pName, oName, this.publics);
        if (result == null)
        {
            // find invited
            result = this.findOne(pName, oName, this.invited);
        }
        return result;
    }

    private T findOne(String pName, String oName, Map<String, TreeMap<String, T>> points)
    {
        for (String result : findIn(pName, points.keySet()))
        {
            Map<String, T> map = points.get(result);
            if (oName == null)
            {
                for (T value : map.values())
                {
                    return value;
                }
            }
            else
            {
                for (String owner : findIn(oName, map.keySet()))
                {
                    return map.get(owner);
                }
            }
        }
        return null;
    }

    private static TreeSet<String> findIn(String pName, Set<String> strings)
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

    public LinkedHashSet<T> find(String name)
    {
        LinkedHashSet<T> result = new LinkedHashSet<>();
        String pName = name;
        String oName = null;
        // Split from ownername and get direct match
        if (name.contains(":"))
        {
            pName = name.substring(name.lastIndexOf(":") + 1);
            oName = name.substring(0, name.lastIndexOf(":"));
            T match = this.get(pName, oName);
            if (match != null)
            {
                result.add(match);
            }
        }
        else
        {
            T match = this.getOwned(pName);
            if (match != null)
            {
                result.add(match);
            }
        }
        // Find owned
        if (oName == null || oName.equals(getHolder().getName()))
        {
            for (String match : findIn(pName, this.owned.keySet()))
            {
                result.add(this.owned.get(match));
            }
        }
        // Find public
        result.addAll(this.find(pName, oName, this.publics));
        // find invited
        result.addAll(this.find(pName, oName, this.invited));
        return result;
    }

    private Set<T> find(String pName, String oName, Map<String, TreeMap<String, T>> points)
    {
        Set<T> result = new LinkedHashSet<>();
        for (String match : findIn(pName, points.keySet()))
        {
            Map<String, T> map = points.get(match);
            if (oName == null)
            {
                for (T value : map.values())
                {
                    result.add(value);
                }
            }
            else
            {
                for (String owner : findIn(oName, map.keySet()))
                {
                    result.add(map.get(owner));
                }
            }
        }
        return result;
    }

    public Set<T> list(boolean owned, boolean publics, boolean invited)
    {
        Set<T> set = new HashSet<>();
        if (owned)
        {
            set.addAll(this.owned.values());
        }
        if (publics)
        {
            for (TreeMap<String, T> map : this.publics.values())
            {
                set.addAll(map.values());
            }
        }
        if (invited)
        {
            for (TreeMap<String, T> map : this.invited.values())
            {
                set.addAll(map.values());
            }
        }
        return set;
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
