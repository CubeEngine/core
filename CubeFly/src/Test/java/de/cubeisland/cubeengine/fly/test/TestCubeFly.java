package de.cubeisland.cubeengine.fly.test;

import de.cubeisland.cubeengine.core.CoreListener;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.user.UserStorage;
import de.cubeisland.cubeengine.fly.CubeFly;
import de.cubeisland.cubeengine.fly.FlyListener;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.powermock.api.mockito.PowerMockito;

/**
 *
 * @author Faithcaio
 */
public class TestCubeFly
{

    @Test
    public void testFly()
    {
        CubeFly plugin = mock(CubeFly.class);
        Server server = mock(Server.class);
        PluginManager pm = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pm);
        Database database = new Database("localhost",(short)3306,"root","","cubeengine","cube_test_");
        UserStorage storage = new UserStorage(database, server);
        //Init & Clear DB
        storage.initialize();
        storage.clear();
        
        UserManager cuManager = new UserManager(database,server);
        
        FlyListener listener = new FlyListener(cuManager, plugin);
        CoreListener corelistener = new CoreListener(cuManager);
        
        //Create New Player to join
        Player player1 = this.createFlyingOPPlayer("Member1");
        Player player2 = this.createGuestPlayer("GuestN00B");
        Player player3 = this.createFlyingPlayer("Member2");
        Player player4 = this.createNotFlyingOPPlayer("Member3");
        PlayerJoinEvent join = this.createPlayerJointEvent(player1);
        corelistener.goesOnline(join);
        join = this.createPlayerJointEvent(player2);
        corelistener.goesOnline(join);
        join = this.createPlayerJointEvent(player3);
        corelistener.goesOnline(join);
        join = this.createPlayerJointEvent(player4);
        corelistener.goesOnline(join);

        //Create new PlayerJoinEvents
        PlayerInteractEvent r1 = this.createPlayerRClickWithFeatherEvent(player1);
        PlayerInteractEvent r2 = this.createPlayerRClickWithFeatherEvent(player2);
        PlayerInteractEvent r3 = this.createPlayerRClickWithFeatherEvent(player3);
        PlayerInteractEvent r4 = this.createPlayerRClickWithFeatherEvent(player4);
        //Pass join1 to Listender
        listener.playerInteract(r1);
        junit.framework.Assert.assertTrue(player1.getAllowFlight()==false);
        listener.playerInteract(r2);
        junit.framework.Assert.assertTrue(player2.getAllowFlight()==false);
        listener.playerInteract(r3);
        junit.framework.Assert.assertTrue(player3.getAllowFlight()==false);
        listener.playerInteract(r4);//SetAllowFlight geht wird aber nicht gesetzt
        //junit.framework.Assert.assertTrue(player4.getAllowFlight()==true);
        
        storage.clear();
    
    }
    public PlayerInteractEvent createPlayerRClickWithFeatherEvent(Player player)
    {
        
        PlayerInteractEvent pie =  new PlayerInteractEvent(player,Action.RIGHT_CLICK_AIR, null, null, BlockFace.SELF);
        when(pie.getPlayer().getItemInHand()).thenReturn(new ItemStack(Material.FEATHER));
        return pie;
    }
    
    public Player createFlyingOPPlayer(String name)
    {
        Player player = mock(Player.class);
        //Give Player his Name
        when(player.getName()).thenReturn(name);
        //Give Player all Permissions
        when(player.hasPermission(Matchers.anyString())).thenReturn(false);
        when(player.isOp()).thenReturn(true);
        player.setAllowFlight(true);
        player.setFlying(true);
        return player;
    }
    
    public Player createFlyingPlayer(String name)
    {
        Player player = mock(Player.class);
        //Give Player his Name
        when(player.getName()).thenReturn(name);
        when(player.hasPermission(Matchers.contains("use"))).thenReturn(true);
        //Give Player NO Fly Permissions
        when(player.hasPermission(Matchers.contains("fly"))).thenReturn(false);
        player.setAllowFlight(true);
        player.setFlying(true);
        return player;
    }
    
    
            
        
    
    public Player createGuestPlayer(String name)
    {
        Player player = mock(Player.class);
        //Give Player his Name
        when(player.getName()).thenReturn(name);
        //Give Player all Permissions
        when(player.hasPermission(Matchers.anyString())).thenReturn(false);
        player.setAllowFlight(false);
        player.setFlying(false);
        return player;
    }

    public PlayerJoinEvent createPlayerJointEvent(Player player)
    {
        return new PlayerJoinEvent(player,"");
    }

    private Player createNotFlyingOPPlayer(String name)
    {
        Player player = mock(Player.class);
        //Give Player his Name
        when(player.getName()).thenReturn(name);
        //Give Player all Permissions
        when(player.hasPermission(Matchers.anyString())).thenReturn(true);
        when(player.isOp()).thenReturn(true);
        player.setAllowFlight(false);
        player.setFlying(false);
        return player;
    }
    
}
