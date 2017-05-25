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
package org.cubeengine.libcube.service.command.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.completer.Completer;
import org.cubeengine.butler.parameter.argument.ArgumentParser;
import org.cubeengine.butler.parameter.argument.DefaultValue;
import org.cubeengine.butler.parameter.argument.ParserException;
import org.cubeengine.libcube.service.i18n.I18n;


public class BooleanParser implements ArgumentParser<Boolean>, Completer, DefaultValue<Boolean>
{
    private final Set<String> yesStrings;
    private final Set<String> noStrings;
    private final I18n i18n;

    public BooleanParser(I18n i18n)
    {
        this.i18n = i18n;
        this.yesStrings = new HashSet<>();
        this.yesStrings.add("yes");
        this.yesStrings.add("+");
        this.yesStrings.add("1");
        this.yesStrings.add("true");

        this.noStrings = new HashSet<>();
        this.noStrings.add("no");
        this.noStrings.add("-");
        this.noStrings.add("0");
        this.noStrings.add("false");
    }

    @Override
    public Boolean parse(Class type, CommandInvocation invocation) throws ParserException
    {
        String arg = invocation.consume(1);
        Locale locale = invocation.getContext(Locale.class);
        arg = arg.trim().toLowerCase(locale);
        if (this.yesStrings.contains(arg))
        {
            return true;
        }
        else if (this.noStrings.contains(arg))
        {
            return false;
        }
        else
        {
            String word = i18n.translate(locale, "yes");
            if (arg.equalsIgnoreCase(word))
            {
                return true;
            }
            word = i18n.translate(locale, "no");
            if (arg.equalsIgnoreCase(word))
            {
                return false;
            }
        }
        return Boolean.parseBoolean(arg);
    }

    @Override
    public List<String> suggest(Class type, CommandInvocation invocation)
    {
        List<String> list = new ArrayList<>();
        String token = invocation.currentToken();
        if ("true".startsWith(token))
        {
            list.add("true");
        }
        if ("false".startsWith(token))
        {
            list.add("false");
        }
        return list;
    }

    @Override
    public Boolean getDefault(CommandInvocation invocation)
    {
        return false;
    }
}
