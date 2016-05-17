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
package org.cubeengine.libcube.service.command.readers;

import java.util.Locale;
import de.cubeisland.engine.logscribe.LogLevel;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.parameter.reader.ArgumentReader;
import org.cubeengine.butler.parameter.reader.ReaderException;
import org.cubeengine.libcube.service.command.TranslatedReaderException;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.i18n.formatter.MessageType;

public class LogLevelReader implements ArgumentReader<LogLevel>
{
    private I18n i18n;

    public LogLevelReader(I18n i18n)
    {

        this.i18n = i18n;
    }

    @Override
    public LogLevel read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String arg = invocation.consume(1);
        LogLevel logLevel = LogLevel.toLevel(arg);
        if (logLevel == null)
        {
            throw new TranslatedReaderException(i18n.getTranslation(invocation.getContext(Locale.class), MessageType.NEGATIVE, "The given log level is unknown."));
        }
        return logLevel;
    }
}
