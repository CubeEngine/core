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
package org.cubeengine.service.command;

import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.ContextValue;
import de.cubeisland.engine.butler.filter.RestrictedSourceException;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.service.user.MultilingualCommandSource;
import org.cubeengine.service.user.MultilingualPlayer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.command.CommandSource;

public class MultilingualContextValue implements ContextValue
{
    private I18n i18n;

    public MultilingualContextValue(I18n i18n)
    {
        this.i18n = i18n;
    }

    @Override
    public Object getContext(CommandInvocation invocation, Class<?> clazz)
    {
        if (invocation.getCommandSource() instanceof Player && clazz.isAssignableFrom(MultilingualPlayer.class))
        {

            return new MultilingualPlayer(i18n, ((Player)invocation.getCommandSource()));
        }
        if (invocation.getCommandSource() instanceof CommandSource && clazz.isAssignableFrom(MultilingualCommandSource.class))
        {
            return new MultilingualCommandSource<>(((CommandSource)invocation.getCommandSource()), i18n);
        }
        throw new RestrictedSourceException(null, clazz);
    }
}
