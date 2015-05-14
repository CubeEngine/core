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
package de.cubeisland.engine.module.service.command.completer;

import java.util.List;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.completer.Completer;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.graph.meta.ModuleMetadata;

import static java.util.stream.Collectors.toList;

public class ModuleCompleter implements Completer
{
    private Modularity modularity;

    public ModuleCompleter(Modularity modularity)
    {
        this.modularity = modularity;
    }

    @Override
    public List<String> getSuggestions(CommandInvocation invocation)
    {
        String token = invocation.currentToken();
        return modularity.getModules().stream()
                          .map(Module::getInformation)
                          .map(ModuleMetadata::getName)
                          .filter(id -> id.startsWith(token))
                          .collect(toList());
    }
}
