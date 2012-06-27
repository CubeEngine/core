package de.cubeisland.cubeengine.core.persistence.filesystem.config.yaml;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.AbstractConfiguration;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.ConfigurationSection;
import java.util.Collection;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 * A YamlConfiguration without bukkit
 *
 * @author Faithcaio
 */
public class YamlConfiguration extends AbstractConfiguration
{
    private final Yaml yaml;

    public YamlConfiguration()
    {
        super();
        this.yaml = new Yaml();

        COMMENT_PREFIX = "# ";
        SPACES = "  ";
        LINEBREAK = "\n";
        QUOTE = "'";
    }

    public void loadFromString(String contents)
    {
        if (contents == null)
        {
            return;
        }
        Map<?, ?> input;
        input = (Map<?, ?>)yaml.load(contents);
        if (input != null)
        {
            convertMapsToSections(input, this);
        }
    }

    public String convertSection(ConfigurationSection section, int offset, boolean first)
    {
        StringBuilder out = new StringBuilder();
        for (String key : section.getKeys())
        {
            Object value = section.get(key);
            out.append(this.buildComment(section, key, offset, first));
            if (first && out.toString().length() != 0)
            {
                first = false;
            }
            if (value instanceof ConfigurationSection)
            {
                out.append(this.offset(offset)).append(key).append(":").append(LINEBREAK);
                out.append(this.convertSection((ConfigurationSection)value, offset + 1, false));
            }
            else
            {
                out.append(this.offset(offset)).append(key).append(": ");
                if (value instanceof String)
                {
                    out.append(QUOTE).append(value.toString()).append(QUOTE); //Quoting Strings
                }
                else if (value instanceof Collection<?>)
                {
                    for (Object o : (Collection)value) //Convert Collection
                    {
                        out.append(LINEBREAK);
                        out.append(this.offset(offset));
                        out.append("- ");
                        if (o instanceof String)
                        {
                            out.append(QUOTE).append(o.toString()).append(QUOTE);
                        }
                        else
                        {
                            out.append(o.toString());
                        }
                    }
                }
                else
                {
                    out.append(value.toString()); //else toString()
                }
                out.append(LINEBREAK);
            }
        }
        return out.toString();
    }

    public String buildComment(ConfigurationSection section, String path, int offset, boolean first)
    {
        String comment = section.getComments().get(path);
        if (comment == null)
        {
            return ""; //No Comment
        }
        else
        {
            comment = comment.replace(LINEBREAK, LINEBREAK + this.offset(offset) + COMMENT_PREFIX); //Multiline
            if (first)
            {
                return this.offset(offset) + COMMENT_PREFIX + comment + LINEBREAK; //First Comment
            }
            return LINEBREAK + this.offset(offset) + COMMENT_PREFIX + comment + LINEBREAK;
        }
    }
}
