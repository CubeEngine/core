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
package org.cubeengine.service.user;

import java.util.UUID;
import org.cubeengine.service.i18n.I18n;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextFormat;

/**
 * Wrapper around {@link Player} for sending translated Messages
 */
public class MultilingualPlayer extends MultilingualCommandSource<Player>
{
    public MultilingualPlayer(I18n i18n, Player player)
    {
        super(player, i18n);
    }

    public boolean hasPermission(String perm)
    {
        return getSource().hasPermission(perm);
    }

    public UUID getUniqueId()
    {
        return getSource().getUniqueId();
    }

    /**
     * Returns the original wrapped player
     * @return the wrapped player
     */
    public Player original()
    {
        return getSource();
    }

    public String getName()
    {
        return getSource().getName();
    }

    public void sendMessage(Text msg)
    {
        if (getSource() != null)
        {
            getSource().sendMessage(msg);
        }
    }

    public void sendMessage(String msg)
    {
        if (msg != null)
        {
            sendMessage(Texts.of(msg));
        }
    }

    public void sendMessage(TextFormat format, String message, Object... params)
    {
        this.sendMessage(getI18n().composeMessage(getSource().getLocale(), format, message, params));
    }
}
