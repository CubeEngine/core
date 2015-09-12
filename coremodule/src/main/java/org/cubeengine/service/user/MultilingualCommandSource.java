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

import org.cubeengine.service.command.Multilingual;
import org.cubeengine.service.i18n.I18n;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.util.command.CommandSource;

public class MultilingualCommandSource<T extends CommandSource> implements Multilingual
{
    private T source;
    private I18n i18n;

    public MultilingualCommandSource(T source, I18n i18n)
    {
        this.source = source;
        this.i18n = i18n;
    }

    protected T getSource()
    {
        return source;
    }

    @Override
    public Text getTranslation(TextFormat format, String message, Object... args)
    {
        return i18n.getTranslation(getSource(), format, message, args);
    }

    @Override
    public Text getTranslationN(TextFormat format, int n, String singular, String plural, Object... args)
    {
        return i18n.getTranslationN(getSource(), format, n, singular, plural, args);
    }

    @Override
    public void sendTranslated(TextFormat format, String message, Object... args)
    {
        i18n.sendTranslated(getSource(), format, message, args);
    }

    @Override
    public void sendTranslatedN(TextFormat format, int n, String singular, String plural, Object... args)
    {
        i18n.sendTranslatedN(getSource(), format, n, singular, plural, args);
    }

    protected I18n getI18n()
    {
        return i18n;
    }
}
