package de.cubeisland.cubeengine.core.test;

import de.cubeisland.cubeengine.core.CoreListener;
import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.persistence.database.Database;
import de.cubeisland.cubeengine.core.test.factory.PlayerFactory;
import de.cubeisland.cubeengine.core.test.factory.TestInstanceFactory;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.user.UserStorage;
import de.cubeisland.cubeengine.core.util.math.*;
import junit.framework.TestCase;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.Test;
import static org.mockito.Mockito.mock;


public class TestTest extends TestCase
{
    @Test
    public void testNothing()
    {
        //This does nothing!
    }
    
    @Test
    public void testMath()
    {
        //2D Vector testing...
        Vector2 v21 = new Vector2(4,-2);
        Vector2 v22 = new Vector2(2,4);
        Vector2 v23 = new Vector2(4,8);
        Vector2 v24 = new Vector2(6,2);
        Vector2 v25 = new Vector2(0,5);
        Vector2 v26 = new Vector2(3,1);
        Vector2 v27 = new Vector2(1.5,2.5);
        assertTrue( v21.x == 4);
        assertTrue( v21.y == -2);
        assertTrue( v21.isOrthogonal(v22));
        assertTrue( !v21.isParallel(v22));
        assertTrue( v23.isParallel(v22));
        assertTrue( v22.dot(v23) == 2*4+4*8);
        assertTrue( v21.add(v22).equals(v24));
        assertTrue( v24.substract(v22).equals(v21));
        assertTrue( v22.multiply(2).equals(v23));
        assertTrue( v23.multiply(0.5).equals(v22));
        assertTrue( v23.divide(2).equals(v22));
        assertTrue( v22.divide(0.5).equals(v23));
        assertTrue( v21.squaredLength() == 20);
        assertTrue( v25.length() == 5);
        assertTrue( v22.distanceVector(v24).equals(v21));
        assertTrue( v22.squaredDistance(v24) == 20);
        assertTrue( v25.distance(v26) == 5);
        assertTrue( v21.crossAngle(v22) == 90);
        assertTrue( v21.normalize().length() == v22.normalize().length());
        assertTrue( v21.midpoint(v22).equals(v26));
        assertTrue( v27.toString().equals("(1.5|2.5)"));
    }
    
    @Test
    public void testCorePlayerJoinEvent()
    {
        TestInstanceFactory factory = new TestInstanceFactory();
        factory.setUp();
        
        //CubeCore core = factory.getCore();
        Server server = mock(Server.class);
        Database database = new Database("localhost",(short)3306,"root","","cubeengine","cube_test_");
        
        UserStorage storage = new UserStorage(database, server);
        //Init & Clear DB
        storage.initialize();
        storage.clear();
        //is Empty?
        assertTrue(storage.getAll().isEmpty());
        
        UserManager cuManager = new UserManager(database,server);
        CoreListener listener = new CoreListener(cuManager);
        
        //Create New Player to join
        Player player1 = PlayerFactory.createOPPlayer("Member1");
        Player player2 = PlayerFactory.createGuestPlayer("GuestN00B");
        Player player3 = PlayerFactory.createOPPlayer("Member2");
        Player player4 = PlayerFactory.createOPPlayer("Member3");
        //Create new PlayerJoinEvents
        PlayerJoinEvent join1 = this.createPlayerJointEvent(player1);
        PlayerJoinEvent join2 = this.createPlayerJointEvent(player2);
        PlayerJoinEvent join3 = this.createPlayerJointEvent(player3);
        PlayerJoinEvent join4 = this.createPlayerJointEvent(player4);
        //Pass join1 to Listender
        listener.goesOnline(join1);
        //the Player should now be in DB
        assertTrue(storage.getAll().size() == 1);
        //Everybody joins | player2 has NO Permissions
        listener.goesOnline(join1);
        listener.goesOnline(join2);
        listener.goesOnline(join3);
        listener.goesOnline(join4);
        //1 +2 Player in DB
        assertTrue(storage.getAll().size() == 3);
        //delete Member2
        storage.delete(cuManager.getUser(player3));
        //3 -1 Player in DB
        assertTrue(storage.getAll().size() == 2);
        //Clear DB again
        cuManager.clean();
        //Db should be empty again
        assertTrue(storage.getAll().isEmpty());
    }

    public PlayerJoinEvent createPlayerJointEvent(Player player)
    {
        return new PlayerJoinEvent(player,"");
    }
}
