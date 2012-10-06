package de.cubeisland.cubeengine.core.config.codec;

import de.cubeisland.cubeengine.core.config.ConfigurationCodec;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 * A YamlConfiguration without bukkit
 *
 * @author Anselm Brehme
 */
public class YamlCodec extends ConfigurationCodec
{
    private final Yaml yaml;
    private boolean useLineBreak = false;

    public YamlCodec()
    {
        super();
        this.yaml = new Yaml();

        COMMENT_PREFIX = "# ";
        SPACES = "  ";
        LINEBREAK = "\n";
        QUOTE = "'";
    }

    @Override
    public Map<String, Object> loadFromString(String contents)
    {
        if (contents == null)
        {
            return new LinkedHashMap<String, Object>();
        }
        Map<String, Object> values = (Map<String, Object>)yaml.load(contents);
        if (values == null)
        {
            return new LinkedHashMap<String, Object>();
        }
        this.loadedKeys = new HashMap<String, String>();
        this.loadedKeys(values);
        return values;
    }

    @Override
    protected void loadedKeys(Map<String, Object> values)
    {
        for (String key : values.keySet())
        {
            this.loadedKeys.put(key.toLowerCase(Locale.ENGLISH), key);
            if (values.get(key) instanceof Map)
            {
                this.loadedKeys((Map<String, Object>)values.get(key));
            }
        }
    }

    @Override
    public String convertValue(String path, Object value, int off)
    {
        StringBuilder sb = new StringBuilder();
        String offset = this.offset(off);
        String key = this.getSubKey(path);
        sb.append(offset).append(key).append(":");//{_OFFSET_Key:}
        if (value == null)
        {
            sb.append(" ");
        }
        else
        {
            if (value instanceof Map)
            {
                sb.append(LINEBREAK);
                sb.append(this.convertMap(path, (Map<Object, Object>)value, off + 1));
                return sb.toString();
            }
            else if (value instanceof String)
            {
                sb.append(" ").append(QUOTE).append(value.toString()).append(QUOTE); //Quoting Strings
            }
            else if (value instanceof Collection<?>)
            {
                if (((Collection<?>)value).isEmpty())
                {
                    return sb.append(" []").append(LINEBREAK).toString();
                }
                for (Object o : (Collection<?>)value) //Convert Collection
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
        }
        sb.append(LINEBREAK);
        this.first = false;
        return sb.toString();
    }

    @Override
    public String convertMap(String path, Map<Object, Object> values, int off)
    {
        StringBuilder sb = new StringBuilder();
        if (values.isEmpty())
        {
            return sb.append(this.offset(off)).append("{}").append(LINEBREAK).toString();
        }
        useLineBreak = false;
        for (Map.Entry<Object, Object> entry : values.entrySet())
        {
            if (off == 0)
            {
                sb.append(this.buildComment(entry.getKey().toString(), off))
                    .append(this.convertValue(entry.getKey().toString(), entry.getValue(), off));//path value off
            }
            else
            {
                sb.append(this.buildComment(path + "." + entry.getKey(), off))
                    .append(this.convertValue(path + "." + entry.getKey(), entry.getValue(), off));
            }
            if (!first)
            {
                useLineBreak = true;
            }
        }
        if (!sb.toString().endsWith(LINEBREAK+LINEBREAK))
        {
            sb.append(LINEBREAK);
        }
        useLineBreak = false;
        first = true;
        return sb.toString();
    }

    @Override
    public String buildComment(String path, int off)
    {
        String comment = this.comments.get(path);
        if (comment == null)
        {
            return ""; //No Comment
        }
        String offset = this.offset(off);
        comment = comment.replace(LINEBREAK, LINEBREAK + offset + COMMENT_PREFIX); //Multiline
        comment = offset + COMMENT_PREFIX + comment + LINEBREAK;
        if (this.first)
        {
            this.first = false;
        }
        if (useLineBreak)
        {
            comment = LINEBREAK + comment;
        }
        return comment;
    }

    @Override
    public String getExtension()
    {
        return "yml";
    }
}