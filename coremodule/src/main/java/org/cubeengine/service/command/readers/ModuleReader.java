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
package org.cubeengine.service.command.readers;

import de.cubeisland.engine.modularity.core.LifeCycle;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.graph.meta.ModuleMetadata;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.completer.Completer;
import org.cubeengine.butler.parameter.reader.ArgumentReader;
import org.cubeengine.butler.parameter.reader.ReaderException;
import org.cubeengine.service.command.TranslatedReaderException;
import org.cubeengine.service.i18n.I18n;

import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;
import static org.cubeengine.service.i18n.formatter.MessageType.NEGATIVE;

public class ModuleReader implements ArgumentReader<Module>, Completer
{
    private Modularity mm;
    private I18n i18n;

    public ModuleReader(Modularity mm, I18n i18n)
    {
        this.mm = mm;
        this.i18n = i18n;
    }

    @Override
    public Module read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String name = invocation.consume(1);
        for (LifeCycle module : this.mm.getModules())
        {
            if (((ModuleMetadata) module.getInformation()).getName().equals(name))
            {
                return ((Module) module.getInstance());
            }
        }
        throw new TranslatedReaderException(i18n.translate(invocation.getContext(Locale.class), NEGATIVE,
                "The given module could not be found!"));
    }

    @Override
    public List<String> getSuggestions(CommandInvocation invocation)
    {
        String token = invocation.currentToken();
        return mm.getModules().stream()
                .map(lifeCycle -> (ModuleMetadata)lifeCycle.getInformation())
                .map(ModuleMetadata::getName)
                .filter(id -> id.startsWith(token))
                .collect(toList());
    }
}
