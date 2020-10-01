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

import static org.cubeengine.dirigent.context.Contexts.LOCALE;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.cubeengine.dirigent.builder.BuilderDirigent;
import org.cubeengine.dirigent.context.Context;
import org.cubeengine.i18n.I18nService;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.locale.LocaleSource;

import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;

public abstract class I18nTranslate
{
    abstract BuilderDirigent<Component, TextComponent.Builder> getCompositor();
    abstract I18nService getI18nService();
    abstract Context contextFromLocale(Locale locale);
    abstract Context contextFromReceiver(Object receiver);

    public Component composeMessage(Locale locale, Style style, String message, Object... args)
    {
        return composeMessage(contextFromLocale(locale), style, message, args);
    }

    public Component composeMessage(CommandCause receiver, Style style, String message, Object... args)
    {
        return getCompositor().compose(contextFromReceiver(receiver), message, args).style(style);
    }

    public Component composeMessage(Audience receiver, Style style, String message, Object... args)
    {
        return getCompositor().compose(contextFromReceiver(receiver), message, args).style(style);
    }

    public Component composeMessage(Context context, Style style, String message, Object... args)
    {
        return getCompositor().compose(context, message, args).style(style);
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

    public String getTranslationN(Locale locale, int n, String singular, String plural)
    {
        return getI18nService().translateN(locale, singular, plural, n);
    }

    // Simple with CommandSource
    public String getTranslation(Audience audience, String message)
    {
        if (audience instanceof LocaleSource) {
            return getI18nService().translate(((LocaleSource) audience).getLocale(), message);
        }
        return getI18nService().translate(message);
    }

    // TextFormat and Locale
    public Component translate(Locale locale, Style style, String message, Object... args)
    {
        return translate(contextFromLocale(locale), style, message, args);
    }

    public Component translateN(Locale locale, Style style, int n, String singular, String plural, Object... args)
    {
        if (locale == null)
        {
            throw new NullPointerException("The language must not be null!");
        }
        if (singular == null || plural == null)
        {
            return null;
        }
        return composeMessage(locale, style, getTranslationN(locale, n, singular, plural), args);
    }

    public Component translate(Context context, Style style, String message, Object... args)
    {
        Objects.requireNonNull(context, "context");
        if (message == null)
        {
            return TextComponent.of("null");
        }
        return composeMessage(context, style, this.getTranslation(context.get(LOCALE), message), args);
    }

    public Component translateN(Context context, Style style, int n, String singular, String plural, Object... args)
    {
        Objects.requireNonNull(context, "context");
        if (singular == null || plural == null)
        {
            return null;
        }
        return composeMessage(context, style, getTranslationN(context.get(LOCALE), n, singular, plural), args);
    }

    // Translate to Component
    public Component translate(Audience mr, String message, Object... args)
    {
        return this.translate(mr, Style.empty(), message, args);
    }

    public Component translateN(Audience mr, int n, String singular, String plural, Object... args)
    {
        return this.translateN(mr, Style.empty(), n, singular, plural, args);
    }

    // Get from Object with TextFormat

    public Component translate(Player source, Style style, String message, Object... args)
    {
        return translate(contextFromReceiver(source), style, message, args);
    }

    public Component translateN(Player source, Style style, int n, String singular, String plural, Object... args)
    {
        return translateN(contextFromReceiver(source), style, n, singular, plural, args);
    }

    public Component translate(Audience source, Style style, String message, Object... args)
    {
        return translate(contextFromReceiver(source), style, message, args);
    }

    public Component translateN(Audience source, Style style, int n, String singular, String plural, Object... args)
    {
        return translateN(contextFromReceiver(source), style, n, singular, plural, args);
    }

    // Send to MessageReceiver with TextFormat

    public void send(Audience source, Style style, String message, Object... args)
    {
        source.sendMessage(translate(source, style, message, args));
    }

    public void sendN(Audience source, Style style, int n, String singular, String plural, Object... args)
    {
        source.sendMessage(translateN(source, style, n, singular, plural, args));
    }

    // Send to ChatTypeMessageReceiver with TextFormat

    public void send(ChatType type, Audience source, Style style, String message, Object... args)
    {
        type.sendTo(source, this.translate(source, style, message, args));
    }

    public void sendN(ChatType type, Audience source, Style style, int n, String singular, String plural, Object... args)
    {
        type.sendTo(source, this.translateN(source, style, n, singular, plural, args));
    }

    public enum ChatType {
        CHAT(Audience::sendMessage),
        SYSTEM((a, c) -> a.sendMessage(c, MessageType.SYSTEM)),
        ACTION_BAR(Audience::sendActionBar);

        private BiConsumer<Audience, Component> biConsumer;

        ChatType(BiConsumer<Audience, Component> biConsumer) {
            this.biConsumer = biConsumer;
        }

        public void sendTo(Audience audience, Component component) {
            this.biConsumer.accept(audience, component);
        }
    }
}
