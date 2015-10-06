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
package org.cubeengine.module.core;

import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.parameter.reader.ArgumentReader;
import org.cubeengine.butler.parameter.reader.ReaderException;
import org.cubeengine.service.user.UserManager;
import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;

public class FindUserReader implements ArgumentReader<User>
{
    private final UserManager um;

    public FindUserReader(UserManager um)
    {
        this.um = um;
    }

    @Override
    public User read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String name = invocation.consume(1);
        Optional<User> found = um.getByName(name);
        if (!found.isPresent())
        {
            found = Optional.ofNullable(um.findUser(name, true));
        }
        if (found == null)
        {
            throw new ReaderException("No match found for {input}!", name);
        }
        return found.get();
    }
}
