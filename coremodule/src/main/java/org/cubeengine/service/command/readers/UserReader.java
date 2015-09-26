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

import java.util.Locale;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.parameter.reader.ArgumentReader;
import org.cubeengine.butler.parameter.reader.DefaultValue;
import org.cubeengine.butler.parameter.reader.ReaderException;
import org.cubeengine.service.command.TranslatedReaderException;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.service.user.UserManager;
import org.cubeengine.service.i18n.formatter.MessageType;
import org.spongepowered.api.entity.living.player.User;

/**
 * This argument is used to get users
 */
public class UserReader implements ArgumentReader<User>, DefaultValue<User>
{
    private final UserManager um;
    private final I18n i18n;

    public UserReader(UserManager um, I18n i18n)
    {
        this.um = um;
        this.i18n = i18n;
    }

    @Override
    public User read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String arg = invocation.consume(1);
        User user = um.findUser(arg);
        if (user == null)
        {
            throw new TranslatedReaderException(i18n.translate(invocation.getContext(Locale.class), MessageType.NEGATIVE,
                                                                                       "Player {user} not found!",
                                                                                       arg));
        }
        return user;
    }

    @Override
    public User getDefault(CommandInvocation invocation)
    {
        if (invocation.getCommandSource() instanceof User)
        {
            return (User)invocation.getCommandSource();
        }
        throw new ReaderException("You need to provide a player");
    }
}
