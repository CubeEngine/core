package de.cubeisland.cubeengine.roles.role;

import java.util.List;

/**
 * A single role a User can have on the Server.
 */
public class Role
{
    private int priority;
    private String name;
    private Role parentRole;
    private List<String> permissions;
}
