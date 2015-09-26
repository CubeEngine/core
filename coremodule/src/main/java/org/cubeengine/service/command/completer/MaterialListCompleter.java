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
package org.cubeengine.service.command.completer;

import java.util.Arrays;
import java.util.List;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.completer.Completer;
import org.cubeengine.module.core.util.StringUtils;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.block.BlockType;

import static java.util.stream.Collectors.toList;
import static org.spongepowered.api.block.BlockTypes.AIR;

public class MaterialListCompleter implements Completer
{
    private final GameRegistry registry;

    // TODO register completer
    public MaterialListCompleter(Game game)
    {
        registry = game.getRegistry();
    }

    @Override
    public List<String> getSuggestions(CommandInvocation invocation)
    {
        String fullToken = invocation.currentToken();
        List<String> tokens = Arrays.asList(StringUtils.explode(",", fullToken));
        int splitAt = fullToken.lastIndexOf(",");
        String lastToken = fullToken.substring(splitAt +1,fullToken.length()).toUpperCase();
        String firstTokens = fullToken.substring(0, splitAt + 1);

        return registry.getAllOf(BlockType.class).stream()
                       .filter(m -> m != AIR)
                       .map(BlockType::getName)
                       .filter(n -> n.startsWith(lastToken))
                       .filter(n -> !tokens.contains(n))
                       .map(n -> firstTokens + n)
                       .collect(toList());
    }
}
