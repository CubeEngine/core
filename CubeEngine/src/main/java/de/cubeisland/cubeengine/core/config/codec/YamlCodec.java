package de.cubeisland.cubeengine.core.config.codec;

import de.cubeisland.cubeengine.core.config.ConfigurationCodec;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 * This class acts as a codec for yaml-configurations.
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
    public Map<String, Object> loadFromInputStream(InputStream is)
    {
        Map<String, Object> map = (Map<String, Object>)yaml.load(is);
        try
        {
            Object rev = map.get("revision");
            if (rev != null)
            {
                this.revision = Convert.fromObject(Integer.class, rev);
            }
        }
        catch (ConversionException ex)
        {
        } // invalid revision //TODO handle this?
        return map;
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
                sb.append(this.
                    convertMap(path, (Map<String, Object>)value, off + 1));
                return sb.toString();
            }
            else
            {
                if (value instanceof String)
                {
                    sb.append(" ").append(QUOTE).append(value.toString()).
                        append(QUOTE); //Quoting Strings
                }
                else
                {
                    if (value instanceof Collection<?>)
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
                                sb.append(QUOTE).append(o.toString()).
                                    append(QUOTE);
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
            }
        }
        sb.append(LINEBREAK);
        this.first = false;
        return sb.toString();
    }

    @Override
    public String convertMap(String path, Map<String, Object> values, int off)
    {
        StringBuilder sb = new StringBuilder();
        if (values.isEmpty())
        {
            return sb.append(this.offset(off)).append("{}").append(LINEBREAK).
                toString();
        }
        useLineBreak = false;
        for (Map.Entry<String, Object> entry : values.entrySet())
        {
            if (off == 0)
            {
                sb.append(this.buildComment(entry.getKey(), off))
                    .append(this.
                    convertValue(entry.getKey(), entry.getValue(), off));//path value off
            }
            else
            {
                sb.append(this.buildComment(path + "." + entry.getKey(), off))
                    .append(this.
                    convertValue(path + "." + entry.getKey(), entry.getValue(), off));
            }
            if (!first)
            {
                useLineBreak = true;
            }
        }
        if (!sb.toString().endsWith(LINEBREAK + LINEBREAK))
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
        String comment = this.getContainer().getComment(path);
        if (comment == null)
        {
            return ""; //No Comment
        }
        String offset = this.offset(off);
        comment = comment.
            replace(LINEBREAK, LINEBREAK + offset + COMMENT_PREFIX); //Multiline
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

    @Override
    public String revision()
    {
        {
            if (revision != null)
            {
                return new StringBuilder("revision: ").append(this.revision).
                    append(LINEBREAK).toString();
            }
            return "";
        }
    }
}