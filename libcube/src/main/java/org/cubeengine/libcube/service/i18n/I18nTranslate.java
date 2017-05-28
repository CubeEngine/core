/*
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
package org.cubeengine.libcube.service.i18n;

import de.cubeisland.engine.i18n.I18nService;
import org.cubeengine.dirigent.builder.BuilderDirigent;
import org.cubeengine.dirigent.context.Context;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.ChatTypeMessageReceiver;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.format.TextFormat;

import java.util.Locale;
import java.util.Objects;

import static org.cubeengine.dirigent.context.Contexts.LOCALE;

public abstract class I18nTranslate
{
    abstract BuilderDirigent<Text, Text.Builder> getCompositor();
    abstract I18nService getI18nService();
    abstract Context contextFromLocale(Locale locale);
    abstract Context contextFromReceiver(Object receiver);

    public Text composeMessage(Locale locale, TextFormat format, String message, Object... args)
    {
        return composeMessage(contextFromLocale(locale), format, message, args);
    }

    public Text composeMessage(MessageReceiver receiver, TextFormat format, String message, Object... args)
    {
        return getCompositor().compose(contextFromReceiver(receiver), message, args).toBuilder().format(format).build();
    }

    public Text composeMessage(Context context, TextFormat format, String message, Object... args)
    {
        return getCompositor().compose(context, message, args).toBuilder().format(format).build();
    }

    public String getTranslation(String message)
    {
        return getI18nService().translate(message);
    }

    public String getTranslationN(int n, String singular, String plural)
    {
        return getI18nService().translateN(singular, plural, n);
    }

    // Simple with Locale
    public String getTranslation(Locale locale, String message)
    {
        return getI18nService().translate(locale, message);
    }

    // Simple with CommandSource
    public String getTranslation(CommandSource commandSource, String message)
    {
        return getI18nService().translate(commandSource.getLocale(), message);
    }

    public String getTranslationN(Locale locale, int n, String singular, String plural)
    {
        return getI18nService().translateN(locale, singular, plural, n);
    }

    // TextFormat and Locale
    public Text translate(Context context, TextFormat format, String message, Object... args)
    {
        Objects.requireNonNull(context, "context");
        if (message == null)
        {
            return Text.of("null");
        }
        return composeMessage(context, format, this.getTranslation(context.get(LOCALE), message), args);
    }

    public Text translateN(Context context, TextFormat format, int n, String singular, String plural, Object... args)
    {
        Objects.requireNonNull(context, "context");
        if (singular == null || plural == null)
        {
            return null;
        }
        return composeMessage(context, format, getTranslationN(context.get(LOCALE), n, singular, plural), args);
    }

    public Text translate(Locale locale, TextFormat format, String message, Object... args)
    {
        return translate(contextFromLocale(locale), format, message, args);
    }

    public Text translateN(Locale locale, TextFormat format, int n, String singular, String plural, Object... args)
    {
        if (locale == null)
        {
            throw new NullPointerException("The language must not be null!");
        }
        if (singular == null || plural == null)
        {
            return null;
        }
        return composeMessage(locale, format, getTranslationN(locale, n, singular, plural), args);
    }

    // Get from Object with TextFormat

    public Text translate(Player source, TextFormat format, String message, Object... args)
    {
        return translate(contextFromReceiver(source), format, message, args);
    }

    public Text translateN(Player source, TextFormat format, int n, String singular, String plural, Object... args)
    {
        return translateN(contextFromReceiver(source), format, n, singular, plural, args);
    }

    public Text translate(MessageReceiver source, TextFormat format, String message, Object... args)
    {
        return translate(contextFromReceiver(source), format, message, args);
    }

    public Text translateN(MessageReceiver source, TextFormat format, int n, String singular, String plural, Object... args)
    {
        return translateN(contextFromReceiver(source), format, n, singular, plural, args);
    }

    public Text translate(ChatTypeMessageReceiver source, TextFormat format, String message, Object... args)
    {
        return translate(contextFromReceiver(source), format, message, args);
    }

    public Text translateN(ChatTypeMessageReceiver source, TextFormat format, int n, String singular, String plural, Object... args)
    {
        return translateN(contextFromReceiver(source), format, n, singular, plural, args);
    }

    // Send to MessageReceiver with TextFormat

    public void send(MessageReceiver source, TextFormat format, String message, Object... args)
    {
        source.sendMessage(translate(source, format, message, args));
    }

    public void sendN(MessageReceiver source, TextFormat format, int n, String singular, String plural, Object... args)
    {
        source.sendMessage(translateN(source, format, n, singular, plural, args));
    }

    // Send to ChatTypeMessageReceiver with TextFormat

    public void send(ChatType type, ChatTypeMessageReceiver source, TextFormat format, String message, Object... args)
    {
        source.sendMessage(type, this.translate(source, format, message, args));
    }

    public void sendN(ChatType type, ChatTypeMessageReceiver source, TextFormat format, int n, String singular, String plural, Object... args)
    {
        source.sendMessage(type, this.translateN(source, format, n, singular, plural, args));
    }
}
