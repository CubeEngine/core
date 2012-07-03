package de.cubeisland.cubeengine.core.persistence.filesystem.config.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import de.cubeisland.cubeengine.core.persistence.filesystem.config.ConfigurationRepresenter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Faithcaio
 */
public class JsonConfiguration extends ConfigurationRepresenter
{
    private JsonParser json;

    public JsonConfiguration()
    {
        super();
        this.json = new JsonParser();
        COMMENT_PREFIX = "// ";
        SPACES = "  ";
        LINEBREAK = "\n";
        QUOTE = "\"";
    }

    @Override
    public String convertMap(String path, Map<String, Object> values, int off)
    {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = values.keySet().iterator();
        String key = iterator.next();
        Object value = values.get(key);
        if (off == 0)
        {
            sb.append(this.convertValue(key, value, off));
        }
        else
        {
            sb.append(this.convertValue(path + "." + key, value, off));
        }
        while (iterator.hasNext())
        {
            key = iterator.next();
            value = values.get(key);
            sb.append(" ,").append(LINEBREAK);
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

    @Override
    public String convertValue(String path, Object value, int off)
    {
        StringBuilder sb = new StringBuilder();
        String offset = this.offset(off);
        String key = this.getSubKey(path);
        //sb.append(this.buildComment(path, off)); //TODO comments in Json dont work?
        sb.append(offset).append(QUOTE).append(key).append(QUOTE).append(":");//{_OFFSET_Key:}
        if (value instanceof Map)
        {
            sb.append(" {").append(LINEBREAK);
            sb.append(this.convertMap(path, (Map<String, Object>)value, off + 1));
            sb.append(LINEBREAK).append(this.offset(off + 1)).append("}");
            return sb.toString();
        }
        else if (value instanceof String)
        {
            sb.append(QUOTE).append(value.toString()).append(QUOTE); //Quoting Strings
        }
        else if (value instanceof Collection<?>)
        {
            sb.append(" [").append(LINEBREAK);
            Iterator iterator = ((Collection)value).iterator();
            if (iterator.hasNext())
            {
                sb.append(this.convertElementofCollection(iterator.next(), off));
            }
            while (iterator.hasNext())
            {
                sb.append(",").append(LINEBREAK);
                sb.append(this.convertElementofCollection(iterator.next(), off));
            }
            sb.append(LINEBREAK).append(this.offset(off)).append("]");
        }
        else
        {
            sb.append(value.toString());
        }
        this.first = false;
        return sb.toString();
    }

    /**
     * Converts a Collection into a String for save
     *
     * @param value the Collection
     * @param off the offset
     * @return the converted Collection
     */
    public String convertElementofCollection(Object value, int off)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.offset(off + 1));
        if (value instanceof String)
        {
            sb.append(QUOTE).append(value.toString()).append(QUOTE);
        }
        else
        {
            sb.append(value.toString());
        }
        return sb.toString();
    }

    @Override
    public void loadFromString(String contents)
    {
        if (contents == null)
        {
            return;
        }
        JsonElement elem = json.parse(contents);
        this.values = loadFromJsonObject(elem.getAsJsonObject());
        if (this.values == null)
        {
            this.values = new LinkedHashMap<String, Object>();
        }
    }

    private LinkedHashMap<String, Object> loadFromJsonObject(JsonObject jsonObject)
    {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        Iterator<Entry<String, JsonElement>> iterator = jsonObject.entrySet().iterator();
        while (iterator.hasNext())
        {
            Entry<String, JsonElement> entry = iterator.next();
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonNull())
            {
                return null;
            }
            else if (value.isJsonPrimitive())
            {
                map.put(key, this.deserializeJsonPrimitives(value.getAsJsonPrimitive()));
                continue;
            }
            else if (value.isJsonArray())
            {
                Collection<Object> col = new ArrayList<Object>();
                JsonArray jsonArray = value.getAsJsonArray();
                for (JsonElement elem : jsonArray)
                {
                    if (elem.isJsonNull())
                    {
                        continue;
                    }
                    if (elem.isJsonPrimitive())
                    {
                        col.add(this.deserializeJsonPrimitives(elem.getAsJsonPrimitive()));
                        continue;
                    }
                    else
                    {
                        col.add(this.loadFromJsonObject(elem.getAsJsonObject()));
                    }
                }
                map.put(key, col);
            }
            else
            {
                map.put(key, this.loadFromJsonObject(value.getAsJsonObject()));
            }
        }
        return map;
    }

    private Object deserializeJsonPrimitives(JsonPrimitive jsonprim)
    {
        if (jsonprim.isBoolean())
        {
            return jsonprim.getAsBoolean();
        }
        else if (jsonprim.isString())
        {
            return jsonprim.getAsString();
        }
        else
        {
            BigDecimal bigDec = jsonprim.getAsBigDecimal();
            try
            {
                return bigDec.intValueExact();
            }
            catch (Exception e)
            {
            }
            return bigDec.doubleValue();
        }
    }

    @Override
    public String buildComment(String path, int off)
    {
        //TODO no need for Comments?
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

    @Override
    public String head()
    {
        return "{\n";
    }

    @Override
    public String tail()
    {
        return "\n}";
    }
}
