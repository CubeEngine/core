package de.cubeisland.cubeengine.core.persistence.filesystem.config;

import java.util.Collection;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

/**
 * A YamlConfiguration without bukkit
 *
 * @author Faithcaio
 */
public class YamlConfiguration extends AbstractConfiguration
{
    protected static final String BLANK_CONFIG = "{}\n";
    private final Yaml yaml;
    private final DumperOptions yamlOptions;
    private final Representer yamlRepresenter;

    public YamlConfiguration()
    {
        super();
        this.yamlOptions = new DumperOptions();
        this.yamlRepresenter = new YamlRepresenter();
        this.yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

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
        input = (Map<?, ?>) yaml.load(contents);
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
            if (value == null)
            {
                System.out.println("Error while saving Key: \"" + key + "\" was null");
            }
            else if (value instanceof ConfigurationSection)
            {
                out.append(this.offset(offset)).append(key).append(":").append(LINEBREAK);
                out.append(this.convertSection((ConfigurationSection) value, offset + 1, false));
            }
            else
            {
                out.append(this.offset(offset)).append(key).append(": ");
                if (value instanceof String)
                {
                    out.append(QUOTE).append(value.toString()).append(QUOTE);
                }
                else if (value instanceof Collection<?>)
                {
                    for (Object o : (Collection) value)
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
                    out.append(value.toString());
                }
                out.append(LINEBREAK);
            }
        }
        return out.toString();
    }

    public String buildComment(ConfigurationSection section, String path, int offset, boolean first)
    {
        String comment = section.comments.get(path);
        if (comment == null)
        {
            return "";
        }
        else
        {
            comment = comment.replace(LINEBREAK, LINEBREAK + this.offset(offset) + COMMENT_PREFIX);
            if (first)
            {
                return this.offset(offset) + COMMENT_PREFIX + comment + LINEBREAK;
            }
            return LINEBREAK + this.offset(offset) + COMMENT_PREFIX + comment + LINEBREAK;
        }
    }
}
