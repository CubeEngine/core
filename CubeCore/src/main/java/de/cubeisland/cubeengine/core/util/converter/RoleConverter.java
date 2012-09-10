package de.cubeisland.cubeengine.core.util.converter;

import de.cubeisland.cubeengine.core.permission.Role;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Anselm Brehme
 */
public class RoleConverter implements Converter<Role>
{
    @Override
    public Object toObject(Role role) throws ConversionException
    {
        Map<String, Object> loc = new LinkedHashMap<String, Object>();
        loc.put("name", role.name);
        loc.put("parents", role.parents);
        loc.put("meta", role.meta);
        loc.put("permissions", role.permissions);
        return loc;
    }

    @Override
    public Role fromObject(Object object) throws ConversionException
    {
        Map<String, Object> input = (Map<String, Object>)object;
        String name = input.get("name").toString();
        ArrayList<String> parents = (ArrayList<String>)input.get("parents");
        LinkedHashMap<String, String> meta = (LinkedHashMap<String, String>)input.get("meta");
        ArrayList<String> permissions = (ArrayList<String>)input.get("permissions");

        return new Role(name, parents, meta, permissions);
    }
    
    @Override
    public String toString(Role object)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Role fromString(String string) throws ConversionException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
