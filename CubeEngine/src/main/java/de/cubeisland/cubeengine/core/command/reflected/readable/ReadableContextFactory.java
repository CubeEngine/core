package de.cubeisland.cubeengine.core.command.reflected.readable;

import de.cubeisland.cubeengine.core.command.BasicContextFactory;
import de.cubeisland.cubeengine.core.command.BasicContext;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.util.StringUtils;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadableContextFactory extends BasicContextFactory
{
    private static final Set<String>         NO_FLAGS = new THashSet<String>(0);
    private static final Map<String, Object> NO_NAMED = new THashMap<String, Object>(0);
    private final Pattern pattern;

    public ReadableContextFactory(Pattern pattern)
    {
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
