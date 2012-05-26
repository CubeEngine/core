package de.cubeisland.cubeengine.core.test;

import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.test.factory.TestInstanceFactory;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.user.UserStorage;
import junit.framework.TestCase;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.mockito.Matchers;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserCreationTest extends TestCase
{
    TestInstanceFactory factory;
    
    @Before
    public void initTests()
    {
        factory = new TestInstanceFactory();
        factory.setUp();
    }

    
    //@Test - disabled for now
    public void testCorePlayerJoinEvent()
    {
        Server server = mock(Server.class);
        PluginManager pm = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pm);
        Database database = new Database("localhost",(short)3306,"root","","cubeengine","cube_test_");
        
        UserStorage storage = new UserStorage(database, server);
        //Init & Clear DB
        storage.initialize();
        storage.clear();
        //is Empty?
        assertTrue(storage.getAll().isEmpty());
        
//        UserManager userManager = new UserManager(database,server);
//        CoreListener listener = new CoreListener(userManager);
//
//        //Create New Player to join
//        Player player1 = this.createOPPlayer("Member1");
//        Player player2 = this.createGuestPlayer("GuestN00B");
//        Player player3 = this.createOPPlayer("Member2");
//        Player player4 = this.createOPPlayer("Member3");
//        //Create new PlayerJoinEvents
//        PlayerJoinEvent join1 = this.createPlayerJointEvent(player1);
//        PlayerJoinEvent join2 = this.createPlayerJointEvent(player2);
//        PlayerJoinEvent join3 = this.createPlayerJointEvent(player3);
//        PlayerJoinEvent join4 = this.createPlayerJointEvent(player4);
//        //Pass join1 to Listender
//        listener.goesOnline(join1);
//        //the Player should now be in DB
//        assertTrue(storage.getAll().size() == 1);
//        //Everybody joins | player2 has NO Permissions
//        listener.goesOnline(join1);
//        listener.goesOnline(join2);
//        listener.goesOnline(join3);
//        listener.goesOnline(join4);
//        //1 +2 Player in DB
//        assertTrue(storage.getAll().size() == 3);
//        //delete Member2
//        storage.delete(userManager.getUser(player3));
//        //3 -1 Player in DB
//        assertTrue(storage.getAll().size() == 2);
//        //Clear DB again
//        userManager.clean();
//        //Db should be empty again
//        assertTrue(storage.getAll().isEmpty());
    }

    public PlayerJoinEvent createPlayerJointEvent(Player player)
    {
        return new PlayerJoinEvent(player,"");
    }
    
    public Player createSimplePlayer(String name)
    {
        Player player = mock(Player.class);
        //Give Player his Name
        when(player.getName()).thenReturn(name);
        return player;
    }
    
    public Player createOPPlayer(String name)
    {
        Player player = mock(Player.class);
        //Give Player his Name
        when(player.getName()).thenReturn(name);
        //Give Player all Permissions
        when(player.hasPermission(Matchers.anyString())).thenReturn(true);
        when(player.isOp()).thenReturn(true);
        return player;
    }
    
    public Player createGuestPlayer(String name)
    {
        Player player = mock(Player.class);
        //Give Player his Name
        when(player.getName()).thenReturn(name);
        //Give Player all Permissions
        when(player.hasPermission(Matchers.anyString())).thenReturn(false);
        return player;
    }

}
