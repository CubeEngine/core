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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Location;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.travel.Travel;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.travel.storage.TableTeleportPoint.TABLE_TP_POINT;
import static de.cubeisland.engine.travel.storage.TeleportPointModel.*;

public class TelePointManager
{
    private final DSLContext dsl;

    private InviteManager inviteManager;
    private final Map<String, Home> homes;
    private final Map<String, Warp> warps;
    private final Travel module;

    public TelePointManager(Travel module)
    {
        this.dsl = module.getCore().getDB().getDSL();
        this.module = module;
        this.homes = new HashMap<>();
        this.warps = new HashMap<>();
    }

    /**
     * Load all warps and homes from the database to a local storage
     */
    public void load(InviteManager inviteManager)
    {
        this.inviteManager = inviteManager;
        for (TeleportPointModel teleportPoint : this.getAll())
        {
            if (teleportPoint.getType() == TYPE_HOME)
            {
                Home home = new Home(teleportPoint, this, inviteManager, this.module);
                if (home.isPublic())
                {
                     this.homes.put(home.getName(), home);
                }
                else
                {
                    homes.put(home.getStorageName(), home);
                }
            }
            else
            {
                Warp warp = new Warp(teleportPoint, this, inviteManager, this.module);
                warps.put(warp.getStorageName(), warp);
            }
        }
    }

    public Collection<TeleportPointModel> getAll()
    {
        return this.dsl.selectFrom(TABLE_TP_POINT).fetch();
    }

    public TeleportPointModel get(Long key)
    {
        return this.dsl.selectFrom(TABLE_TP_POINT).where(TABLE_TP_POINT.KEY.eq(UInteger.valueOf(key))).fetchOne();
    }

    /**
     * Get a home by name relative to an user
     * The name can be edit distance <= 2 away from a home name
     * <p/>
     * the name can be just the name of the home, owner:name or public:name
     *
     * @param user the user
     * @param name the name of the home relative to the user
     *
     * @return The home if found, else null
     */
    public Home getHome(User user, String name)
    {
        if (name == null || user == null)
        {
            return null;
        }

        HomeAttachment attachment = user.attachOrGet(HomeAttachment.class, this.module);
        if ((name.equals("home") || name.equals(user.getName())) && this.homes.containsKey(user.getId()+":home"))
        {
            return this.homes.get(user.getId()+":home");
        }
        if (attachment.hasHome(name))
        {
            return attachment.getHome(name);
        }
        else if (name.startsWith("public:"))
        {
            name = name.replaceFirst("public:", "");
            if (homes.containsKey(name))
            {
                Home home = this.homes.get(name);
                if (home.canAccess(user))
                {
                    return home;
                }
            }
            else
            {
                Set<String> matches = Match.string().getBestMatches(name, homes.keySet(), 2);
                if (!matches.isEmpty())
                {
                    for (String match : matches)
                    {
                        if (match.contains(":")) continue;
                        return homes.get(match);
                    }
                }
            }
        }
        else if (name.contains(":"))
        {
            User userOfHome = this.module.getCore().getUserManager().getUser(name.substring(0, name.indexOf(":")));
            if (userOfHome == null) return null;
            name = name.replaceFirst(userOfHome.getName(), userOfHome.getId().toString());
            if (homes.containsKey(name))
            {
                Home home = homes.get(name);
                if (home.canAccess(userOfHome))
                {
                    this.putHomeToUser(home, userOfHome);
                    return home;
                }
            }
            else
            {
                Set<String> matches = Match.string().getBestMatches(name, attachment.allHomes().keySet(), 2);
                if (!matches.isEmpty())
                {
                    return attachment.getHome(matches.iterator().next());
                }
            }
        }
        else
        {
            if (homes.containsKey(user.getId() + ":" + name))
            {
                Home home = homes.get(user.getId() + ":" + name);
                this.putHomeToUser(home, user);
                return home;
            }
            else if (homes.containsKey(name))
            {
                Home home = this.homes.get(name);
                if (home.canAccess(user))
                {
                    return home;
                }
            }
            else
            {
                Set<String> matches = Match.string().getBestMatches(name, attachment.allHomes().keySet(), 2);
                if (!matches.isEmpty())
                {
                    return attachment.getHome(matches.iterator().next());
                }

                Set<String> publicMatches = Match.string().getBestMatches(name, homes.keySet(), 2);
                for (String match : publicMatches)
                {
                    if (match.contains(":")) continue;
                    Home home = homes.get(match);
                    if (home.canAccess(user)) return home;
                }

                for (Home home : homes.values())
                {
                    if (home.getName().equals(name) && home.canAccess(user))
                    {
                        this.putHomeToUser(home, user);
                        return home;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get a home by its name.
     * this have to be in the format owner:home or just home if public
     *
     * @return the home if found
     */
    public Home getHome(String name)
    {
        if (name.contains(":"))
        {
            User userOfWarp = this.module.getCore().getUserManager().getUser(name.substring(0, name.indexOf(":")));
            if (userOfWarp == null) return null;
            name = name.replaceFirst(userOfWarp.getName(), userOfWarp.getId().toString());
        }
        return homes.get(name);
    }

    public int getNumberOfHomes(User user)
    {
        return this.dsl.selectFrom(TABLE_TP_POINT).where(TABLE_TP_POINT.OWNER.eq(user.getEntity().getKey())).fetchCount();
    }

    /**
     * If a home by that name relative to the user exist
     * The name must be an exact match
     *
     * @param name the name relative to the user
     * @param user the user
     */
    public boolean hasHome(String name, User user)
    {
        Home home = this.getHome(user, name);
        return home != null && home.getName().equals(name);
    }

    /**
     * Put a home in an users home storage
     * TODO move this into HomeAttachment?
     *
     * @param home the home
     * @param user the user
     */
    public void putHomeToUser(Home home, User user)
    {
        HomeAttachment attachment = user.attachOrGet(HomeAttachment.class, this.module);

        if (attachment.containsHome(home.getName()))
        {
            Home rename = attachment.getHome(home.getName());
            if (!rename.isOwner(user) && home.isOwner(user))
            {
                attachment.addHome(home.getName(), home);
            }
            else
            {
                attachment.addHome(home.getStorageName(), home);
            }
            attachment.addHome(rename.getStorageName(), rename);
        }
        else
        {
            if (home.isOwner(user))
            {
                attachment.addHome(home.getName(), home);
            }
            else
            {
                attachment.addHome(home.getStorageName(), home);
            }
        }
    }

    /**
     * Unload a home from an users home storage
     * TODO move this into HomeAttachment?
     *
     * @param home the home
     * @param user the user
     */
    public void removeHomeFromUser(Home home, User user)
    {
        if (home == null || user == null)
        {
            return;
        }
        HomeAttachment attachment = user.attachOrGet(HomeAttachment.class, this.module);

        if (!attachment.hasHome(home.getName()))
        {
            return;
        }

        // Remove the home from the users list
        if (attachment.getHome(home.getName()).equals(home))
        {
            attachment.removeHome(home.getName());
        }
        else if (attachment.getHome(home.getStorageName()).equals(home))
        {
            attachment.removeHome(home.getStorageName());
        }

        // If another home the player has can be taken away it's prefix, do it
        Set<Home> prefixed = new HashSet<>();
        for (String name : attachment.allHomes().keySet())
        {
            String[] parts = name.split(":");
            if (parts.length == 2)
            {
                if (parts[1].equals(home.getName()))
                {
                    prefixed.add(attachment.getHome(name));
                }
            }
        }

        if (prefixed.size() == 1) // We only want to remove the prefix if there are no conflicts
        {
            for (Home h : prefixed)
            {
                attachment.addHome(home.getName(), h);
            }
        }
    }

    /**
     * Create a home
     * <p/>
     * Obs: This does not add it to the home storage, nor the database
     *
     * @return the newly generated home
     */
    public Home createHome(Location location, String name, User owner, short visibility)
    {
        TeleportPointModel model = this.dsl.newRecord(TABLE_TP_POINT).newTPPoint(location, name, owner, null, TYPE_HOME, visibility);
        Home home = new Home(model, this, inviteManager, this.module);
        model.insert();
        this.putHomeToUser(home, home.getOwner());
        this.homes.put(home.getStorageName(), home);
        for (User user : home.getInvitedUsers())
        {
            this.putHomeToUser(home, user);
        }
        return home;
    }

    /**
     * Delete and unload a home
     * this will also remove it from the database
     */
    public void deleteHome(Home home)
    {
        if (home.getVisibility() == VISIBILITY_PRIVATE)
        {
            this.removeHomeFromUser(home, home.getOwner());
            for (User user : home.getInvitedUsers())
            {
                this.removeHomeFromUser(home, user);
            }
        }
        else
        {
            for (User user : CubeEngine.getUserManager().getLoadedUsers())
            {
                removeHomeFromUser(home, user);
            }
        }
        home.getModel().delete();
        if (home.isPublic())
        {
            this.homes.remove(home.getName());
        }
        else
        {
            this.homes.remove(home.getStorageName());
        }
    }

    public int getNumberOfWarps()
    {
        return this.warps.keySet().size();
    }

    /**
     * Get a warp by name relative to an user
     *
     * @param user the user
     * @param name the name relative to the user
     *
     * @return the warp if found, or null
     */
    public Warp getWarp(User user, String name)
    {
        if (user == null || name == null)
        {
            return null;
        }

        WarpAttachment attachment = user.attachOrGet(WarpAttachment.class, this.module);

        if (attachment.hasWarp(name))
        {
            return attachment.getWarp(name);
        }
        else if (attachment.hasWarp("public:" + name))
        {
            return attachment.getWarp("public:" + name);
        }
        else if (name.contains(":"))
        {
            User userOfWarp = this.module.getCore().getUserManager().getUser(name.substring(0, name.indexOf(":")));
            if (userOfWarp == null) return null;
            name = name.replaceFirst(userOfWarp.getName(), userOfWarp.getId().toString());
            if (warps.containsKey(name))
            {
                Warp warp = warps.get(name);
                this.putWarpToUser(warp, user);
                return warp;
            }
            else
            {
                Set<String> matches = Match.string().getBestMatches(name, attachment.allWarps().keySet(), 2);
                if (!matches.isEmpty())
                {
                    return attachment.getWarp(matches.iterator().next());
                }
            }
        }
        else
        {
            if (warps.containsKey(user.getId() + ":" + name))
            {
                Warp warp = warps.get(user.getId()  + ":" + name);
                this.putWarpToUser(warp, user);
                return warp;
            }
            else if (warps.containsKey("public:" + name))
            {
                Warp warp = warps.get("public:" + name);
                this.putWarpToUser(warp, user);
                return warp;
            }
            else
            {
                Set<String> matches = Match.string().getBestMatches(name, attachment.allWarps().keySet(), 2);
                if (!matches.isEmpty())
                {
                    return attachment.getWarp(matches.iterator().next());
                }

                for (Warp warp : warps.values())
                {
                    if (warp.getName().equals(name) && warp.canAccess(user))
                    {
                        this.putWarpToUser(warp, user);
                        return warp;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Return the public warp with that name
     */
    public Warp getWarp(String name)
    {
        if (name.contains(":"))
        {
            return this.warps.get(name);
        }
        else
        {
            return this.warps.get("public:" + name);
        }
    }

    /**
     * If a warp by that name exist or not.
     *
     * @param name the name in this format: {"public",owner}:home
     */
    public boolean hasWarp(String name)
    {
        return getWarp(name) != null;
    }

    /**
     * Get a ranked list of how well warps matches the search
     *
     * @param search search word
     * @param sender the sender that sent the command
     */
    public TreeMap<String, Integer> searchWarp(String search, CommandSender sender)
    {
        Set<String> warps = new HashSet<>();
        if (sender instanceof User)
        {
            User user = (User)sender;
            for (Warp iterate : this.warps.values())
            {
                if (iterate.canAccess(user))
                {
                    warps.add(iterate.getName());
                }
            }
        }
        else
        {
            for (Warp iterate : this.warps.values())
            {
                warps.add(iterate.getName());
            }
        }

        return Match.string().getMatches(search, warps, 5, true);
    }

    /**
     * Put a warp in an user warp storage
     *
     * @param warp the warp
     * @param user the user
     */
    public void putWarpToUser(Warp warp, User user)
    {
        WarpAttachment attachment = user.attachOrGet(WarpAttachment.class, this.module);
        if (attachment.containsWarp(warp.getName()))
        {
            if (warp.isPublic() || warp.isOwner(user))
            {
                Warp rename = attachment.getWarp(warp.getName());
                attachment.addWarp(warp.getName(), warp);
                attachment.addWarp(rename.getStorageName(), warp);
            }
            else
            {
                attachment.addWarp(warp.getStorageName(), warp);
            }
        }
        else
        {
            attachment.addWarp(warp.getName(), warp);
        }
    }

    /**
     * Remove a warp from an users warp storage
     *
     * @param warp the warp
     * @param user the user
     */
    public void removeWarpFromUser(Warp warp, User user)
    {
        WarpAttachment attachment = user.attachOrGet(WarpAttachment.class, this.module);

        if (attachment.containsWarp(warp.getName()))
        {
            if (attachment.getWarp(warp.getName()).equals(warp))
            {
                attachment.removeWarp(warp.getName());
            }
        }
        else if (attachment.containsWarp(warp.getStorageName()))
        {
            if (attachment.getWarp(warp.getStorageName()).equals(warp))
            {
                attachment.removeWarp(warp.getStorageName());
            }
        }
    }

    /**
     * Create a warp
     * <p/>
     * Obs: This does not add it to the warp storage, nor the database
     *
     * @return the newly generated warp
     */
    public Warp createWarp(Location location, String name, User owner, short visibility)
    {
        TeleportPointModel model = this.dsl.newRecord(TABLE_TP_POINT).newTPPoint(location, name, owner, null, TYPE_WARP, visibility);
        Warp warp = new Warp(model, this, inviteManager, this.module);
        model.insert();
        this.putWarpToUser(warp, warp.getOwner());
        this.warps.put(warp.getStorageName(), warp);
        for (User user : warp.getInvitedUsers())
        {
            this.putWarpToUser(warp, user);
        }
        return warp;
    }

    /**
     * Rename a warp
     */
    public void renameWarp(Warp warp, String name)
    {
        this.warps.remove(warp.getStorageName());
        for (User user : CubeEngine.getUserManager().getOnlineUsers())
        {
            for (Object object : user.attachOrGet(WarpAttachment.class, this.module).allWarps().keySet())
            {
                if (object instanceof String)
                {
                    String string = (String)object;
                    if (string.equalsIgnoreCase(warp.getName()) || string.equalsIgnoreCase(warp.getStorageName()))
                    {
                        user.attachOrGet(WarpAttachment.class, this.module).removeWarp(string);
                    }
                }
            }
        }
        warp.setName(name);
        this.warps.put(warp.getStorageName(), warp);
    }

    /**
     * Delete and unload a warp
     * This will also delete the warp from the database
     */
    public void deleteWarp(Warp warp)
    {
        for (User user : CubeEngine.getUserManager().getOnlineUsers())
        {
            for (Object object : user.attachOrGet(WarpAttachment.class, this.module).allWarps().keySet())
            {
                if (object instanceof String)
                {
                    String string = (String)object;
                    if (string.equalsIgnoreCase(warp.getName()) || string.equalsIgnoreCase(warp.getStorageName()))
                    {
                        user.attachOrGet(WarpAttachment.class, this.module).removeWarp(string);
                    }
                }
            }
        }
        this.warps.remove(warp.getStorageName());
        warp.getModel().delete();
    }

    public final int ALL = -1;
    public final int PUBLIC = 1 << 0;
    public final int PRIVATE = 1 << 1;
    public final int OWNED = 1 << 2;
    public final int INVITED = 1 << 3;

    // TODO - It seems there has been a mix-up in the "storageNames" of teleportpoints.
    // Sometimes it they are used as <user name>:<teleport point name> and sometimes they are used as
    // <user key>:<teleport point name>.

    public Set<Home> listHomes(int mask)
    {
        Set<Home> homes = new HashSet<>();
        if ((mask & PUBLIC) == PUBLIC)
        {
            Result<Record1<String>> fetch = this.dsl.select(TABLE_TP_POINT.NAME)
                                                     .from(TABLE_TP_POINT)
                                                     .where(TABLE_TP_POINT.TYPE.eq(TYPE_HOME),
                                                            TABLE_TP_POINT.VISIBILITY.eq(VISIBILITY_PUBLIC)).fetch();
            for (Record1<String> record2 : fetch)
            {
                Home home = this.homes.get(record2.value1());
                if (home != null)
                {
                    homes.add(home);
                }
            }
        }
        if ((mask & PRIVATE) == PRIVATE)
        {
            Result<Record2<String,UInteger>> fetch = this.dsl.select(TABLE_TP_POINT.NAME, TABLE_TP_POINT.OWNER)
                                                             .from(TABLE_TP_POINT)
                                                             .where(TABLE_TP_POINT.TYPE.eq(TYPE_HOME),
                                                                    TABLE_TP_POINT.VISIBILITY.eq(VISIBILITY_PRIVATE)).fetch();
            for (Record2<String, UInteger> record2 : fetch)
            {
                Home home = this.homes.get(record2.value2().longValue() + ":" + record2.value1());
                if (home != null)
                {
                    homes.add(home);
                }
            }
        }
        return homes;
    }

    public Set<Home> listHomes(User user, int mask)
    {
        Set<Home> homes = new HashSet<>();
        if (mask == -1)
        {
            homes.addAll(this.listHomes(PUBLIC));
            homes.addAll(this.listHomes(user, PUBLIC | PRIVATE | OWNED | INVITED));
            return homes;
        }

        // If mask contains neither PUBLIC nor PRIVATE turn both on
        if ((mask & (PUBLIC | PRIVATE)) == 0)
        {
            mask |= PUBLIC | PRIVATE;
        }

        // If mask contains neither OWNED nor INVITED turn both on
        if ((mask & (OWNED | INVITED)) == 0)
        {
            mask |= OWNED | INVITED;
        }

        if ((mask & (PUBLIC | OWNED)) == (PUBLIC | OWNED))
        {
            Result<Record1<String>> fetch = this.dsl.select(TABLE_TP_POINT.NAME)
                                                             .from(TABLE_TP_POINT)
                                                             .where(TABLE_TP_POINT.TYPE.eq(TYPE_HOME),
                                                                    TABLE_TP_POINT.VISIBILITY.eq(VISIBILITY_PUBLIC),
                                                                    TABLE_TP_POINT.OWNER.eq(user.getEntity().getKey())).fetch();
            for (Record1<String> record1 : fetch)
            {
                Home home = this.homes.get(record1.value1());
                if (home != null)
                {
                    homes.add(home);
                }
            }
        }
        if ((mask & (PRIVATE | OWNED)) == (PRIVATE | OWNED))
        {
            Result<Record2<String,UInteger>> fetch = this.dsl.select(TABLE_TP_POINT.NAME, TABLE_TP_POINT.OWNER)
                                                             .from(TABLE_TP_POINT)
                                                             .where(TABLE_TP_POINT.TYPE.eq(TYPE_HOME),
                                                                    TABLE_TP_POINT.VISIBILITY.eq(VISIBILITY_PRIVATE),
                                                                    TABLE_TP_POINT.OWNER.eq(user.getEntity().getKey())).fetch();
            for (Record2<String, UInteger> record2 : fetch)
            {
                Home home = this.homes.get(record2.value2().longValue() + ":" + record2.value1());
                if (home != null)
                {
                    homes.add(home);
                }
            }
        }
        if ((mask & INVITED) == INVITED)
        {
            for (TeleportInvite invite : this.inviteManager.getInvites(user))
            {
                TeleportPointModel point = this.get(invite.getTeleportpoint().longValue());
                if (point == null)
                {
                    this.module.getLog().warn("TeleportPointModel is null for #{}", invite.getTeleportpoint().longValue());
                    continue;
                }
                if (point.getType() == TYPE_HOME)
                {
                    Home home = this.homes.get(point.getOwnerKey().longValue() + ":" + point.getName());
                    if (home != null)
                    {
                        if ((home.isPublic() && (mask & PUBLIC) == PUBLIC) || (!home.isPublic() && (mask & PRIVATE) == PRIVATE))
                        {
                            homes.add(home);
                        }
                    }
                }
            }
        }
        return homes;
    }

    public Set<Warp> listWarps(int mask)
    {
        Set<Warp> warps = new HashSet<>();
        if ((mask & PUBLIC) == PUBLIC)
        {
            Result<Record2<String,UInteger>> fetch = this.dsl.select(TABLE_TP_POINT.NAME, TABLE_TP_POINT.OWNER)
                                                             .from(TABLE_TP_POINT)
                                                             .where(TABLE_TP_POINT.TYPE.eq(TYPE_WARP),
                                                                    TABLE_TP_POINT.VISIBILITY.eq(VISIBILITY_PUBLIC)).fetch();
            for (Record2<String, UInteger> record2 : fetch)
            {
                Warp warp = this.warps.get(record2.value2().longValue() + ":" + record2.value1());
                if (warp != null)
                {
                    warps.add(warp);
                }
            }
        }
        if ((mask & PRIVATE) == PRIVATE)
        {
            Result<Record2<String,UInteger>> fetch = this.dsl.select(TABLE_TP_POINT.NAME, TABLE_TP_POINT.OWNER)
                                                             .from(TABLE_TP_POINT)
                                                             .where(TABLE_TP_POINT.TYPE.eq(TYPE_WARP),
                                                                    TABLE_TP_POINT.VISIBILITY.eq(VISIBILITY_PRIVATE)).fetch();
            for (Record2<String, UInteger> record2 : fetch)
            {
                Warp warp = this.warps.get(record2.value2().longValue() + ":" + record2.value1());
                if (warp != null)
                {
                    warps.add(warp);
                }
            }
        }
        return warps;
    }

    public Set<Warp> listWarps(User user, int mask)
    {
        Set<Warp> warps = new HashSet<>();
        if (mask == -1)
        {
            warps.addAll(this.listWarps(PUBLIC));
            warps.addAll(this.listWarps(user, PUBLIC | PRIVATE | OWNED | INVITED));
            return warps;
        }

        // If mask contains neither PUBLIC nor PRIVATE turn both on
        if ((mask & (PUBLIC | PRIVATE)) == 0)
        {
            mask |= PUBLIC | PRIVATE;
        }

        // If mask contains neither OWNED nor INVITED turn both on
        if ((mask & (OWNED | INVITED)) == 0)
        {
            mask |= OWNED | INVITED;
        }

        if ((mask & (PUBLIC | OWNED)) == (PUBLIC | OWNED))
        {
            Result<Record2<String,UInteger>> fetch = this.dsl.select(TABLE_TP_POINT.NAME, TABLE_TP_POINT.OWNER)
                                                             .from(TABLE_TP_POINT)
                                                             .where(TABLE_TP_POINT.TYPE.eq(TYPE_WARP),
                                                                    TABLE_TP_POINT.VISIBILITY.eq(VISIBILITY_PUBLIC),
                                                                    TABLE_TP_POINT.OWNER.eq(user.getEntity().getKey())).fetch();
            for (Record2<String, UInteger> record2 : fetch)
            {
                Warp warp = this.warps.get("public:" + record2.value1());
                if (warp != null)
                {
                    warps.add(warp);
                }
            }
        }
        if ((mask & (PRIVATE | OWNED)) == (PRIVATE | OWNED))
        {
            Result<Record2<String,UInteger>> fetch = this.dsl.select(TABLE_TP_POINT.NAME, TABLE_TP_POINT.OWNER)
                                                             .from(TABLE_TP_POINT)
                                                             .where(TABLE_TP_POINT.TYPE.eq(TYPE_WARP),
                                                                    TABLE_TP_POINT.VISIBILITY.eq(VISIBILITY_PRIVATE),
                                                                    TABLE_TP_POINT.OWNER.eq(user.getEntity().getKey())).fetch();
            for (Record2<String, UInteger> record2 : fetch)
            {
                Warp warp = this.warps.get(record2.value2().longValue() + ":" + record2.value1());
                if (warp != null)
                {
                    warps.add(warp);
                }
            }
        }
        if ((mask & INVITED) == INVITED)
        {
            for (TeleportInvite invite : this.inviteManager.getInvites(user))
            {
                TeleportPointModel point = this.get(invite.getTeleportpoint().longValue());
                if (point.getType() == TYPE_WARP)
                {
                    Warp warp = this.warps.get(point.getOwnerKey().longValue() + ":" + point.getName());
                    if (warp != null)
                    {
                        if ((warp.isPublic() && (mask & PUBLIC) == PUBLIC) || (!warp.isPublic() && (mask & PRIVATE) == PRIVATE))
                        {
                            warps.add(warp);
                        }
                    }
                }
            }
        }
        return warps;
    }

    public void deleteHomes(int mask)
    {
        Set<Home> homes = this.listHomes(mask);
        for (Home home : homes)
        {
            this.deleteHome(home);
        }
    }

    public void deleteHomes(User user, int mask)
    {
        Set<Home> homes = this.listHomes(user, mask);
        for (Home home : homes)
        {
            this.deleteHome(home);
        }
    }

    public void deleteWarps(int mask)
    {
        Set<Warp> warps = this.listWarps(mask);
        for (Warp warp : warps)
        {
            this.deleteWarp(warp);
        }
    }

    public void deleteWarps(User user, int mask)
    {
        Set<Warp> warps = this.listWarps(mask);
        for (Warp warp : warps)
        {
            this.deleteWarp(warp);
        }
    }

}
