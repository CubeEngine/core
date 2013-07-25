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
package de.cubeisland.engine.core.util.convert.converter;

import java.util.Locale;

import de.cubeisland.engine.core.config.node.Node;
import de.cubeisland.engine.core.config.node.StringNode;
import de.cubeisland.engine.core.i18n.I18n;
import de.cubeisland.engine.core.util.convert.ConversionException;
import de.cubeisland.engine.core.util.convert.Converter;

public class LocaleConverter implements Converter<Locale>
{
    @Override
    public Node toNode(Locale locale) throws ConversionException
    {
        return new StringNode(I18n.localeToString(locale));
    }

    @Override
    public Locale fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return I18n.stringToLocale(((StringNode)node).getValue());
        }
        throw new ConversionException("Locales can only be loaded from a string node!");
    }
}
