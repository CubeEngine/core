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
package de.cubeisland.engine.service.command.sender;

import de.cubeisland.engine.service.i18n.I18n;
import de.cubeisland.engine.service.command.CommandSender;
import org.spongepowered.api.text.Text.Translatable;
import org.spongepowered.api.text.format.BaseFormatting;

import static de.cubeisland.engine.module.core.util.ChatFormat.BASE_CHAR;
import static de.cubeisland.engine.module.core.util.ChatFormat.fromLegacy;

public abstract class BaseCommandSender implements CommandSender
{
    private final I18n i18n;

    protected BaseCommandSender(I18n i18n)
    {
        this.i18n = i18n;
    }

    @Override
    public Translatable getTranslation(BaseFormatting format, String message, Object... args)
    {
        return i18n.getTranslation(format, getLocale(), message, args);
    }

    @Override
    public Translatable getTranslationN(BaseFormatting format, int n, String singular, String plural, Object... args)
    {
        return i18n.getTranslationN(format, getLocale(), n, singular, plural, args);
    }

    @Override
    public void sendTranslated(BaseFormatting format, String message, Object... args)
    {
        sendMessage(fromLegacy(this.getTranslation(format, message, args).getTranslation().get(getLocale()), BASE_CHAR));
    }

    @Override
    public void sendTranslatedN(BaseFormatting format, int n, String singular, String plural, Object... args)
    {
        sendMessage(fromLegacy(this.getTranslationN(format, n, singular, plural, args).getTranslation().get(getLocale()), BASE_CHAR));
    }
}
