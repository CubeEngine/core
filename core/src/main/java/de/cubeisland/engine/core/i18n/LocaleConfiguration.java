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

import de.cubeisland.engine.i18n.language.LanguageDefinition;
import de.cubeisland.engine.i18n.plural.PluralExpr;
import de.cubeisland.engine.reflect.ReflectedYaml;

/**
 * This configuration is used to parse the language configurations.
 */
@SuppressWarnings("all")
public class LocaleConfiguration extends ReflectedYaml implements LanguageDefinition
{
    public Locale locale;
    public String name;
    public String localName;
    public Locale parent = null;
    public Locale[] clones = null;
    public int pluralCount;
    public PluralExpr pluralExpression;

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
    public Locale getParent()
    {
        return this.parent;
    }

    @Override
    public Locale[] getClones()
    {
        return this.clones;
    }

    @Override
    public int getPluralCount()
    {
        return this.pluralCount;
    }

    @Override
    public PluralExpr getPluralExpression()
    {
        return this.pluralExpression;
    }
}
