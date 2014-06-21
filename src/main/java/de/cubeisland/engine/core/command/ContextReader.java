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
package de.cubeisland.engine.core.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import de.cubeisland.engine.core.command.exception.IncorrectArgumentException;
import de.cubeisland.engine.core.command.exception.ReaderException;
import de.cubeisland.engine.core.command.parameterized.CommandParameter;
import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;

public class ContextReader extends ContextParser
{
    public void readContext(ReadContext ctx, Locale locale)
    {
        if (!ctx.rawIndexed.isEmpty())
        {
            if (ctx.rawIndexed.get(ctx.rawIndexed.size() - 1).isEmpty())
            {
                // remove last if empty (only needed for tabcompletion)
                ctx.rawIndexed.remove(ctx.rawIndexed.size() - 1);
                ctx.indexedCount--;
            }
        }
        ctx.indexed = this.readIndexed(locale, ctx.rawIndexed);
        ctx.named = this.readNamed(locale, ctx.rawNamed);
    }

    protected List<Object> readIndexed(Locale locale, List<String> rawIndexed)
    {
        List<Object> result = new ArrayList<>();
        int i = 0;
        for (String arg : rawIndexed)
        {
            CommandParameterIndexed indexed = this.indexedMap.get(i++);
            if (indexed == null)
            {
                result.add(arg); // handle OOB somewhere else
            }
            else
            {
                boolean foundStatic = false;
                for (String label : indexed.getLabels())
                {
                    if (label.startsWith("!") && label.substring(1).equalsIgnoreCase(arg))
                    {
                        result.add(arg);
                        foundStatic = true;
                    }
                }
                if (foundStatic)
                {
                    continue;
                }

                ReaderException e = null;
                for (Class<?> type : indexed.getType())
                {
                    try
                    {
                        result.add(ArgumentReader.read(type, arg, locale));
                        e = null;
                        break;
                    }
                    catch (ReaderException ex)
                    {
                        e = ex;
                    }
                }
                if (e != null)
                {
                    throw new IncorrectArgumentException(i, e);
                }
            }
        }
        return result;
    }

    protected Map<String, Object> readNamed(Locale locale, Map<String, String> rawNamed)
    {
        Map<String, Object> readParams = new LinkedHashMap<>();

        for (Entry<String, String> entry : rawNamed.entrySet())
        {
            CommandParameter param = this.namedMap.get(entry.getKey());
            try
            {
                readParams.put(entry.getKey(), ArgumentReader.read(param.getType(), entry.getValue(), locale));
            }
            catch (ReaderException ex)
            {
                throw new IncorrectArgumentException(param.getName(), ex);
            }
        }
        return readParams;
    }
}
