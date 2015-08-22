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
package de.cubeisland.engine.service.user;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import com.google.common.base.Optional;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.core.attachment.AttachmentHolder;
import de.cubeisland.engine.service.command.sender.BaseCommandSender;
import de.cubeisland.engine.service.i18n.I18n;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextFormat;

import static org.spongepowered.api.data.key.Keys.DISPLAY_NAME;
import static org.spongepowered.api.data.manipulator.catalog.CatalogEntityData.INVISIBILITY_DATA;

/**
 * A CubeEngine User (can exist offline too).
 * <p>Do not instantiate outside of {@link UserManager} implementations
 */
public class User extends BaseCommandSender implements AttachmentHolder<UserAttachment>
{
    private UserManager um;
    private final UUID uuid;
    private org.spongepowered.api.entity.player.User player;
    private Map<Class<? extends UserAttachment>, UserAttachment> attachments = new ConcurrentHashMap<>();

    private CompletableFuture<UserEntity> future;

    public User(I18n i18n, UserManager um, UUID uuid)
    {
        super(i18n);
        this.um = um;
        this.uuid = uuid;
        this.player = um.getPlayer(uuid);
        this.future = um.loadEntity(uuid);
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

    public void sendMessage(TextFormat format, String message, Object... params)
    {
        this.sendMessage(i18n.composeMessage(this.getLocale(), format, message, params));
    }

    /**
     * Returns the users configured locale
     *
     * @return a locale string
     */
    @Override
    public Locale getLocale()
    {
        Locale locale = getEntity().getLocale();
        if (locale == null)
        {
            return getPlayer().transform(Player::getLocale).or(i18n.getDefaultLanguage().getLocale());
        }
        return locale;
    }

    public void setLocale(Locale locale)
    {
        if (locale == null)
        {
            throw new NullPointerException();
        }
        getEntity().setLocale(locale);
    }

    public UserEntity getEntity()
    {
        try
        {
            return future.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public CompletableFuture<UserEntity> entity()
    {
        return future;
    }

    private InetSocketAddress address = null;

    public void refreshIP()
    {
        address = this.getPlayer().get().getConnection().getAddress();
    }

    public InetSocketAddress getAddress()
    {
        return address;
    }

    @Override
    public String getName()
    {
        return getUser().getName();
    }

    public Optional<Player> getPlayer()
    {
        if (player == null)
        {
            player = um.getPlayer(uuid);
        }
        if (player instanceof Player)
        {
            return Optional.of(((Player)player));
        }
        return Optional.absent();
    }

    public org.spongepowered.api.entity.player.User getUser()
    {
        return getPlayer().transform(p -> (org.spongepowered.api.entity.player.User)p).or(player);
    }

    @Override
    public Text getDisplayName()
    {
        if (getPlayer().isPresent())
        {
            return asPlayer().get(DISPLAY_NAME).or(Texts.of(player.getName()));
        }
        return Texts.of(player.getName());
    }

    @Override
    public boolean hasPermission(String perm)
    {
        return getUser().hasPermission(perm);
    }

    @Override
    public UUID getUniqueId()
    {
        return uuid;
    }

    public boolean canSee(Player player)
    {
        return getPlayer().isPresent() && player.get(INVISIBILITY_DATA).transform(
            p -> p.invisibleToPlayerIds().contains(getUniqueId())).or(false);
    }

    public Player asPlayer()
    {
        return player.getPlayer().get();
    }

    @Override
    public void sendMessage(String msg)
    {
        if (msg != null)
        {
            sendMessage(Texts.of(msg));
        }
    }

    @Override
    public void sendMessage(Text msg)
    {
        if (getPlayer().isPresent())
        {
            asPlayer().sendMessage(msg);
        }
    }
}
