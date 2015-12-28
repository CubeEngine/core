package org.cubeengine.service.i18n.formatter.component;

import org.cubeengine.dirigent.Component;
import org.spongepowered.api.text.Text;

public class TextComponent implements Component
{
    private final Text text;

    public TextComponent(Text text)
    {
        this.text = text;
    }

    public Text getText()
    {
        return text;
    }
}
