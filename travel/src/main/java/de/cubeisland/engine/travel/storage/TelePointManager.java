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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Location;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.storage.SingleKeyStorage;
import de.cubeisland.engine.core.storage.StorageException;
import de.cubeisland.engine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.travel.Travel;
import de.cubeisland.engine.travel.storage.TeleportPoint.Visibility;

import static de.cubeisland.engine.travel.storage.TeleportPoint.Type.HOME;
import static de.cubeisland.engine.travel.storage.TeleportPoint.Type.WARP;

public class TelePointManager extends SingleKeyStorage<Long, TeleportPoint>
{
    private static final int REVISION = 6;

    private InviteManager inviteManager;
    private final Map<String, Home> homes;
    private final Map<String, Home> publicHomes;
    private final Map<String, Warp> warps;
    private final Travel module;

    public TelePointManager(Travel module)
    {
        super(module.getCore().getDB(), TeleportPoint.class, REVISION);
        this.module = module;
        this.initialize();
        this.homes = new HashMap<String, Home>();
        this.publicHomes = new HashMap<String, Home>();
        this.warps = new HashMap<String, Warp>();
    }

    @Override
    protected void prepareStatements() throws SQLException
    {
        super.prepareStatements();
        QueryBuilder builder = this.database.getQueryBuilder();

        // List statements
        this.database.storeStatement(this.modelClass, "filtered_getAll",
                                     builder.select(this.tableName+".key","name","owner","player").from(this.tableName)
                                            .joinOnEqual("user", "key", tableName, "owner")
                                     .where().field("type").isEqual().value() // First value TeleportPoint.Type.<>.ordinal() (home/warp)
                                     .and().field("visibility").isEqual().value() // Second value Visibility.<>.ordinal() (public/private)
            .end().end());
        this.database.storeStatement(this.modelClass, "filtered_getAll_owned",
                                     builder.select(this.tableName+".key","name","owner","player").from(this.tableName)
                                            .joinOnEqual("user", "key", tableName, "owner")
                                            .where().field("type").isEqual().value() // First value TeleportPoint.Type.<>.ordinal() (home/warp)
                                         .and().field("visibility").isEqual().value() // Second value Visibility.<>.ordinal() (public/private)
                                         .and().field("owner").isEqual().value() // Third value owner key
                                         .end().end());
        // Override getAll and get Statements
        this.database.storeStatement(this.modelClass, "getall", builder.select().
            fields(tableName + ".key", "owner", "type", "visibility", "world", "x", "y", "z", "yaw", "pitch", "name", "welcomemsg", "player")
           .from(tableName).joinOnEqual("user", "key", tableName, "owner")
           .end().end());

        this.database.storeStatement(this.modelClass, "get", builder.select().
            fields(tableName + ".key", "owner", "type", "visibility", "world", "x", "y", "z", "yaw", "pitch", "name", "welcomemsg", "player")
               .from(tableName).joinOnEqual("user", "key", tableName, "owner")
            .where().field(tableName + ".key").isEqual().value()
               .end().end());
    }

    /**
     * Load all warps and homes from the database to a local storage
     */
    public void load(InviteManager inviteManager)
    {
        this.inviteManager = inviteManager;
        for (TeleportPoint teleportPoint : this.getAll())
        {
            if (teleportPoint.type.equals(HOME))
            {
                Home home = new Home(teleportPoint, this, inviteManager, this.module);
                if (home.isPublic())
                {
                    this.publicHomes.put(home.getName(), home);
                }
                homes.put(home.getStorageName(), home);
            }
            else
            {
                Warp warp = new Warp(teleportPoint, this, inviteManager, this.module);
                warps.put(warp.getStorageName(), warp);
            }
        }
    }

    @Override
    public Collection<TeleportPoint> getAll()
    {
        Collection<TeleportPoint> loadedModels = new ArrayList<TeleportPoint>();
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(this.modelClass, "getall");
            while (resulsSet.next())
            {
                Map<String, Object> values = new LinkedHashMap<String, Object>();
                for (String name : this.reverseFieldNames.keySet())
                {
                    values.put(name, resulsSet.getObject(name));
                }
                values.put("player", resulsSet.getObject("player"));
                TeleportPoint tpPoint = this.modelConstructor.newInstance(values);
                loadedModels.add(tpPoint);
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while getting Model from Database", ex, this.database.getStoredStatement(this.modelClass,"getall"));
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while creating fresh Model from Database", ex);
        }
        return loadedModels;
    }

    @Override
    public TeleportPoint get(Long key)
    {
        try
        {
            ResultSet resulsSet = this.database.preparedQuery(this.modelClass, "getall");
            if (resulsSet.next())
            {
                Map<String, Object> values = new HashMap<String, Object>();
                for (String name : this.reverseFieldNames.keySet())
                {
                    values.put(name, resulsSet.getObject(name));
                }
                values.put("player", resulsSet.getObject("player"));
                TeleportPoint tpPoint = this.modelConstructor.newInstance(values);
                return tpPoint;
            }
            return null;
        }
        catch (SQLException ex)
        {
            throw new StorageException("Error while getting Model from Database", ex, this.database.getStoredStatement(this.modelClass,"get"));
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while creating fresh Model from Database", ex);
        }
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
        if ((name.equals("home") || name.equals(user.getName())) && this.homes.containsKey(user.getName()+":home"))
        {
            return this.homes.get(user.getName()+":home");
        }
        if (attachment.hasHome(name))
        {
            return attachment.getHome(name);
        }
        else if (name.startsWith("public:"))
        {
            name.replaceFirst("public:", "");
            if (publicHomes.containsKey(name))
            {
                Home home = this.publicHomes.get(name);
                if (home.canAccess(user))
                {
                    return home;
                }
            }
            else
            {
                Set<String> matches = Match.string().getBestMatches(name, publicHomes.keySet(), 2);
                if (!matches.isEmpty())
                {
                    Home home = publicHomes.get(matches.iterator().next());
                    if (home.canAccess(user))
                    {
                        return home;
                    }
                }
            }
        }
        else if (name.contains(":"))
        {
            if (homes.containsKey(name))
            {
                Home home = homes.get(name);
                if (home.canAccess(user))
                {
                    this.putHomeToUser(home, user);
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
            if (homes.containsKey(user.getName() + ":" + name))
            {
                Home home = homes.get(user.getName() + ":" + name);
                this.putHomeToUser(home, user);
                return home;
            }
            else if (publicHomes.containsKey(name))
            {
                Home home = this.publicHomes.get(name);
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

                Set<String> publicMatches = Match.string().getBestMatches(name, publicHomes.keySet(), 2);
                if (!publicMatches.isEmpty())
                {
                    Home home = publicHomes.get(publicMatches.iterator().next());
                    if (home.canAccess(user))
                    {
                        return home;
                    }
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
     * this have to be in the format owner:home if the home is not public
     *
     * @return the home if found
     */
    public Home getHome(String name)
    {
        Home home = homes.get(name);
        if (home == null)
        {
            home = publicHomes.get(name);
        }
        return home;
    }

    public int getNumberOfHomes(User user)
    {
        int homes = 0;
        for (Home home : this.homes.values())
        {
            if (home.isOwner(user))
            {
                homes++;
            }
        }
        for (Home home : this.publicHomes.values())
        {
            if (home.isOwner(user))
            {
                homes++;
            }
        }
        return homes;
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
        Set<Home> prefixed = new HashSet<Home>();
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
    public Home createHome(Location location, String name, User owner, TeleportPoint.Visibility visibility)
    {
        Home home = new Home(new TeleportPoint(location, name, owner, null, HOME, visibility), this, inviteManager, this.module);
        this.store(home.getModel());
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
        if (home.getVisibility() == TeleportPoint.Visibility.PRIVATE)
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
        this.delete(home.getModel());
        this.homes.remove(home.getStorageName());
        if (home.isPublic())
        {
            this.publicHomes.remove(home.getName());
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
            if (warps.containsKey(user.getName() + ":" + name))
            {
                Warp warp = warps.get(user.getName() + ":" + name);
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
        Set<String> warps = new HashSet<String>();
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
    public Warp createWarp(Location location, String name, User owner, TeleportPoint.Visibility visibility)
    {
        Warp warp = new Warp(new TeleportPoint(location, name, owner, null, TeleportPoint.Type.WARP, visibility),
                this, inviteManager, this.module);
        this.store(warp.getModel());
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
        this.delete(warp.getModel());
    }

    public final int ALL = -1;
    public final int PUBLIC = 1 << 0;
    public final int PRIVATE = 1 << 1;
    public final int OWNED = 1 << 2;
    public final int INVITED = 1 << 3;

    public Set<Home> listHomes(int mask)
    {
        Set<Home> homes = new HashSet<Home>();
        if ((mask & PUBLIC) == PUBLIC)
        {
            try
            {
                ResultSet resultSet = database.preparedQuery(this.modelClass, "filtered_getAll", HOME.ordinal(), Visibility.PUBLIC.ordinal());
                homes.addAll(this.getHomes(resultSet));
                resultSet.close();
            }
            catch (SQLException e)
            {
                throw new StorageException("Failed getting all public homes", e);
            }
        }

        if ((mask & PRIVATE) == PRIVATE)
        {
            try
            {
                ResultSet resultSet = database.preparedQuery(this.modelClass, "filtered_getAll", HOME.ordinal(), Visibility.PRIVATE.ordinal());
                homes.addAll(this.getHomes(resultSet));
                resultSet.close();
            }
            catch (SQLException e)
            {
                throw new StorageException("Failed getting all private homes", e);
            }
        }
        return homes;
    }

    public Set<Home> listHomes(User user, int mask)
    {
        Set<Home> homes = new HashSet<Home>();
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
            try
            {
                ResultSet resultSet = database.preparedQuery(this.modelClass, "filtered_getAll_owned", HOME.ordinal(), Visibility.PUBLIC.ordinal(), user.getId());
                homes.addAll(this.getHomes(resultSet));
                resultSet.close();
            }
            catch (SQLException e)
            {
                throw new StorageException("Failed getting public homes owned by " + user.getName(), e);
            }
        }
        if ((mask & (PRIVATE | OWNED)) == (PRIVATE | OWNED))
        {
            try
            {
                ResultSet resultSet = database.preparedQuery(this.modelClass, "filtered_getAll_owned", HOME.ordinal(), Visibility.PRIVATE.ordinal(), user.getId());
                homes.addAll(this.getHomes(resultSet));
                resultSet.close();
            }
            catch (SQLException e)
            {
                throw new StorageException("Failed getting private homes owned by " + user.getName(), e);
            }
        }
        if ((mask & INVITED) == INVITED)
        {
            for (TeleportInvite invite : this.inviteManager.getInvites(user))
            {
                TeleportPoint point = this.get(invite.teleportPoint);
                if (point.type == HOME)
                {
                    Home home = this.homes.get(point.ownerName + ":" + point.name);
                    if (home != null)
                    {
                        if ((home.isPublic() && (mask & PUBLIC) == PUBLIC) || (!home
                            .isPublic() && (mask & PRIVATE) == PRIVATE))
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
        Set<Warp> warps = new HashSet<Warp>();
        if ((mask & PUBLIC) == PUBLIC)
        {
            try
            {
                ResultSet resultSet = database.preparedQuery(this.modelClass, "filtered_getAll", WARP.ordinal(), Visibility.PUBLIC.ordinal());
                warps.addAll(this.getWarps(resultSet));
                resultSet.close();
            }
            catch (SQLException e)
            {
                throw new StorageException("Failed getting all public warps", e);
            }
        }

        if ((mask & PRIVATE) == PRIVATE)
        {
            try
            {
                ResultSet resultSet = database.preparedQuery(this.modelClass, "filtered_getAll", WARP.ordinal(), Visibility.PRIVATE.ordinal());
                warps.addAll(this.getWarps(resultSet));
                resultSet.close();
            }
            catch (SQLException e)
            {
                throw new StorageException("Failed getting all private warps", e);
            }
        }
        return warps;
    }

    public Set<Warp> listWarps(User user, int mask)
    {
        Set<Warp> warps = new HashSet<Warp>();
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
            try
            {
                ResultSet resultSet = database.preparedQuery(this.modelClass, "filtered_getAll_owned", WARP.ordinal(), Visibility.PUBLIC.ordinal(), user.getId());
                warps.addAll(this.getWarps(resultSet));
                resultSet.close();
            }
            catch (SQLException e)
            {
                throw new StorageException("Failed getting public warps owned by " + user.getName(), e);
            }
        }
        if ((mask & (PRIVATE | OWNED)) == (PRIVATE | OWNED))
        {
            try
            {
                ResultSet resultSet = database.preparedQuery(this.modelClass, "filtered_getAll_owned", WARP.ordinal(), Visibility.PRIVATE.ordinal(), user.getId());
                warps.addAll(this.getWarps(resultSet));
                resultSet.close();
            }
            catch (SQLException e)
            {
                throw new StorageException("Failed getting private warps owned by " + user.getName(), e);
            }
        }
        if ((mask & INVITED) == INVITED)
        {
            for (TeleportInvite invite : this.inviteManager.getInvites(user))
            {
                TeleportPoint point = this.get(invite.teleportPoint);
                if (point.type == HOME)
                {
                    Warp warp = this.warps.get(point.ownerName + ":" + point.name);
                    if (warp != null)
                    {
                        if ((warp.isPublic() && (mask & PUBLIC) == PUBLIC) || (!warp
                            .isPublic() && (mask & PRIVATE) == PRIVATE))
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

    private Set<Home> getHomes(ResultSet resultSet) throws SQLException
    {
        Set<Home> homes = new HashSet<Home>();
        while (resultSet.next())
        {
            String name = resultSet.getString("name");
            String ownerName = resultSet.getString("player");
            Home home;
            if (ownerName == null)
            {
                home = this.homes.get("Unknown" + ":" + name);
            }
            else
            {
                home = this.homes.get(ownerName + ":" + name);
            }
            homes.add(home);
        }
        return homes;
    }

    private Set<Warp> getWarps(ResultSet resultSet) throws SQLException
    {
        Set<Warp> warps = new HashSet<Warp>();
        while (resultSet.next())
        {
            String name = resultSet.getString("name");
            String ownerName = resultSet.getString("player");
            int visibility = resultSet.getInt("visibility");
            Warp warp;
            if (visibility == TeleportPoint.Visibility.PRIVATE.ordinal())
            {
                warp = this.warps.get(ownerName + ":" + name);
            }
            else
            {
                warp = this.warps.get("public:" + name);
            }
            warps.add(warp);
        }
        return warps;
    }
}