package de.cubeisland.cubeengine.basics.command.moderation.kit;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import org.bukkit.Material;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KitItemConverter implements Converter<KitItem>
{
    private static final Pattern pat = Pattern.compile("(?:([0-9]+)\\*)?([a-zA-Z0-9]+)(?::([0-9]+))?(?: (.+))?");

    @Override
    public Node toNode(KitItem object) throws ConversionException
    {
        return Convert.wrapIntoNode(object.amount
                + "*" + object.mat.getId()
                + ":" + object.dura +
                (object.customName == null ? "" : " " + object.customName));
    }

    @Override
    public KitItem fromNode(Node node) throws ConversionException
    {
        //TODO add support for enchantments
        //suported formats: [amount*]id[:data][ customname] 
        String itemString = node.unwrap();
        if (itemString.matches(pat.pattern()))
        {
            Matcher matcher = pat.matcher(itemString);
            matcher.find();
            String materialString = matcher.group(2);
            String duraString = matcher.group(3);
            String amountString = matcher.group(1);
            String name = matcher.group(4);
            int amount;
            short dura;
            try
            {
                Material mat = Match.material().material(materialString);
                if (amountString == null)
                {
                    amount = mat.getMaxStackSize();
                }
                else
                {
                    amount = Integer.parseInt(amountString);
                }
                if (duraString == null)
                {
                    dura = 0;
                }
                else
                {
                    dura = Short.parseShort(duraString);
                }
                return new KitItem(mat, dura, amount, name);
            }
            catch (Exception ex)
            {
                throw new ConversionException("Could not parse kitItem!", ex);
            }
        }
        throw new ConversionException("Could not parse kitItem!");
    }
}
