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
package de.cubeisland.engine.core.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileFormater extends Formatter
{
    private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(LogRecord record)
    {
        return format.format(new Date(record.getMillis())) + " [" + record.getLevel().getName() + "] " +
            parse(record.getMessage(), record.getParameters()) + "\n";
    }

    private static final Pattern pattern = Pattern.compile("\\{(\\d*)}");

    public static String parse(String message, Object... args)
    {
        if (args == null || args.length == 0)
        {
            return message;
        }
        Matcher matcher = pattern.matcher(message);
        int i = 0;
        while (matcher.find())
        {
            String group = matcher.group(1);
            if (group.isEmpty())
            {
                message = matcher.replaceFirst(String.valueOf(args[i++]));
                matcher.reset(message);
            }
            else
            {
                Integer atPos = Integer.valueOf(group);
                message = matcher.replaceFirst(String.valueOf(args[atPos]));
                matcher.reset(message);
            }
        }
        return message;
    }
}
