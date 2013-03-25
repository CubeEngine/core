package de.cubeisland.cubeengine.travel.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Location;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.travel.Travel;

import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.EQUAL;

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
    public void initialize()
    {
        try
        {
            super.initialize();
            QueryBuilder builder = this.database.getQueryBuilder();

            // List statements
            this.database.storeStatement(this.modelClass, "homes_all_public", builder.select().cols("key", "name", "owner").from(this.tableName)
                    .where().field("visibility").is(EQUAL)
                    .value(TeleportPoint.Visibility.PUBLIC.ordinal()).and().field("type").is(EQUAL).value(TeleportPoint.Type.HOME.ordinal()).end().end());
            this.database.storeStatement(this.modelClass, "homes_all_private", builder.select().cols("key", "name", "owner").from(this.tableName)
                    .where().field("visibility").is(EQUAL).value(TeleportPoint.Visibility.PRIVATE.ordinal())
                    .and().field("type").is(EQUAL).value(TeleportPoint.Type.HOME.ordinal()).end().end());
            this.database.storeStatement(this.modelClass, "homes_owned_private", builder.select().cols("key", "name", "owner").from(this.tableName)
                    .where().field("owner").is(EQUAL).value().and().field("visibility").is(EQUAL)
                    .value(TeleportPoint.Visibility.PRIVATE.ordinal()).and().field("type").is(EQUAL)
                    .value(TeleportPoint.Type.HOME.ordinal()).end().end());
            this.database.storeStatement(this.modelClass, "homes_owned_public",builder.select().cols("key", "name", "owner").from(this.tableName)
                    .where().field("owner").is(EQUAL).value().and().field("visibility").is(EQUAL)
                    .value(TeleportPoint.Visibility.PUBLIC.ordinal()).and().field("type").is(EQUAL)
                    .value(TeleportPoint.Type.HOME.ordinal()).end().end());
            this.database.storeStatement(this.modelClass, "warps_all_public", builder.select().cols("key", "name", "owner", "visibility").from(this.tableName)
                    .where().field("visibility").is(EQUAL)
                    .value(TeleportPoint.Visibility.PUBLIC.ordinal()).and().field("type").is(EQUAL).value(TeleportPoint.Type.WARP.ordinal()).end().end());
            this.database.storeStatement(this.modelClass, "warps_all_private", builder.select().cols("key", "name", "visibility").from(this.tableName)
                    .where().field("visibility").is(EQUAL).value(TeleportPoint.Visibility.PRIVATE.ordinal())
                    .and().field("type").is(EQUAL).value(TeleportPoint.Type.WARP.ordinal()).end().end());
            this.database.storeStatement(this.modelClass, "warps_owned_private", builder.select().cols("key", "name", "owner", "visibility").from(this.tableName)
                    .where().field("owner").is(EQUAL).value().and().field("visibility").is(EQUAL)
                    .value(TeleportPoint.Visibility.PRIVATE.ordinal()).and().field("type").is(EQUAL)
                    .value(TeleportPoint.Type.WARP.ordinal()).end().end());
            this.database.storeStatement(this.modelClass, "warps_owned_public",builder.select().cols("key", "name", "owner", "visibility").from(this.tableName)
                    .where().field("owner").is(EQUAL).value().and().field("visibility").is(EQUAL)
                    .value(TeleportPoint.Visibility.PUBLIC.ordinal()).and().field("type").is(EQUAL)
                    .value(TeleportPoint.Type.WARP.ordinal()).end().end());

            // Other statements
            this.database.storeStatement(this.modelClass, "get_name",
                    builder.select().cols("name").from(this.tableName).where().field("key").is(EQUAL).value().end().end());
            this.database.storeStatement(User.class, "get_owner",
                    builder.select().cols("owner").from(this.tableName).where().field("key").is(EQUAL).value()
                            .end().end());

        }
        catch (SQLException ex)
        {
            module.getLogger().log(LogLevel.ERROR, "An error occurred while preparing the database statements");
            module.getLogger().log(LogLevel.DEBUG, "The error was: {0}", ex.getMessage());
            module.getLogger().log(LogLevel.DEBUG, "This is the stack: ", ex);
        }
    }

    /**
     * Load all warps and homes
     *
     * @param inviteManager
     */
    public void load(InviteManager inviteManager)
    {
        this.inviteManager = inviteManager;
        for (TeleportPoint teleportPoint : this.getAll())
        {
            if (teleportPoint.type.equals(TeleportPoint.Type.HOME))
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
                Warp warp = new Warp(teleportPoint, this, inviteManager);
                warps.put(warp.getStorageName(), warp);
            }
        }
    }

    /**
     * Get a home by name relative to an user
     * The name can be edit distance <= 2 away from a home name
     *
     * the name can be just the name of the home, or owner:name
     *
     * @param user  the user
     * @param name  the name of the home relative to the user
     * @return The home if found, else null
     */
    public Home getHome(User user, String name)
    {
        if (name == null || user == null)
        {
            return null;
        }

        HomeAttachment attachment = user.attachOrGet(HomeAttachment.class, this.module);
        if (attachment.hasHome(name))
        {
            return attachment.getHome(name);
        }
        else if (name.startsWith("public:"))
        {
            name.replaceFirst("public:", "");
            if (publicHomes.containsKey(name))
            {
                Home home =  this.publicHomes.get(name);
                if (home.canAccess(user)) return home;
            }
            else
            {
                Set<String> matches = Match.string().getBestMatches(name, publicHomes.keySet(), 2);
                if (!matches.isEmpty())
                {
                    Home home =  publicHomes.get(matches.iterator().next());
                    if (home.canAccess(user)) return home;
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
                if (home.canAccess(user)) return home;
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
                    Home home = publicHomes.get(matches.iterator().next());
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
     * this have to be in the format owner:home if the home is not public
     *
     * @param name
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

    /**
     * If a home by that name relative to the user exist
     * @param name the name relative to the user
     * @param user the user
     * @return
     */
    public boolean hasHome(String name, User user)
    {
        Home home = this.getHome(user, name);
        return home != null && home.getName().equals(name);
    }

    /**
     * Put a home in an users home storage
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
        if (prefixed.size() == 1)
        {
            for (Home h : prefixed)
            {
                attachment.addHome(home.getName(), h);
            }
        }
    }

    /**
     * Create a home
     *
     * Obs: This does not add it to the home storage, nor the database
     * @param location
     * @param name
     * @param owner
     * @param visibility
     * @return the newly generated home
     */
    public Home createHome(Location location, String name, User owner, TeleportPoint.Visibility visibility)
    {
        Home home = new Home(new TeleportPoint(location, name, owner, null, TeleportPoint.Type.HOME, visibility), this, inviteManager, this.module);
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
     *
     * @param home
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
                removeHomeFromUser(home,  user);
            }
        }
        this.delete(home.getModel());
        this.homes.remove(home.getStorageName());
        if (home.isPublic())
        {
            this.publicHomes.remove(home.getName());
        }
    }

    /**
     * Get a warp by name relative to an user
     * @param user  the user
     * @param name  the name relative to the user
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
     * @param name
     * @return
     */
    public Warp getWarp(String name)
    {
        if (name.contains(":"))
        {
            return this.warps.get(name);
        } else
        {
            return this.warps.get("public:" + name);
        }
    }

    /**
     * If a warp by that name exist or not.
     * @param name the name in this format: ["public",owner]:home
     * @return
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
     * @return
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
     *
     * Obs: This does not add it to the warp storage, nor the database
     *
     * @param location
     * @param name
     * @param owner
     * @param visibility
     * @return the newly generated warp
     */
    public Warp createWarp(Location location, String name, User owner, TeleportPoint.Visibility visibility)
    {
        Warp warp = new Warp(new TeleportPoint(location, name, owner, null, TeleportPoint.Type.WARP, visibility), this, inviteManager);
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
     * @param warp
     * @param name
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
     * @param warp
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
                ResultSet resultSet = database.preparedQuery(this.modelClass, "homes_all_public");
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
                ResultSet resultSet = database.preparedQuery(this.modelClass, "homes_all_private");
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
            try {
                ResultSet resultSet = database.preparedQuery(this.modelClass, "homes_owned_public", user.getKey());
                homes.addAll(this.getHomes(resultSet));
                resultSet.close();
            } catch (SQLException e) {
                throw new StorageException("Failed getting public homes owned by " + user.getName(), e);
            }
        }
        if ((mask & (PRIVATE | OWNED)) == (PRIVATE | OWNED))
        {
            try {
                ResultSet resultSet = database.preparedQuery(this.modelClass, "homes_owned_private", user.getKey());
                homes.addAll(this.getHomes(resultSet));
                resultSet.close();
            } catch (SQLException e) {
                throw new StorageException("Failed getting private homes owned by " + user.getName(), e);
            }
        }
        if ((mask & INVITED) == INVITED)
        {
            for (TeleportInvite invite : this.inviteManager.getInvites(user))
            {
                TeleportPoint point = this.get(invite.teleportPoint);
                if (point.type == TeleportPoint.Type.HOME)
                {
                    Home home = this.homes.get(point.owner.getName() + ":" + point.name);
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
        Set<Warp> warps = new HashSet<Warp>();
        if ((mask & PUBLIC) == PUBLIC)
        {
            try
            {
                ResultSet resultSet = database.preparedQuery(this.modelClass, "warps_all_public");
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
                ResultSet resultSet = database.preparedQuery(this.modelClass, "warps_all_private");
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
            try {
                ResultSet resultSet = database.preparedQuery(this.modelClass, "warps_owned_public", user.getKey());
                warps.addAll(this.getWarps(resultSet));
                resultSet.close();
            } catch (SQLException e) {
                throw new StorageException("Failed getting public warps owned by " + user.getName(), e);
            }
        }
        if ((mask & (PRIVATE | OWNED)) == (PRIVATE | OWNED))
        {
            try {
                ResultSet resultSet = database.preparedQuery(this.modelClass, "warps_owned_private", user.getKey());
                warps.addAll(this.getWarps(resultSet));
                resultSet.close();
            } catch (SQLException e) {
                throw new StorageException("Failed getting private warps owned by " + user.getName(), e);
            }
        }
        if ((mask & INVITED) == INVITED)
        {
            for (TeleportInvite invite : this.inviteManager.getInvites(user))
            {
                TeleportPoint point = this.get(invite.teleportPoint);
                if (point.type == TeleportPoint.Type.HOME)
                {
                    Warp warp = this.warps.get(point.owner.getName() + ":" + point.name);
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

    private Set<Home> getHomes(ResultSet resultSet) throws SQLException {
        Set<Home> homes = new HashSet<Home>();
        while(resultSet.next())
        {
            String name = resultSet.getString("name");
            long ownerId = resultSet.getLong("owner");
            User owner = CubeEngine.getUserManager().getUser(ownerId);
            Home home = this.homes.get(owner.getName() + ":" + name);
            homes.add(home);
        }
        return homes;
    }

    private Set<Warp> getWarps(ResultSet resultSet) throws SQLException {
        Set<Warp> warps = new HashSet<Warp>();
        while(resultSet.next())
        {
            String name = resultSet.getString("name");
            long ownerId = resultSet.getLong("owner");
            int visibility = resultSet.getInt("visibility");

            User owner = CubeEngine.getUserManager().getUser(ownerId);
            Warp warp;
            if (visibility == TeleportPoint.Visibility.PRIVATE.ordinal())
            {
                warp = this.warps.get(owner.getName() + ":" + name);
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
