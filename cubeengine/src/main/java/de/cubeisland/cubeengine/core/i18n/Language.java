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
package de.cubeisland.cubeengine.core.i18n;

import java.util.Locale;
import java.util.Map;

import de.cubeisland.cubeengine.core.util.Cleanable;

/**
 * This interface represents a language containing translations.
 */
public interface Language extends Cleanable
{

    /**
     * Returns the language's locale
     *
     * @return a locale
     */
    public Locale getLocale();

    /**
     * Return the language's name
     *
     * @return the name
     */
    public String getName();

    /**
     * Returns the language's local name
     *
     * @return the local name
     */
    public String getLocalName();

    /**
     * Gets a translation from this language
     *
     * @param message the message
     * @return the translation
     */
    public String getTranslation(String message);

    /**
     * Returns a map of all translations of the given category
     *
     * @return all translations of the category
     */
    public Map<String, String> getMessages();

    public boolean equals(Locale locale);
}
