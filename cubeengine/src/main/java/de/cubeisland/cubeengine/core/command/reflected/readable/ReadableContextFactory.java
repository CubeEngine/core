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
package de.cubeisland.cubeengine.core.command.reflected.readable;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.cubeisland.cubeengine.core.command.ArgBounds;
import de.cubeisland.cubeengine.core.command.BasicContext;
import de.cubeisland.cubeengine.core.command.BasicContextFactory;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.util.StringUtils;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import static de.cubeisland.cubeengine.core.command.ArgBounds.NO_MAX;

public class ReadableContextFactory extends BasicContextFactory
{
    private static final Set<String> NO_FLAGS = new THashSet<String>(0);
    private static final Map<String, Object> NO_NAMED = new THashMap<String, Object>(0);
    private final Pattern pattern;

    public ReadableContextFactory(Pattern pattern)
    {
        super(new ArgBounds(0, NO_MAX));
        this.pattern = pattern;
    }

    @Override
    public BasicContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] commandLine)
    {
        Matcher matcher = this.pattern.matcher(StringUtils.implode(" ", commandLine));
        final int groupCount = matcher.groupCount();
        LinkedList<String> indexed = new LinkedList<String>();
        for (int i = 1; i <= groupCount; ++i)
        {
            indexed.add(matcher.group(i));
        }

        return new BasicContext(command, sender, labels, indexed);
    }
}
