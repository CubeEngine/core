package de.cubeisland.cubeengine.travel.storage;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.StorageException;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.QueryBuilder;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.travel.Travel;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static de.cubeisland.cubeengine.core.storage.database.querybuilder.ComponentBuilder.EQUAL;

public class TelePointManager extends SingleKeyStorage<Long, TeleportPoint>
{
    private static final int REVISION = 6;

    private InviteManager inviteManager;
    private final Map<String, Home> homes;
    private final Map<String, Home> publicHomes;
    private final Map<String, Warp> warps;
    private final Travel module;

    public TelePointManager(Database database, Travel module)
    {
        super(database, TeleportPoint.class, REVISION);
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
            this.database.storeStatement(this.modelClass, "listHomesOfUser",
                    builder.select().cols("key", "name").from(this.tableName).where().field("owner").is(EQUAL).value().end().end());
            this.database.storeStatement(this.modelClass, "listAllHomes",
                    builder.select().cols("name").from(this.tableName).end().end());
            this.database.storeStatement(this.modelClass, "listAllPublicHomes",
                    builder.select().cols("name").from(this.tableName).where().field("visibility").is(EQUAL).value("PUBLIC").end().end());
            this.database.storeStatement(this.modelClass, "listPublicHomesOfUser",
                    builder.select().cols("name").from(this.tableName).where().field("owner").is(EQUAL).value()
                            .and().field("visibility").is(EQUAL).value("PUBLIC").end().end());
            this.database.storeStatement(this.modelClass, "getName",
                    builder.select().cols("name").from(this.tableName).where().field("key").is(EQUAL).value().end().end());
            this.database.storeStatement(User.class, "getOwner",
                    builder.select().cols("owner").from(this.tableName).where().field("key").is(EQUAL).value()
                            .end().end());
            this.database.storeStatement(this.modelClass, "deleteAllHomes",
                    builder.deleteFrom(this.tableName).end().end());
            this.database.storeStatement(this.modelClass, "deletePublicHomes",
                    builder.deleteFrom(this.tableName).where().field("visibility").is(EQUAL)
                            .value(TeleportPoint.Visibility.PUBLIC.toString()).end().end());
            this.database.storeStatement(this.modelClass, "deleteAllUserHomes",
                    builder.deleteFrom(this.tableName).where().field("owner").is(EQUAL).value().end().end());
            this.database.storeStatement(this.modelClass, "deletePublicUserHomes",
                    builder.deleteFrom(this.tableName).where().field("owner").is(EQUAL).value()
                            .and().field("visibility").is(EQUAL).value(TeleportPoint.Visibility.PUBLIC.toString()).end().end());

        }
        catch (SQLException ex)
        {
            module.getLogger().log(LogLevel.ERROR, "An error occurred while preparing the database statements");
            module.getLogger().log(LogLevel.DEBUG, "The error was: {0}", ex.getMessage());
            module.getLogger().log(LogLevel.DEBUG, "This is the stack: ", ex);
        }
    }

    public void load(InviteManager inviteManager)
    {
        this.inviteManager = inviteManager;
        for (TeleportPoint teleportPoint : this.getAll())
        {
            if (teleportPoint.type.equals(TeleportPoint.Type.HOME))
            {
                Home home = new Home(teleportPoint, this, inviteManager);
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

    public Home getHome(User user, String name)
    {
        if (name == null || user == null)
        {
            return null;
        }

        if (user.getAttribute(module, "homes") == null)
        {
            user.setAttribute(module, "homes", new HashMap<String, Home>());
        }
        HashMap<String, Home> userHomes = user.getAttribute(module, "homes");


        if (name.startsWith("public:"))
        {
            name.replaceFirst("public:", "");
            if (publicHomes.containsKey(name))
            {
                return this.publicHomes.get(name);
            }
            else
            {
                Set<String> matches = Match.string().getBestMatches(name, publicHomes.keySet(), 2);
                if (!matches.isEmpty())
                {
                    return publicHomes.get(matches.iterator().next());
                }
            }

        }
        else if (userHomes.containsKey(name))
        {
            return userHomes.get(name);
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
                Set<String> matches = Match.string().getBestMatches(name, userHomes.keySet(), 2);
                if (!matches.isEmpty())
                {
                    return userHomes.get(matches.iterator().next());
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
                return this.publicHomes.get(name);
            }
            else
            {
                Set<String> matches = Match.string().getBestMatches(name, userHomes.keySet(), 2);
                if (!matches.isEmpty())
                {
                    return userHomes.get(matches.iterator().next());
                }

                Set<String> publicMatches = Match.string().getBestMatches(name, publicHomes.keySet(), 2);
                if (!publicMatches.isEmpty())
                {
                    return publicHomes.get(matches.iterator().next());
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

    public Warp getWarp(User user, String name)
    {
        if (user == null || name == null)
        {
            return null;
        }

        if (user.getAttribute(module, "warps") == null)
        {
            user.setAttribute(module, "warps", new HashMap<String, Warp>());
        }
        HashMap<String, Warp> userWarps = user.getAttribute(module, "warps");

        if (userWarps.containsKey(name))
        {
            return userWarps.get(name);
        }
        else if (userWarps.containsKey("public:" + name))
        {
            return userWarps.get("public:" + name);
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
                Set<String> matches = Match.string().getBestMatches(name, userWarps.keySet(), 2);
                if (!matches.isEmpty())
                {
                    return userWarps.get(matches.iterator().next());
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
                Set<String> matches = Match.string().getBestMatches(name, userWarps.keySet(), 2);
                if (!matches.isEmpty())
                {
                    return userWarps.get(matches.iterator().next());
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

    public boolean hasHome(String name, User user)
    {
        Home home = this.getHome(user, name);
        return home != null && home.getName().equals(name);
    }

    public void removeHomeFromUser(Home home, User user)
    {
        if (user.getAttribute(module, "homes") == null)
        {
            user.setAttribute(module, "homes", new HashMap<String, Home>());
        }
        Map<String, Home> homes = user.getAttribute(module, "homes");

        // Remove the home from the users list
        if (homes.get(home.getName()).equals(home))
        {
            homes.remove(home.getName());
        }
        else if (homes.get(home.getStorageName()).equals(home))
        {
            homes.remove(home.getStorageName());
        }

        // If another home the player has can be taken away it's prefix, do it
        Set<Home> prefixed = new HashSet<Home>();
        for (String name : homes.keySet())
        {
            String[] parts = name.split(":");
            if (parts.length == 2)
            {
                if (parts[1].equals(home.getName()))
                {
                    prefixed.add(homes.get(name));
                }
            }
        }
        if (prefixed.size() == 1)
        {
            for (Home h : prefixed)
            {
                homes.put(home.getName(), h);
            }
        }
    }

    public void putHomeToUser(Home home, User user)
    {
        if (user.getAttribute(module, "homes") == null)
        {
            user.setAttribute(module, "homes", new HashMap<String, Home>());
        }
        Map<String, Home> homes = user.getAttribute(module, "homes");

        if (homes.containsKey(home.getName()))
        {
            Home rename = homes.get(home.getName());
            if (!rename.isOwner(user) && home.isOwner(user))
            {
                homes.put(home.getName(), home);
            }
            else
            {
                homes.put(home.getStorageName(), home);
            }
            homes.put(rename.getStorageName(), rename);
        }
        else
        {
            if (home.isOwner(user))
            {
                homes.put(home.getName(), home);
            }
            else
            {
                homes.put(home.getStorageName(), home);
            }
        }
    }

    public void putWarpToUser(Warp warp, User user)
    {
        if (user.getAttribute(module, "warps") == null)
        {
            user.setAttribute(module, "warps", new HashMap<String, Warp>());
        }
        HashMap<String, Warp> userWarps = user.getAttribute(module, "warps");

        if (userWarps.containsKey(warp.getName()))
        {
            if (warp.isPublic() || warp.isOwner(user))
            {
                Warp rename = userWarps.get(warp.getName());
                userWarps.put(warp.getName(), warp);
                userWarps.put(rename.getStorageName(), warp);
            }
            else
            {
                userWarps.put(warp.getStorageName(), warp);
            }
        }
        else
        {
            userWarps.put(warp.getName(), warp);
        }
    }

    public void removeWarpFromUser(Warp warp, User user)
    {
        if (user.getAttribute(module, "warps") == null)
        {
            user.setAttribute(module, "warps", new HashMap<String, Warp>());
        }
        HashMap<String, Warp> userWarps = user.getAttribute(module, "warps");

        if (userWarps.containsKey(warp.getName()))
        {
            if (userWarps.get(warp.getName()).equals(warp))
            {
                userWarps.remove(warp.getName());
            }
        }
        else if (userWarps.containsKey(warp.getStorageName()))
        {
            if (userWarps.get(warp.getStorageName()).equals(warp))
            {
                warps.remove(warp.getStorageName());
            }
        }
    }

    public Home createHome(Location location, String name, User owner, TeleportPoint.Visibility visibility)
    {
        Home home = new Home(new TeleportPoint(location, name, owner, null, TeleportPoint.Type.HOME, visibility), this, inviteManager);
        this.store(home.getModel());
        this.putHomeToUser(home, home.getOwner());
        this.homes.put(home.getStorageName(), home);
        for (User user : home.getInvitedUsers())
        {
            this.putHomeToUser(home, user);
        }
        return home;
    }

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
            for (User user : CubeEngine.getUserManager().getOnlineUsers())
            {
                for (Object object : user.getAttributes(module))
                {
                    if (object instanceof String)
                    {
                        String string = (String)object;
                        if (string.equalsIgnoreCase(home.getName()) || string.equalsIgnoreCase(home.getStorageName()))
                        {
                            user.removeAttribute(module, string);
                        }
                    }
                }
            }
        }
        this.delete(home.getModel());
        this.homes.remove(home.getStorageName());
        if (home.isPublic())
        {
            this.publicHomes.remove(home.getName());
        }
    }

    public void deleteAllHomes()
    {
        try
        {
            if (!this.database.preparedExecute(this.modelClass, "deleteAllHomes"))
            {
                throw new StorageException("Deletion of all homes returned false");
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("could not delete the homes on the server", ex);
        }
    }

    public void deleteAllHomes(User user)
    {
        try
        {
            if (!this.database.preparedExecute(this.modelClass, "deleteAllUserHomes", user.getKey()))
            {
                throw new StorageException("Deletion of all homes of user returned false");
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("could not delete the homes that belongs to an user on the server", ex);
        }
    }

    public void deletePublicHomes(User user)
    {
        try
        {
            if (!this.database.preparedExecute(this.modelClass, "deletePublicHomes"))
            {
                throw new StorageException("Deletion of all public homes returned false");
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("could not delete the public homes on the server", ex);
        }
    }

    public Home getHome(String name)
    {
        return homes.get(name);
    }

    public Set<String> listOwnedHomes(User user)
    {
        Set<String> homes = new HashSet<String>();
        try
        {
            ResultSet ownedHomes = this.database.preparedQuery(this.modelClass, "listHomesOfUser", user.getKey());
            while (ownedHomes.next())
            {
                String name = ownedHomes.getString("name");
                if (name != null)
                {
                    homes.add(name);
                }
            }

        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not get a list of the homes of an user", ex);
        }
        return homes;
    }

    public Set<String> listInvitedHomes(User user)
    {
        Set<String> homes = new HashSet<String>();
        try
        {
            Set<Long> invitedHomes = this.inviteManager.getInvitedTo(user);
            for (Long home : invitedHomes)
            {
                StringBuilder longName = new StringBuilder();
                ResultSet ownerIds = this.database.preparedQuery(User.class, "getOwner", home);
                if (!ownerIds.next())
                {
                    continue;
                }
                long ownerId = ownerIds.getLong("owner");
                User owner = CubeEngine.getUserManager().get(ownerId);
                if (owner != null)
                {
                    longName.append(owner.getName()).append(':');

                    ResultSet names = this.database.preparedQuery(this.modelClass, "getName", home);
                    if (names.next())
                    {
                        String name = names.getString("name");
                        if (name != null)
                        {
                            homes.add(longName.append(name).toString());
                        }
                    }
                }
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not get a list of the homes of an user", ex);
        }
        return homes;
    }

    public Set<String> listAvailableHomes(User user)
    {
        Set<String> homes = new HashSet<String>();
        homes.addAll(listOwnedHomes(user));
        homes.addAll(listInvitedHomes(user));
        homes.addAll(listAllPublicHomes(true));
        return homes;
    }

    public Set<String> listAllHomes()
    {
        Set<String> homes = new HashSet<String>();
        try
        {
            ResultSet allHomes = this.database.preparedQuery(this.modelClass, "listAllHomes");
            while (allHomes.next())
            {
                String name = allHomes.getString("name");
                if (name != null)
                {
                    homes.add(name);
                }
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not get a list of the homes of an user", ex);
        }
        return homes;
    }

    public Set<String> listAllPublicHomes()
    {
        return this.listAllPublicHomes(false);
    }

    public Set<String> listAllPublicHomes(boolean prefixed)
    {
        Set<String> homes = new HashSet<String>();
        try
        {
            ResultSet allHomes = this.database.preparedQuery(this.modelClass, "listAllPublicHomes");
            while (allHomes.next())
            {
                String name = allHomes.getString("name");
                if (name != null)
                {
                    if (prefixed)
                    {
                        homes.add("public:" + name);
                    }
                    else
                    {
                        homes.add(name);
                    }
                }
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not get a list of the homes of an user", ex);
        }
        return homes;
    }

    public Set<String> listPublicOwnedHomes(User user)
    {
        Set<String> homes = new HashSet<String>();
        try
        {
            ResultSet ownedHomes = this.database.preparedQuery(this.modelClass, "listPublicHomesOfUser", user.getKey());
            while (ownedHomes.next())
            {
                String name = ownedHomes.getString("name");
                if (name != null)
                {
                    homes.add(name);
                }
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("Could not get a list of the homes of an user", ex);
        }
        return homes;
    }

    public Set<Pair<String, Set<String>>> listHomesAndInvited(User user)
    {
        Set<Pair<String, Set<String>>> homesInvites = new HashSet<Pair<String, Set<String>>>();
        try
        {
            ResultSet homes = database.preparedQuery(this.modelClass, "listHomesOfUser", user.getKey());
            while (homes.next())
            {
                String name = homes.getString("name");
                Long home =  homes.getLong("key");
                Set<String> invites = inviteManager.getInvited(this.get(home));
                homesInvites.add(new Pair<String, Set<String>>(name, invites));
            }
        }
        catch (SQLException ex)
        {
            throw new StorageException("could not fetch the homes owned by " + user.getName(), ex);     
        }
        return homesInvites;
    }

    public Home findPublicHome(String name)
    {
        return this.publicHomes.get(name);
    }

    public boolean hasWarp(String name)
    {
        return getWarp(name) != null;
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

    public void deleteWarp(Warp warp)
    {
        for (User user : CubeEngine.getUserManager().getOnlineUsers())
        {
            for (Object object : user.getAttributes(module))
            {
                if (object instanceof String)
                {
                    String string = (String)object;
                    if (string.equalsIgnoreCase(warp.getName()) || string.equalsIgnoreCase(warp.getStorageName()))
                    {
                        user.removeAttribute(module, string);
                    }
                }
            }
        }
        this.warps.remove(warp.getStorageName());
        this.delete(warp.getModel());
    }

    public void renameWarp(Warp warp, String name)
    {
        this.warps.remove(warp.getStorageName());
        for (User user : CubeEngine.getUserManager().getOnlineUsers())
        {
            for (Object object : user.getAttributes(module))
            {
                if (object instanceof String)
                {
                    String string = (String)object;
                    if (string.equalsIgnoreCase(warp.getName()) || string.equalsIgnoreCase(warp.getStorageName()))
                    {
                        user.removeAttribute(module, string);
                    }
                }
            }
        }
        warp.setName(name);
        this.warps.put(warp.getStorageName(), warp);

    }

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
}
