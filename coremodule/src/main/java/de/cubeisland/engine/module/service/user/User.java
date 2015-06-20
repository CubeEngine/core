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
package de.cubeisland.engine.module.service.user;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import com.google.common.base.Optional;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.core.attachment.AttachmentHolder;
import de.cubeisland.engine.module.core.i18n.I18n;
import de.cubeisland.engine.module.core.util.ChatFormat;
import de.cubeisland.engine.module.service.command.CommandSender;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.service.profile.GameProfileResolver;
import org.spongepowered.api.service.user.UserStorage;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Translatable;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.BaseFormatting;

import static de.cubeisland.engine.module.core.util.ChatFormat.BASE_CHAR;

/**
 * A CubeEngine User (can exist offline too).
 * <p>Do not instantiate outside of {@link UserManager} implementations
 */
public class User implements CommandSender, AttachmentHolder<UserAttachment>
{
    private final Game game;
    private final UUID uuid;
    private final UserEntity entity;
    private final Modularity modularity;
    private org.spongepowered.api.entity.player.User player;

    private final Map<Class<? extends UserAttachment>, UserAttachment> attachments;

    public User(Modularity modularity, UserEntity entity, org.spongepowered.api.entity.player.User player)
    {
        this.game = modularity.start(Game.class);
        this.modularity = modularity;
        this.uuid = entity.getUniqueId();
        this.entity = entity;
        this.attachments = new HashMap<>();
        this.player = player;
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
        return this.entity.getId().longValue();
    }

    @Override
    public void sendMessage(String string)
    {
        if (string != null)
        {
            this.sendMessage(ChatFormat.fromLegacy(string, BASE_CHAR));
        }
    }

    @Override
    public void sendMessage(Text msg)
    {
        if (getPlayer().isPresent())
        {
            getPlayer().get().sendMessage(msg);
        }
    }

    @Override
    public Translatable getTranslation(BaseFormatting format, String message, Object... args)
    {
        return getI18n().getTranslation(format, getLocale(), message, args);
    }

    private I18n getI18n()
    {
        return modularity.start(I18n.class);
    }

    @Override
    public Translatable getTranslationN(BaseFormatting format, int n, String singular, String plural, Object... args)
    {
        return getI18n().getTranslationN(format, getLocale(), n, singular, plural, args);
    }

    /**
     * Sends a translated Message to this User
     * @param format
     * @param message the message to translate
     * @param args optional parameter
     */
    @Override
    public void sendTranslated(BaseFormatting format, String message, Object... args)
    {
        this.sendMessage(this.getTranslation(format, message, args).getTranslation().get(getLocale()));
    }

    @Override
    public void sendTranslatedN(BaseFormatting format, int n, String singular, String plural, Object... args)
    {
        this.sendMessage(this.getTranslationN(format, n, singular, plural, args).getTranslation().get(getLocale()));
    }

    public void sendMessage(BaseFormatting format, String message, Object... params)
    {
        this.sendMessage(getI18n().composeMessage(this.getLocale(), format, message, params));
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

            locale = getI18n().getDefaultLanguage().getLocale();
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

    private InetSocketAddress address = null;
    public void refreshIP()
    {
        address = this.getPlayer().get().getConnection().getAddress();
    }

    public InetSocketAddress getAddress()
    {
        return address;
    }

    public UserEntity getEntity()
    {
        return entity;
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
            player = game.getServer().getPlayer(uuid).orNull();
            if (player == null)
            {
                getUser();
            }
        }
        if (player instanceof Player)
        {
            return Optional.of(((Player)player));
        }
        return Optional.absent();
    }

    public Player asPlayer()
    {
        return getPlayer().orNull();
    }

    public org.spongepowered.api.entity.player.User getUser()
    {
        UserStorage storage = game.getServiceManager().provide(UserStorage.class).get();
        player = storage.get(uuid).orNull();
        if (player == null)
        {
            GameProfileResolver resolver = game.getServiceManager().provide(GameProfileResolver.class).get();
            try
            {
                GameProfile profile = resolver.get(uuid).get();
                player = storage.getOrCreate(profile);
            }
            catch (InterruptedException | ExecutionException e)
            {
                throw new IllegalStateException(e);
            }
        }
        return player;
    }

    @Override
    public Text getDisplayName()
    {
        if (getPlayer().isPresent())
        {
            return asPlayer().getDisplayNameData().getDisplayName();
        }
        return Texts.of();
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
        return true; // TODO
    }
}
