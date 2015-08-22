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
package org.cubeengine.service.command.sender;

import java.util.Locale;
import java.util.UUID;

import org.cubeengine.service.i18n.I18n;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandSource;

public class WrappedCommandSender<W extends CommandSource> extends BaseCommandSender
{
    private final W wrapped;

    public WrappedCommandSender(I18n i18n, W sender)
    {
        super(i18n);
        this.wrapped = sender;
    }

    @Override
    public UUID getUniqueId()
    {
        if (wrapped instanceof Player)
        {
            return ((Player)wrapped).getUniqueId();
        }
        return NON_PLAYER_UUID;
    }

    @Override
    public String getName()
    {
        return this.getWrappedSender().getIdentifier();
    }

    @Override
    public Text getDisplayName()
    {
        return Texts.of(getWrappedSender().getName());
    }

    @Override
    public Locale getLocale()
    {
        return i18n.getDefaultLanguage().getLocale();
    }

    @Override
    public boolean hasPermission(String name)
    {
        return this.getWrappedSender().hasPermission(name);
    }

    public W getWrappedSender()
    {
        return this.wrapped;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o instanceof WrappedCommandSender)
        {
            return ((WrappedCommandSender)o).getName().equals(this.getWrappedSender().getName());
        }
        else if (o instanceof CommandSource)
        {
            return ((CommandSource)o).getName().equals(this.getWrappedSender().getName());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return this.getWrappedSender().hashCode();
    }

    @Override
    public void sendMessage(String msg)
    {
        if (msg != null)
        {
            this.sendMessage(Texts.of(msg));
        }
    }

    @Override
    public void sendMessage(Text msg)
    {
        getWrappedSender().sendMessage(msg);
    }
}
