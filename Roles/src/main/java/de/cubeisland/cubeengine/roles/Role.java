package de.cubeisland.cubeengine.roles;

import java.util.List;

/**
 * A role a User can have on the Server.
 */
public class Role
{
    
    
    private String name;
    
    private Role parentRole;
    
    private List<String> permissions;
}
