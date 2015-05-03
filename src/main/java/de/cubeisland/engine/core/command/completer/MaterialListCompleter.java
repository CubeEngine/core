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
package de.cubeisland.engine.core.command.completer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.completer.Completer;
import de.cubeisland.engine.core.util.StringUtils;
import org.bukkit.Material;
import org.spongepowered.api.Game;
import org.spongepowered.api.item.ItemType;

public class MaterialListCompleter implements Completer
{
    private final Game game;

    public MaterialListCompleter(Game game)
    {
        this.game = game;
    }

    @Override
    public List<String> getSuggestions(CommandInvocation invocation)
    {
        String token = invocation.currentToken();
        List<String> tokens = Arrays.asList(StringUtils.explode(",", token));
        String lastToken = token.substring(token.lastIndexOf(",")+1,token.length()).toUpperCase();
        List<String> matches = new ArrayList<>();
        for (ItemType material : this.allTypes())
        {
            if (material.isBlock() && material != Material.AIR && material.getName().startsWith(lastToken) && !tokens.contains(material.getName()))
            {
                matches.add(token.substring(0, token.lastIndexOf(",") + 1) + material.getName());
            }
        }
        return matches;
    }

    private Collection<ItemType> allTypes()
    {
        Set<ItemType> allTypes = new HashSet<>();
        for (Set<ItemType> types : game.getRegistry().getGameDictionary().getAllItems().values())
        {
            allTypes.addAll(types);
        }
        return allTypes;
    }
}
