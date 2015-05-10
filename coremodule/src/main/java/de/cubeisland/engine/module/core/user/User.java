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
package de.cubeisland.engine.module.core.user;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.google.common.base.Optional;
import de.cubeisland.engine.module.core.Core;
import de.cubeisland.engine.module.core.CubeEngine;
import de.cubeisland.engine.module.core.attachment.AttachmentHolder;
import de.cubeisland.engine.module.core.ban.IpBan;
import de.cubeisland.engine.module.core.ban.UserBan;
import de.cubeisland.engine.module.core.command.CommandSender;
import de.cubeisland.engine.module.core.module.trash.Module;
import de.cubeisland.engine.module.core.sponge.SpongeCore;
import de.cubeisland.engine.module.core.util.ChatFormat;
import de.cubeisland.engine.module.core.util.formatter.MessageType;
import org.jooq.types.UInteger;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.manipulators.entities.InvulnerabilityData;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.Location;

import static de.cubeisland.engine.module.core.util.BlockUtil.isNonObstructingSolidBlock;
import static org.spongepowered.api.service.permission.SubjectData.GLOBAL_CONTEXT;

/**
 * A CubeEngine User (can exist offline too).
 * <p>Do not instantiate outside of {@link UserManager} implementations
 */
public class User extends UserBase implements CommandSender, AttachmentHolder<UserAttachment>
{
    private final UserEntity entity;

    boolean loggedInState = false;
    private final Map<Class<? extends UserAttachment>, UserAttachment> attachments;
    private final Core core;

    /**
     * Do not instantiate outside of {@link UserManager} implementations
     *
     * @param core
     * @param player
     */
    public User(SpongeCore core, org.spongepowered.api.entity.player.User player)
    {
        super(core.getGame(), player.getUniqueId());
        this.entity = core.getDB().getDSL().newRecord(TableUser.TABLE_USER).newUser(player);
        this.attachments = new HashMap<>();
        this.core = core;
    }

    /**
     * Do not instantiate outside of {@link UserManager} implementations
     *
     * @param entity
     */
    public User(SpongeCore core, UserEntity entity)
    {
        super(core.getGame(), entity.getUniqueId());
        this.core = CubeEngine.getCore();
        this.entity = entity;
        this.attachments = new HashMap<>();
    }

    @Override
    public Core getCore()
    {
        return this.core;
    }

    @Override
    public synchronized <A extends UserAttachment> A attach(Class<A> type, Module module)
    {
        try
        {
            A attachment = type.newInstance();
            attachment.attachTo(module, this);
            @SuppressWarnings("unchecked")
            A oldAttachment = (A) this.attachments.put(type, attachment);
            if (oldAttachment != null)
            {
                oldAttachment.onDetach();
            }
            return attachment;
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("The given attachment could not be created!", e);
        }
    }

    @Override
    public synchronized <A extends UserAttachment> A attachOrGet(Class<A> type, Module module)
    {
        A attachment = this.get(type);
        if (attachment == null)
        {
            attachment = this.attach(type, module);
        }
        return attachment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <A extends UserAttachment> A get(Class<A> type)
    {
        return (A)this.attachments.get(type);
    }

    @Override
    public synchronized Set<UserAttachment> getAll()
    {
        return new HashSet<>(this.attachments.values());
    }

    @Override
    public synchronized <A extends UserAttachment> boolean has(Class<A> type)
    {
        return this.attachments.containsKey(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <A extends UserAttachment> A detach(Class<A> type)
    {
        A attachment = (A)this.attachments.remove(type);
        if (attachment != null)
        {
            attachment.onDetach();
        }
        return attachment;
    }

    @Override
    public synchronized void detachAll(Module module)
    {
        final Iterator<Entry<Class<? extends UserAttachment>, UserAttachment>> it = this.attachments.entrySet().iterator();
        UserAttachment attachment;
        while (it.hasNext())
        {
            attachment = it.next().getValue();
            if (attachment.getModule() == module)
            {
                attachment.onDetach();
                it.remove();
            }
        }
    }

    @Override
    public synchronized void detachAll()
    {
        final Iterator<Entry<Class<? extends UserAttachment>, UserAttachment>> it = this.attachments.entrySet().iterator();
        while (it.hasNext())
        {
            it.next().getValue().onDetach();
            it.remove();
        }
    }

    public Long getId()
    {
        return this.entity.getKey().longValue();
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
            CubeEngine.getLog().debug("A module sent an untranslated message!");
        }
        super.sendMessage(ChatFormat.parseFormats(string));
    }

    @Override
    public String getTranslation(MessageType type, String message, Object... params)
    {
        return this.getCore().getI18n().translate(this.getLocale(), type, message, params);
    }

    @Override
    public String getTranslationN(MessageType type, int n, String singular, String plural, Object... params)
    {
        return this.getCore().getI18n().translateN(this.getLocale(), type, n, singular, plural, params);
    }

    /**
     * Sends a translated Message to this User
     *
     * @param type
     * @param message the message to translate
     * @param params optional parameter
     */
    @Override
    public void sendTranslated(MessageType type, String message, Object... params)
    {
        this.sendMessage(this.getTranslation(type, message, params));
    }

    @Override
    public void sendTranslatedN(MessageType type, int n, String singular, String plural, Object... params)
    {
        this.sendMessage(this.getTranslationN(type, n, singular, plural, params));
    }

    public void sendMessage(MessageType type, String message, Object... params)
    {
        this.sendMessage(this.getCore().getI18n().composeMessage(this.getLocale(), type, message, params));
    }

    /**
     * Returns the users configured locale
     *
     * @return a locale string
     */
    @Override
    public Locale getLocale()
    {
        if (this.entity.getLocale() != null)
        {
            return this.entity.getLocale();
        }
        Locale locale = null;
        Optional<Player> player = getPlayer();
        if (player.isPresent())
        {
            locale = player.get().getLocale();
        }
        if (locale == null)
        {
            locale = this.getCore().getI18n().getDefaultLanguage().getLocale();
        }
        return locale;
    }

    public void setLocale(Locale locale)
    {
        if (locale == null)
        {
            throw new NullPointerException();
        }
        this.entity.setLocale(locale);
    }

    public int getPing()
    {
        Optional<Player> player = getPlayer();
        if (player.isPresent())
        {
            return player.get().getConnection().getPing();
        }
        return -1;
    }

    @Override
    public Date getLastPlayed()
    {
        if (this.isOnline())
        {
            return new Date();
        }
        return super.getLastPlayed();
        // TODO do we still need this? return this.entity.getValue(TABLE_USER.LASTSEEN).getTime();
    }

    public boolean safeTeleport(Location location, boolean keepDirection)
    {
        Optional<Location> safeLocation = ((SpongeCore)core).getGame().getTeleportHelper().getSafeLocation(location);
        if (safeLocation.isPresent())
        {
            location = safeLocation.get();
        }

        if (keepDirection)
        {
            this.teleport(location);
            // TODO rotation stays? this.setRotation(getRotation());
        }
        else
        {
            this.teleport(location);
        }
        return true;
    }

    public boolean isPasswordSet()
    {
        byte[] value = this.entity.getValue(TableUser.TABLE_USER.PASSWD);
        return value != null && value.length > 0;
    }

    public void logout()
    {
        this.loggedInState = false;
    }

    public boolean isLoggedIn()
    {
        return this.loggedInState;
    }

    public void setPermission(String permission, boolean b)
    {
        getPlayer().get().getData().setPermission(GLOBAL_CONTEXT, permission, Tristate.fromBoolean(b)); // TODO context
    }

    public void setPermission(Map<String, Boolean> permissions)
    {
        for (Entry<String, Boolean> entry : permissions.entrySet())
        {
            this.setPermission(entry.getKey(), entry.getValue());
        }
    }

    public boolean isInvulnerable()
    {
        return getData(InvulnerabilityData.class).isPresent();
    }


    public void setInvulnerable(boolean state)
    {
        InvulnerabilityData data = ((SpongeCore)core).getGame().getRegistry().getManipulatorRegistry().getBuilder(InvulnerabilityData.class).get().create();
        data.setInvulnerableTicks(100000000);
        offer(data);
    }

    public UInteger getWorldId()
    {
        try
        {
            return this.getCore().getWorldManager().getWorldId(this.getWorld());
        }
        catch (IllegalArgumentException ex)
        {
            return null;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (this == o)
        {
            return true;
        }
        else if (o instanceof org.spongepowered.api.entity.player.User)
        {
            return this.getOfflinePlayer().equals(o);
        }
        else if (o instanceof CommandSender)
        {
            return ((CommandSender)o).getUniqueId().equals(this.getUniqueId());
        }
        else if (o instanceof CommandSource)
        {
            return ((CommandSource)o).getName().equals(this.getName());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return this.getOfflinePlayer().hashCode();
    }

    private InetSocketAddress address = null;
    public void refreshIP()
    {
        address = this.getPlayer().get().getConnection().getAddress();
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

    public void banIp(CommandSource source, String reason)
    {
        this.banIp(source, reason, null);
    }

    public void banIp(CommandSource source, String reason, Date expire)
    {
        this.banIp(source, reason, new Date(System.currentTimeMillis()), expire);
    }

    public void banIp(CommandSource source, String reason, Date created, Date expire)
    {
        this.getCore().getBanManager().addBan(new IpBan(this.getAddress().getAddress(), source, Texts.of(reason), created, expire));
    }

    public void ban(CommandSource source, String reason)
    {
        this.ban(source, reason, null);
    }

    public void ban(CommandSource source, String reason, Date expire)
    {
        this.ban(source, reason, new Date(System.currentTimeMillis()), expire);
    }

    public void ban(CommandSource source, String reason, Date created, Date expire)
    {
        this.getCore().getBanManager().addBan(new UserBan(this.getOfflinePlayer(), source, Texts.of(reason), created, expire));
    }

    public UserEntity getEntity()
    {
        return entity;
    }

    public Iterator<Location> getLineOfSight(int maxDistance)
    {

        if (maxDistance > Bukkit.getServer().getViewDistance() * 16) {
            maxDistance = Bukkit.getServer().getViewDistance() * 16;
        }
        return new BlockIterator(this, maxDistance);
    }

    public Location getTargetBlock(int maxDistance)
    {
        Iterator<Location> lineOfSight = this.getLineOfSight(maxDistance);
        while (lineOfSight.hasNext())
        {
            Location next = lineOfSight.next();
            if (next.getType().isSolidCube() && !isNonObstructingSolidBlock(next.getType()))
            {
                return next;
            }
        }
        return null;
    }

    public Location getTargetBlock(int maxDistance, BlockType... transparent)
    {
        Iterator<Location> lineOfSight = this.getLineOfSight(maxDistance);
        List<BlockType> list = Arrays.asList(transparent);
        while (lineOfSight.hasNext())
        {
            Location next = lineOfSight.next();
            if (!list.contains(next.getType()))
            {
                return next;
            }
        }
        return null;
    }

    @Override
    public String getName()
    {
        String name = super.getName();
        if (name == null)
        {
            return this.entity.getValue(TableUser.TABLE_USER.LASTNAME);
        }
        return name;
    }
}
