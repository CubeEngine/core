package de.cubeisland.cubeengine.core.persistence.filesystem.config.ini;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.AbstractConfiguration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Faithcaio
 */
public class IniConfiguration extends AbstractConfiguration
{
    //TODO hier läuft noch alles schief ...
    public IniConfiguration()
    {
        super();
        
        COMMENT_PREFIX = "; ";
        SPACES = "  ";
        LINEBREAK = "\n";
        QUOTE = "\"";
    }

    public void loadFromString(String contents)
    {
        if (contents == null)
        {
            return;
        }
        //this.values = (LinkedHashMap<String, Object>)yaml.load(contents); //TODO find an ini parser
        if (this.values == null)
        {
            this.values = new LinkedHashMap<String, Object>();
        }
    }

    public String convertValue(String path, Object value, int off)
    {
        //TODO convertValues correct
        StringBuilder sb = new StringBuilder();

        sb.append(this.buildComment(path, 0));

        if (value instanceof Map)
        {
            sb.append(LINEBREAK);
            sb.append("[").append(this.getLastSubKey(path)).append("]");
            sb.append(LINEBREAK);
            sb.append(this.convertSection(path, (Map<String, Object>)value, off + 1));
            return sb.toString();
        }
        else if (value instanceof String)
        {
            sb.append(path).append(" = ");
            sb.append(QUOTE).append(value.toString()).append(QUOTE); //Quoting Strings
        }
        else if (value instanceof Collection<?>)
        {
            sb.append(path).append(" = ");
            sb.append("[");
            for (Object o : (Collection)value) //Convert Collection
            {
                if (o instanceof String)
                {
                    sb.append(QUOTE).append(o.toString()).append(QUOTE);
                }
                else
                {
                    sb.append(o.toString());
                }
                sb.append(",");
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("]");
        }
        else
        {
            sb.append(value.toString());
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
            sb.append(this.convertValue(path + "." + key, value, off));
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
            comment = comment.replace(LINEBREAK, LINEBREAK + COMMENT_PREFIX); //Multiline
            comment = COMMENT_PREFIX + comment + LINEBREAK;
            if (this.first)
            {
                this.first = false;
                return comment;
            }
            return LINEBREAK + comment;
        }
    }
}
