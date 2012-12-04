package de.cubeisland.cubeengine.core.config.codec;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.ConfigurationCodec;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.yaml.snakeyaml.Yaml;

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
        LINEBREAK = "\n";
        QUOTE = "'";
    }

    //TODO \n in Strings do get lost when restarting
    @Override
    public Map<String, Object> loadFromInputStream(InputStream is)
    {
        Map<Object, Object> map = (Map<Object, Object>) yaml.load(is);
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
                || s.startsWith(">") || s.startsWith("!")|| s.startsWith("%")
                || s.endsWith(":") || s.startsWith("- ") || s.startsWith(",")
                || s.matches("[0-9]+:[0-9]+")) || s.isEmpty();
    }

    @Override
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
                sb.append(LINEBREAK);
                sb.append(this.convertMap(path, (Map<String, Object>) value, off + 1, inCollection));
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
                        if (((Collection<?>) value).isEmpty())
                        {
                            return sb.append(" []").append(LINEBREAK).toString();
                        }
                        for (Object o : (Collection<?>) value) //Convert Collection
                        {
                            sb.append(LINEBREAK).append(offset).append("- ");
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
                                sb.append(this.convertMap(path, (Map<String, Object>) o, off + 1, true));
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
        if (!inCollection && !sb.toString().endsWith(LINEBREAK + LINEBREAK)) // if in collection the collection will append its linebreak
        {
            sb.append(LINEBREAK);
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
            return sb.append(this.offset(off)).append("{}").append(LINEBREAK).toString();
        }
        for (Entry<String, Object> entry : values.entrySet())
        {
            String subpath = off == 0 ? entry.getKey().toString() : (path + PATHSEPARATOR + entry.getKey());
            String comment = this.buildComment(subpath, off);
            if (!comment.isEmpty())
            {
                if (!sb.toString().endsWith(LINEBREAK + LINEBREAK)) // if not already one line free
                {
                    sb.append(LINEBREAK); // add free line before comment
                }
                sb.append(comment);
            }
            sb.append(this.convertValue(subpath, entry.getValue(), off, inCollection));
        }
        if (!sb.toString().endsWith(LINEBREAK + LINEBREAK))
        {
            sb.append(LINEBREAK);
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
        comment = comment.replace(LINEBREAK, LINEBREAK + offset + COMMENT_PREFIX); //Multiline
        comment = offset + COMMENT_PREFIX + comment + LINEBREAK;
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
