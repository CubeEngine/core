package org.cubeengine.service.command.readers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.completer.Completer;
import org.cubeengine.butler.parameter.TooFewArgumentsException;
import org.cubeengine.butler.parameter.reader.ArgumentReader;
import org.cubeengine.butler.parameter.reader.DefaultValue;
import org.cubeengine.butler.parameter.reader.ReaderException;
import org.cubeengine.service.command.TranslatedReaderException;
import org.cubeengine.service.i18n.I18n;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.storage.WorldProperties;

import static org.cubeengine.module.core.util.StringUtils.startsWithIgnoreCase;
import static org.cubeengine.service.i18n.formatter.MessageType.NEGATIVE;

public class WorldPropertiesReader implements ArgumentReader<WorldProperties>, DefaultValue<WorldProperties>, Completer
{
    private I18n i18n;

    public WorldPropertiesReader(I18n i18n)
    {
        this.i18n = i18n;
    }

    @Override
    public WorldProperties read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String name = invocation.consume(1);
        Optional<WorldProperties> world = Sponge.getServer().getWorldProperties(name);
        if (!world.isPresent())
        {
            throw new TranslatedReaderException(i18n.translate(invocation.getContext(Locale.class), NEGATIVE, "World {input} not found!", name));
        }
        return world.get();
    }

    @Override
    public WorldProperties getDefault(CommandInvocation invocation)
    {
        if (invocation.getCommandSource() instanceof Player)
        {
            return ((Player)invocation.getCommandSource()).getWorld().getProperties();
        }
        throw new TooFewArgumentsException();
    }

    @Override
    public List<String> getSuggestions(CommandInvocation invocation)
    {
        List<String> offers = new ArrayList<>();
        for (WorldProperties world : Sponge.getServer().getAllWorldProperties())
        {
            final String name = world.getWorldName();
            if (startsWithIgnoreCase(name, invocation.currentToken()))
            {
                offers.add(name);
            }
        }
        return offers;
    }
}
