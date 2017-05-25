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
package org.cubeengine.libcube.service.command.readers;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.parameter.argument.ReaderException;
import org.cubeengine.libcube.service.command.TranslatedReaderException;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.i18n.formatter.MessageType;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;

public class DifficultyParser extends DefaultedCatalogTypeParser<Difficulty>
{
    private Map<Integer, Difficulty> difficultyMap = new HashMap<Integer, Difficulty>()
    {
        {
            put(0, Difficulties.PEACEFUL);
            put(1, Difficulties.EASY);
            put(2, Difficulties.NORMAL);
            put(3, Difficulties.HARD);
        }
    };
    private final I18n i18n;

    public DifficultyParser(I18n i18n)
    {
        super(Difficulty.class, Difficulties.NORMAL);
        this.i18n = i18n;
    }

    @Override
    public Difficulty parse(Class type, CommandInvocation invocation) throws ReaderException
    {
        String token = invocation.currentToken();
        Locale locale = invocation.getContext(Locale.class);
        try
        {
            Difficulty difficulty = difficultyMap.get(Integer.valueOf(token));
            if (difficulty == null)
            {
                throw new TranslatedReaderException(i18n.getTranslation(locale, MessageType.NEGATIVE, "The given difficulty level is unknown!"));
            }
            invocation.consume(1);
            return difficulty;
        }
        catch (NumberFormatException e)
        {
            return super.parse(type, invocation);
        }
    }
}
