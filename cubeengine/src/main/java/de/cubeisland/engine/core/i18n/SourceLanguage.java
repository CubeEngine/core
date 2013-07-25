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
package de.cubeisland.engine.core.i18n;

import java.util.Locale;
import java.util.Map;

import de.cubeisland.engine.core.util.ChatFormat;

import gnu.trove.map.hash.THashMap;

/**
 * This class represents the source language.
 */
public final class SourceLanguage implements Language
{
    private final String code = "en_US";
    private final Locale locale = Locale.US;
    private final String name = "English";
    private final String localName = "English";
    private final Map<String, String> messages = new THashMap<String, String>();

    SourceLanguage()
    {}

    @Override
    public Locale getLocale()
    {
        return this.locale;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String getLocalName()
    {
        return this.localName;
    }

    @Override
    public String getTranslation(String message)
    {
        String translation = this.messages.get(message);
        if (translation == null)
        {
            this.messages.put(message, translation = ChatFormat.parseFormats(message));
        }

        return translation;
    }

    @Override
    public Map<String, String> getMessages()
    {
        return new THashMap<String, String>(this.messages);
    }

    @Override
    public boolean equals(Locale locale)
    {
        return this.locale.equals(locale);
    }

    @Override
    public int hashCode()
    {
        return this.code.hashCode();
    }

    @Override
    public void clean()
    {
        this.messages.clear();
    }
}
