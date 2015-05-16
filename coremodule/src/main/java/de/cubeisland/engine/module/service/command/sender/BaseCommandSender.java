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
package de.cubeisland.engine.module.service.command.sender;

import de.cubeisland.engine.module.service.command.CommandSender;
import de.cubeisland.engine.module.core.i18n.I18n;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.core.util.formatter.MessageType;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.translation.Translation;

public abstract class BaseCommandSender implements CommandSender
{
    private final I18n i18n;
    private CoreModule core;

    protected BaseCommandSender(CoreModule core)
    {
        this.core = core;
        this.i18n = core.getModularity().start(I18n.class);
    }

    @Override
    public CoreModule getCore()
    {
        return this.core;
    }


    @Override
    public Translation getTranslation(MessageType type, String message, Object... args)
    {
        return i18n.getTranslation(type, getLocale(), message, args);
    }

    @Override
    public Translation getTranslationN(MessageType type, int n, String singular, String plural, Object... args)
    {
        return i18n.getTranslationN(type, getLocale(), n, singular, plural, args);
    }

    @Override
    public void sendTranslated(MessageType type, String message, Object... args)
    {
        this.sendMessage(Texts.of(this.getTranslation(type, message, args).get(getLocale())));
    }

    @Override
    public void sendTranslatedN(MessageType type, int n, String singular, String plural, Object... args)
    {
        this.sendMessage(this.getTranslationN(type, n, singular, plural, args).get(getLocale()));
    }
}
