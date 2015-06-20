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
package de.cubeisland.engine.service.i18n;

import java.util.Locale;
import org.spongepowered.api.text.format.BaseFormatting;
import org.spongepowered.api.text.translation.Translation;

// TODO remove passing args. Waiting for https://github.com/SpongePowered/SpongeAPI/issues/639
public class CubeEngineTranslation implements Translation
{
    private I18n i18n;
    private final BaseFormatting format;
    private final Locale locale;
    private final int n;
    private final String toTranslate;
    private final String plural;
    private Object[] args;

    public CubeEngineTranslation(I18n i18n, BaseFormatting format, Locale locale, String toTranslate, Object... args)
    {
        this(i18n, format, locale, 0, toTranslate, null, args);
    }

    public CubeEngineTranslation(I18n i18n, BaseFormatting format, Locale locale, int n, String singular, String plural, Object... args)
    {
        this.i18n = i18n;
        this.format = format;
        this.locale = locale;
        this.n = n;
        this.toTranslate = singular;
        this.plural = plural;
        this.args = args;

        for (int i = 0; i < args.length; i++)
        {
            final Object arg = args[i];
            if (arg instanceof Translation)
            {
                args[i] = ((Translation)arg).get(locale);
            }
        }
    }

    @Override
    public String getId()
    {
        return toTranslate;
    }

    @Override
    public String get(Locale locale)
    {
        if (plural != null)
        {
            return i18n.translateN(locale, format, n, toTranslate, plural, args);
        }
        return i18n.translate(locale, format, toTranslate, args);
    }

    @Override
    public String get(Locale locale, Object... args)
    {

        if (plural != null)
        {
            return i18n.translateN(locale, format, n, toTranslate, plural, args);
        }
        return i18n.translate(locale, format, toTranslate, args);
    }
}
