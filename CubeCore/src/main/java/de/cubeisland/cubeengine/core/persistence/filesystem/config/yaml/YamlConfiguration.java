package de.cubeisland.cubeengine.core.persistence.filesystem.config.yaml;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.AbstractConfiguration;
import java.util.Collection;
import java.util.LinkedHashMap;
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
        this.values = (LinkedHashMap<String, Object>)yaml.load(contents);
        if (this.values == null)
        {
            this.values = new LinkedHashMap<String, Object>();
        }
    }

    public String convertValue(String path, Object value, int off)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(this.buildComment(path, off));

        String offset = this.offset(off);
        String key = this.getLastSubKey(path);
        sb.append(offset).append(key).append(":");//{_OFFSET_Key:}
        if (value instanceof Map)
        {
            sb.append(LINEBREAK);
            sb.append(this.convertSection(path, (Map<String, Object>)value, off + 1));
            return sb.toString();
        }
        else if (value instanceof String)
        {
            sb.append(" ").append(QUOTE).append(value.toString()).append(QUOTE); //Quoting Strings
        }
        else if (value instanceof Collection<?>)
        {
            for (Object o : (Collection)value) //Convert Collection
            {
                sb.append(LINEBREAK).append(offset).append("- ");
                if (o instanceof String)
                {
                    sb.append(QUOTE).append(o.toString()).append(QUOTE);
                }
                else
                {
                    sb.append(o.toString());
                }
            }
        }
        else
        {
            sb.append(" ").append(value.toString());
        }
        sb.append(LINEBREAK);
        this.first = false;
        return sb.toString();
    }

    public String convertSection(String path, Map<String, Object> values, int off)
    {
        StringBuilder sb = new StringBuilder();
        for (String key : values.keySet())
        {
            Object value = values.get(key);
            if (off == 0)
            {
                sb.append(this.convertValue(key, value, off));
            }
            else
            {
                sb.append(this.convertValue(path + "." + key, value, off));
            }
        }
        return sb.toString();
    }

    public String buildComment(String path, int off)
    {
        String comment = this.comments.get(path);
        if (comment == null)
        {
            return ""; //No Comment
        }
        else
        {
            String offset = this.offset(off);
            comment = comment.replace(LINEBREAK, LINEBREAK + offset + COMMENT_PREFIX); //Multiline
            comment = offset + COMMENT_PREFIX + comment + LINEBREAK;
            if (this.first)
            {
                this.first = false;
                return comment;
            }
            return LINEBREAK + comment;
        }
    }
}
