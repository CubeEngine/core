package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.bukkit.BukkitUtils;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.i18n.Language;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Index;
import de.cubeisland.cubeengine.core.storage.database.SingleKeyEntity;
import de.cubeisland.cubeengine.core.attachment.AttachmentHolder;
import de.cubeisland.cubeengine.core.attachment.UserAttachment;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import gnu.trove.map.hash.THashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.BlockIterator;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import static de.cubeisland.cubeengine.core.i18n.I18n._;
import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;
import static de.cubeisland.cubeengine.core.storage.database.Index.IndexType.UNIQUE;

/**
 * A CubeEngine User (can exist offline too).
 */
@SingleKeyEntity(tableName = "user", primaryKey = "key", autoIncrement = true, indices = {
    @Index(value = UNIQUE, fields = "player")
})
public class User extends UserBase implements Model<Long>, CommandSender, AttachmentHolder<UserAttachment>
{
    public static Long NO_ID = -1L;
    @Attribute(type = AttrType.INT, unsigned = true)
    public Long key;
    @Attribute(type = AttrType.VARCHAR, length = 16)
    public final String player;
    @Attribute(type = AttrType.BOOLEAN)
    public boolean nogc = false;
    @Attribute(type = AttrType.DATETIME)
    public Timestamp lastseen;
    @Attribute(type = AttrType.VARBINARY, length = 128, notnull = false)
    public byte[] passwd;
    @Attribute(type = AttrType.DATETIME)
    public final Timestamp firstseen;
    @Attribute(type = AttrType.VARCHAR, length = 5, notnull = false)
    public String language = null;
    private boolean isLoggedIn = false;
    private final Map<Class<? extends UserAttachment>, UserAttachment> attachments;

    // TODO we might move this to the UserManager
    Integer removalTaskId; // only used in UserManager no AccessModifier is intended
    private final static MessageDigest hasher;

    static
    {
        try
        {
            hasher = MessageDigest.getInstance("SHA-512");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("SHA-512 hash algorithm not available!");
        }
    }

    @DatabaseConstructor
    public User(List<Object> args) throws ConversionException
    {
        super(Bukkit.getOfflinePlayer((String)args.get(1)));
        this.key = (Long)args.get(0);
        this.player = this.offlinePlayer.getName();
        this.nogc = (Boolean)args.get(2);
        this.lastseen = (Timestamp)args.get(3);
        this.firstseen = (Timestamp)args.get(3);
        this.passwd = (byte[])args.get(4);
        this.attachments = new THashMap<Class<? extends UserAttachment>, UserAttachment>();
    }

    User(Long key, OfflinePlayer player)
    {
        super(player);
        this.key = key;
        this.player = player.getName();
        this.lastseen = new Timestamp(System.currentTimeMillis());
        this.firstseen = this.lastseen;
        this.passwd = new byte[0];
        this.attachments = new THashMap<Class<? extends UserAttachment>, UserAttachment>();
    }

    User(OfflinePlayer player)
    {
        this(NO_ID, player);
    }

    User(String playername)
    {
        this(NO_ID, Bukkit.getOfflinePlayer(playername));
    }

    @Override
    public synchronized UserAttachment addAttachment(Class<UserAttachment> type)
    {
        try
        {
            UserAttachment attachment = type.newInstance();
            attachment.onAttach(this);
            this.attachments.put(type, attachment);
            return attachment;
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("The given attachment could not be created!", e);
        }
    }

    @Override
    public synchronized UserAttachment addOrGetAttachment(Class<UserAttachment> type)
    {
        UserAttachment attachment = this.getAttachment(type);
        if (attachment == null)
        {
            attachment = this.addAttachment(type);
        }
        return attachment;
    }

    @Override
    public synchronized UserAttachment getAttachment(Class<UserAttachment> type)
    {
        return this.attachments.get(type);
    }

    @Override
    public synchronized boolean hasAttachment(Class<UserAttachment> type)
    {
        return this.attachments.containsKey(type);
    }

    @Override
    public synchronized UserAttachment removeAttachment(Class<UserAttachment> type)
    {
        UserAttachment attachment = this.attachments.remove(type);
        if (attachment != null)
        {
            attachment.onDetach();
        }
        return attachment;
    }

    public synchronized void clearAttachments(Module module)
    {
        final Iterator<Entry<Class<? extends UserAttachment>, UserAttachment>> attachmentIt = this.attachments.entrySet().iterator();
        UserAttachment attachment;
        while (attachmentIt.hasNext())
        {
            attachment = attachmentIt.next().getValue();
            if (attachment.getModule() == module)
            {
                attachment.onDetach();
                attachmentIt.remove();
            }
        }
    }

    public synchronized void clearAttachments()
    {
        final Iterator<Entry<Class<? extends UserAttachment>, UserAttachment>> attachmentIt = this.attachments.entrySet().iterator();
        while (attachmentIt.hasNext())
        {
            attachmentIt.next().getValue().onDetach();
            attachmentIt.remove();
        }
    }

    /**
     * @return the offlineplayer
     */
    public OfflinePlayer getOfflinePlayer()
    {
        return this.offlinePlayer;
    }

    @Override
    public Long getKey()
    {
        return this.key;
    }

    @Override
    public void setKey(Long id)
    {
        this.key = id;
    }

    @Override
    public void sendMessage(String string)
    {
        if (string == null)
        {
            return;
        }
        if (!Thread.currentThread().getStackTrace()[1].getClassName().equals(this.getClass().getName()))
        {
            CubeEngine.getLogger().log(DEBUG, "A module sent an untranslated message!");
        }
        super.sendMessage(ChatFormat.parseFormats(string));
    }

    /**
     * Sends a translated Message to this User
     *
     * @param string the message to translate
     * @param params optional parameter
     */
    public void sendMessage(String category, String string, Object... params)
    {
        this.sendMessage(_(this, category, string, params));
    }

    @Override
    public boolean isAuthorized(de.cubeisland.cubeengine.core.permission.Permission perm)
    {
        return this.hasPermission(perm.getPermission());
    }

    /**
     * Returns the users configured language
     *
     * @return a locale string
     */
    public String getLanguage()
    {
        if (this.language != null)
        {
            return this.language;
        }
        String lang = null;
        Player onlinePlayer = this.offlinePlayer.getPlayer();
        if (onlinePlayer != null)
        {
            lang = BukkitUtils.getLanguage(onlinePlayer);
        }
        if (lang == null)
        {
            lang = CubeEngine.getCore().getConfiguration().defaultLanguage;
        }
        return lang;
    }

    public void setLanguage(Language lang)
    {
        this.language = lang.getCode();
    }

    public int getPing()
    {
        Player onlinePlayer = this.offlinePlayer.getPlayer();
        if (onlinePlayer == null)
        {
            return BukkitUtils.getPing(onlinePlayer);
        }
        else
        {
            return -1;
        }
    }

    @Override
    public long getLastPlayed()
    {
        if (this.isOnline())
        {
            return 0;
        }
        return this.lastseen.getTime();
    }

    public void safeTeleport(Location location, TeleportCause cause, boolean keepDirection)
    {
        Location checkLocation = location.clone().add(0, 1, 0);
        while (location.getBlock().getType().isSolid() || checkLocation.getBlock().getType().isSolid())
        {
            location.add(0, 1, 0);
            checkLocation.add(0, 1, 0);
        }
        if (!this.isFlying())
        {
            checkLocation = location.clone();
            while (checkLocation.add(0, -1, 0).getBlock().getType() == Material.AIR)
            {
                location.add(0, -1, 0);
            }
        }
        checkLocation = location.clone().add(0, -1, 0);
        if (checkLocation.getBlock().getType() == Material.STATIONARY_LAVA || checkLocation.getBlock().getType() == Material.LAVA)
        {
            location = location.getWorld().getHighestBlockAt(location).getLocation().add(0, 1, 0); // If would fall in lava tp on highest position.
            // If there is still lava then you shall burn!
        }
        if (location.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.FENCE)
                || location.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.NETHER_FENCE))
        {
            location.add(0, 2, 0);
        }
        if (keepDirection)
        {
            final Location loc = this.getLocation();
            location.setPitch(loc.getPitch());
            location.setYaw(loc.getYaw());
        }
        this.teleport(location, cause);
    }

    public void setPassword(String password)
    {
        // TODO all the password logic should be moved to the user manager I'd say
        synchronized (hasher)
        {
            hasher.reset();
            password += UserManager.salt;
            password += this.firstseen.toString();
            this.passwd = hasher.digest(password.getBytes());
            CubeEngine.getUserManager().update(this);
        }
    }

    public void resetPassword()
    {
        this.passwd = null;
        CubeEngine.getUserManager().update(this);
    }

    public boolean isPasswordSet()
    {
        return this.passwd.length > 0;
    }

    public boolean checkPassword(String password)
    {
        synchronized (hasher)
        {
            hasher.reset();
            password += UserManager.salt;
            password += this.firstseen.toString();
            return Arrays.equals(this.passwd, hasher.digest(password.getBytes()));
        }
    }

    public boolean login(String password)
    {
        if (!this.isLoggedIn)
        {
            this.isLoggedIn = this.checkPassword(password);
        }
        CubeEngine.getEventManager().fireEvent(new UserAuthorizedEvent(CubeEngine.getCore(), this));
        return this.isLoggedIn;
    }

    public void logout()
    {
        this.isLoggedIn = false;
    }

    public boolean isLoggedIn()
    {
        return this.isLoggedIn;
    }

    public void setPermission(String permission, boolean b)
    {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        Permission perm;
        if (b)
        {
            perm = pm.getPermission(this.getName());
        }
        else
        {
            perm = pm.getPermission("!" + this.getName());
        }
        perm.getChildren().put(permission, b);
        this.recalculatePermissions();
    }

    public void setPermission(Map<String, Boolean> permissions)
    {
        this.setPermission(permissions, this.getPlayer());
    }

    /**
     * Use this method to assign permissions to a user while logging in
     *
     * @param permissions a map of permissions
     * @param player the player
     */
    public void setPermission(Map<String, Boolean> permissions, Player player)
    {
        String posName = this.getName();
        String negName = "-" + this.getName();
        PluginManager pm = Bukkit.getServer().getPluginManager();
        Permission posPerm = pm.getPermission(posName);
        Permission negPerm = pm.getPermission(negName);
        Map<String, Boolean> positive;
        Map<String, Boolean> negative;
        if (posPerm == null)
        {
            pm.addPermission(posPerm = new Permission(posName, PermissionDefault.FALSE, new HashMap<String, Boolean>()));
            positive = posPerm.getChildren();
        }
        else
        {
            positive = posPerm.getChildren();
        }
        if (negPerm == null)
        {
            pm.addPermission(negPerm = new Permission(negName, PermissionDefault.FALSE, new HashMap<String, Boolean>()));
            negative = negPerm.getChildren();
        }
        else
        {
            negative = negPerm.getChildren();
        }
        positive.clear();
        negative.clear();
        for (String perm : permissions.keySet())
        {
            if (perm.endsWith("*") && (perm.startsWith("-cubeengine.") || perm.startsWith("cubeengine.")))
            {
                continue;
            }
            if (permissions.get(perm))
            {
                positive.put(perm, true);
            }
            else
            {
                negative.put(perm, false);
            }
        }
        PermissionAttachment attachment = null;
        if (player.getEffectivePermissions() != null)
        {
            for (PermissionAttachmentInfo attachmentInfo : player.getEffectivePermissions())
            {
                if (attachmentInfo.getAttachment() != null && attachmentInfo.getAttachment().getPlugin() != null && attachmentInfo.getAttachment().getPlugin() instanceof BukkitCore)
                {
                    attachment = attachmentInfo.getAttachment();
                    break;
                }
            }
        }
        if (attachment == null)
        {
            attachment = player.addAttachment((Plugin)CubeEngine.getCore());
            attachment.setPermission(posPerm, true);
            attachment.setPermission(negPerm, true);
        }
        player.recalculatePermissions();
    }

    public boolean isInvulnerable()
    {
        return BukkitUtils.isInvulnerable(this);
    }

    public void setInvulnerable(boolean state)
    {
        BukkitUtils.setInvulnerable(this, state);
    }

    public long getWorldId()
    {
        return CubeEngine.getCore().getWorldManager().getWorldId(this.getWorld());
    }

    /**
     * Returns all entities in line of sight of this player
     *
     * @param distance the max distance
     * @return a set of all entities in line of sight OR null if not online
     */
    public TreeSet<Entity> getTargets(int distance)
    {
        if (this.offlinePlayer.isOnline())
        {
            final Location blockLoc = new Location(null, 0, 0, 0);
            final Location entityLoc = new Location(null, 0, 0, 0);
            final Location playerLoc = this.getLocation();
            Comparator<Entity> compare = new Comparator<Entity>()
            {
                final Location l1 = new Location(null, 0, 0, 0);
                final Location l2 = new Location(null, 0, 0, 0);

                @Override
                public int compare(Entity o1, Entity o2)
                {
                    o1.getLocation(l1);
                    o2.getLocation(l2);
                    return (int)(l1.distanceSquared(playerLoc) - l2.distanceSquared(playerLoc));
                }
            };
            BlockIterator iterator = new BlockIterator(
                    this.getPlayer().getWorld(),
                    this.getLocation().toVector(),
                    this.getEyeLocation().getDirection(),
                    0, distance);
            TreeSet<Entity> targets = new TreeSet<Entity>(compare);
            Collection<Entity> list = this.getNearbyEntities(distance, distance, distance);
            double detectDistance = 1;
            while (iterator.hasNext())
            {
                Block block = iterator.next();
                detectDistance += 0.015;
                block.getLocation(blockLoc).add(0.5, 0.5, 0.5);
                for (Entity entity : list)
                {
                    if (entity.getLocation(entityLoc).distanceSquared(blockLoc) < ((entity instanceof Spider) ? detectDistance + 0.5 : detectDistance))
                    {
                        targets.add(entity);
                    }
                }
            }
            return targets;
        }
        return null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        else if (o instanceof OfflinePlayer)
        {
            return this.offlinePlayer.equals(o);
        }
        else if (o instanceof CommandSender)
        {
            return ((CommandSender)o).getName().equals(this.getName());
        }
        else if (o instanceof org.bukkit.command.CommandSender)
        {
            return ((org.bukkit.command.CommandSender)o).getName().equals(this.getName());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return this.offlinePlayer.hashCode();
    }

    private InetSocketAddress address = null;
    public void refreshIP()
    {
        address = this.getPlayer().getAddress();
    }

    @Override
    public InetSocketAddress getAddress()
    {
        if (this.isOnline())
        {
            return super.getAddress();
        }
        return this.address;
    }
}
