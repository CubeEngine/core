package de.cubeisland.cubeengine.core.command.parameterized.completer;

import de.cubeisland.cubeengine.core.command.parameterized.Completer;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static de.cubeisland.cubeengine.core.util.StringUtils.startsWithIgnoreCase;

public abstract class SimpleCompleter implements Completer
{
    private final String[] strings;

    protected SimpleCompleter(String... strings)
    {
        this.strings = strings;
    }

    @Override
    public List<String> complete(CommandSender sender, String token)
    {
        token = token.toLowerCase(Locale.ENGLISH); // TODO replace with sender locale
        List<String> offers = new ArrayList<String>();
        for (String string : this.strings)
        {
            if (startsWithIgnoreCase(string, token))
            {
                offers.add(string);
            }
        }

        Collections.sort(offers, String.CASE_INSENSITIVE_ORDER);
        return offers;
    }
}
