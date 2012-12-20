package de.cubeisland.cubeengine.core.config.codec;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.ConfigurationCodec;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class acts as a codec for yaml-configurations.
 */
public class YamlCodec extends ConfigurationCodec
{
    private final Yaml yaml;

    public YamlCodec()
    {
        super();
        this.yaml = new Yaml();

        COMMENT_PREFIX = "# ";
        SPACES = "  ";
        LINE_BREAK = "\n";
        QUOTE = "'";
    }

    //TODO \n in Strings do get lost when restarting
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadFromInputStream(InputStream is)
    {
        Map<Object, Object> map = (Map<Object, Object>)yaml.load(is);
        if (map == null)
        {
            return null;
        }
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
            CubeEngine.getLogger().log(LogLevel.WARNING, "Invalid revision in a configuration!", ex);
        }
        Map<String, Object> resultmap = new LinkedHashMap<String, Object>();
        for (Object key : map.keySet())
        {
            resultmap.put(key.toString(), map.get(key));
        }
        return resultmap;
    }

    private boolean needsQuote(Object o)
    {
        String s = o.toString();
        return (s.startsWith("#") || s.contains(" #") || s.startsWith("@")
                || s.startsWith("`") || s.startsWith("[") || s.startsWith("]")
                || s.startsWith("{") || s.startsWith("}") || s.startsWith("|")
                || s.startsWith(">") || s.startsWith("!") || s.startsWith("%")
                || s.endsWith(":") || s.startsWith("- ") || s.startsWith(",")
                || s.matches("[0-9]+:[0-9]+")) || s.isEmpty() || s.equals("*");
    }

    @Override
    @SuppressWarnings("unchecked")
    public String convertValue(String path, Object value, int off, boolean inCollection)
    {
        StringBuilder sb = new StringBuilder();
        String offset = this.offset(off);
        String key = this.getSubKey(path);
        if (!inCollection) //do not append offset when converting a value in a list
        {
            sb.append(offset);
        }
        sb.append(key).append(":");
        if (value == null)
        {
            sb.append(" ");
        }
        else
        {
            if (value instanceof Map)
            {
                sb.append(LINE_BREAK);
                sb.append(this.convertMap(path, (Map<String, Object>)value, off + 1, inCollection));
                return sb.toString();
            }
            else
            {
                if (value instanceof String)
                {
                    sb.append(" ");
                    if (this.needsQuote(value))
                    {
                        sb.append(QUOTE).append(value.toString()).append(QUOTE);
                    }
                    else
                    {
                        sb.append(value.toString());
                    }
                }
                else
                {
                    if (value instanceof Collection<?>)
                    {
                        if (((Collection<?>)value).isEmpty())
                        {
                            return sb.append(" []").append(LINE_BREAK).toString();
                        }
                        for (Object o : (Collection<?>)value) //Convert Collection
                        {
                            sb.append(LINE_BREAK).append(offset).append("- ");
                            if (o instanceof String)
                            {
                                if (this.needsQuote(o))
                                {
                                    sb.append(QUOTE).append(o.toString()).append(QUOTE);
                                }
                                else
                                {
                                    sb.append(o.toString());
                                }
                            }
                            else if (o instanceof Map)
                            {
                                sb.append(this.convertMap(path, (Map<String, Object>)o, off + 1, true));
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
        if (!inCollection && !sb.toString().endsWith(LINE_BREAK + LINE_BREAK)) // if in collection the collection will append its linebreak
        {
            sb.append(LINE_BREAK);
        }
        this.first = false;
        return sb.toString();
    }

    @Override
    public String convertMap(String path, Map<String, Object> values, int off, boolean inCollection)
    {
        StringBuilder sb = new StringBuilder();
        if (values.isEmpty())
        {
            return sb.append(this.offset(off)).append("{}").append(LINE_BREAK).toString();
        }
        for (Entry<String, Object> entry : values.entrySet())
        {
            String subPath = off == 0 ? entry.getKey() : (path + PATH_SEPARATOR + entry.getKey());
            String comment = this.buildComment(subPath, off);
            if (!comment.isEmpty())
            {
                if (!sb.toString().endsWith(LINE_BREAK + LINE_BREAK)) // if not already one line free
                {
                    sb.append(LINE_BREAK); // add free line before comment
                }
                sb.append(comment);
            }
            sb.append(this.convertValue(subPath, entry.getValue(), off, inCollection));
        }
        if (!sb.toString().endsWith(LINE_BREAK + LINE_BREAK))
        {
            sb.append(LINE_BREAK);
        }
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
        comment = comment.replace(LINE_BREAK, LINE_BREAK + offset + COMMENT_PREFIX); // multi line
        comment = offset + COMMENT_PREFIX + comment + LINE_BREAK;
        if (this.first)
        {
            this.first = false;
        }
        return comment;
    }

    @Override
    public String getExtension()
    {
        return "yml";
    }
}
