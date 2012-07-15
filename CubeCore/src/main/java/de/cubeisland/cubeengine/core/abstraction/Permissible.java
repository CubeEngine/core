package de.cubeisland.cubeengine.core.abstraction;

/**
 *
 * @author CodeInfection
 */
public interface Permissible extends Operator
{
    public boolean hasPermission(String permission);
}
