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
package de.cubeisland.engine.core.command.readers;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import de.cubeisland.engine.core.CubeEngine;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;

public class DifficultyReader implements ArgumentReader<Difficulty>
{
    private GameRegistry registry;
    private Map<Integer, Difficulty> difficultyMap = new HashMap<Integer, Difficulty>()
    {
        {
            put(0, Difficulties.PEACEFUL);
            put(1, Difficulties.EASY);
            put(2, Difficulties.NORMAL);
            put(3, Difficulties.HARD);
        }
    };

    public DifficultyReader(Game game)
    {
        registry = game.getRegistry();
    }

    @Override
    public Difficulty read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String token = invocation.consume(1);
        Locale locale = invocation.getLocale();
        try
        {
            Difficulty difficulty = difficultyMap.get(Integer.valueOf(token));
            if (difficulty == null)
            {
                throw new ReaderException(CubeEngine.getCore().getI18n().translate(locale, NEGATIVE, "The given difficulty level is unknown!"));
            }
            return difficulty;
        }
        catch (NumberFormatException e)
        {
            for (Difficulty difficulty : registry.getAllOf(Difficulty.class))
            {
                if (difficulty.getName().equalsIgnoreCase(token))
                {
                    return difficulty;
                }
            }
            throw new ReaderException(CubeEngine.getCore().getI18n().translate(locale, NEGATIVE, "{input} is not a known difficulty!", token));
        }
    }
}
