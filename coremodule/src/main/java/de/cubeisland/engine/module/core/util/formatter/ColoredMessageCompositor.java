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
package de.cubeisland.engine.module.core.util.formatter;

import java.util.Locale;

import de.cubeisland.engine.service.filesystem.FileManager;
import de.cubeisland.engine.module.core.util.ChatFormat;
import de.cubeisland.engine.messagecompositor.DefaultMessageCompositor;
import de.cubeisland.engine.reflect.Reflector;
import org.spongepowered.api.text.format.BaseFormatting;

import static de.cubeisland.engine.module.core.util.ChatFormat.BASE_CHAR;

public class ColoredMessageCompositor extends DefaultMessageCompositor
{
    private ColorConfiguration colorConfiguration;

    public ColoredMessageCompositor(Reflector reflector, FileManager fm)
    {
        reflector.getDefaultConverterManager().registerConverter(new ChatFormatConverter(), ChatFormat.class);

        // TODO this.colorConfiguration = reflector.load(ColorConfiguration.class, fm.getDataPath().resolve("formatColor" + FileExtensionFilter.YAML.getExtention()).toFile());
        this.registerMacro(new WorldFormatter())
            .registerMacro(new StringFormatter())
            .registerMacro(new BooleanFormatter())
            .registerMacro(new IntegerFormatter())
            .registerMacro(new CommandSenderFormatter())
            .registerMacro(new TextMacro())
            .registerMacro(new BiomeFormatter())
            .registerMacro(new VectorFormatter())
            .registerMacro(new DecimalFormatter())
            .registerReader("color", new ColorPostProcessor.ColorReader());

        this.addDefaultPostProcessor(new ColorPostProcessor());
    }

    public String composeMessage(BaseFormatting format, Locale locale, String sourceMessage, Object... messageArgs)
    {
        return this.composeMessage(locale, String.valueOf(BASE_CHAR) + format.getCode() + sourceMessage, messageArgs);
    }
}
