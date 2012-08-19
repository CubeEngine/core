package de.cubeisland.cubeengine.core.permission;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 *
 * @author Anselm Brehme
 */
public class Role
{
    public String name;
    public ArrayList<String> parents;
    public LinkedHashMap<String, String> meta;
    public ArrayList<String> permissions;

    public Role(String name, ArrayList<String> parents, LinkedHashMap<String, String> meta, ArrayList<String> permissions)
    {
        this.name = name;
        this.parents = parents;
        this.meta = meta;
        this.permissions = permissions;
 
    }
    
}
