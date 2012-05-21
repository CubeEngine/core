package de.cubeisland.cubeengine.core.test.factory;

import junit.framework.TestCase;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.mockito.Matchers;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 *
 * @author Faithcaio
 */
public class PlayerFactory extends TestCase
{
    
    @Test
    public static Player createSimplePlayer(String name)
    {
        Player player = mock(Player.class);
        //Give Player his Name
        when(player.getName()).thenReturn(name);
        return player;
    }
    
    @Test
    public static Player createOPPlayer(String name)
    {
        Player player = mock(Player.class);
        //Give Player his Name
        when(player.getName()).thenReturn(name);
        //Give Player all Permissions
        when(player.hasPermission(Matchers.anyString())).thenReturn(true);
        when(player.isOp()).thenReturn(true);
        return player;
    }
    
    @Test
    public static Player createGuestPlayer(String name)
    {
        Player player = mock(Player.class);
        //Give Player his Name
        when(player.getName()).thenReturn(name);
        //Give Player all Permissions
        when(player.hasPermission(Matchers.anyString())).thenReturn(false);
        return player;
    }
    
}
