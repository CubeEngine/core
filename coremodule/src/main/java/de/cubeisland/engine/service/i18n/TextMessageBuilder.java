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
package de.cubeisland.engine.service.i18n;

import de.cubeisland.engine.messagecompositor.parser.component.MessageComponent;
import de.cubeisland.engine.messagecompositor.parser.formatter.MessageBuilder;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextStyle;

public class TextMessageBuilder extends MessageBuilder<Text,TextBuilder>
{

    @Override
    public TextBuilder newBuilder()
    {
        return Texts.builder();
    }

    @Override
    public Text finalize(TextBuilder textBuilder)
    {
        return textBuilder.build();
    }

    @Override
    public void build(de.cubeisland.engine.messagecompositor.parser.component.Text component, TextBuilder builder)
    {
        builder.append(Texts.of(component.getString()));
    }

    @Override
    public void buildOther(MessageComponent styled, TextBuilder builder)
    {
        if (styled instanceof StyledComponent)
        {
            TextBuilder b = Texts.builder();
            if (((StyledComponent)styled).getFormat() instanceof TextColor)
            {
                b.color(((TextColor)((StyledComponent)styled).getFormat()));
            }
            else if (((StyledComponent)styled).getFormat() instanceof TextStyle)
            {
                b.style(((TextStyle)((StyledComponent)styled).getFormat()));
            }
            buildAny(((StyledComponent)styled).getComponent(), b);
            builder.append(b.build());
        }

        // TODO
    }
}
