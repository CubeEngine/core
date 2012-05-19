package de.cubeisland.cubeengine.war.user;

import de.cubeisland.cubeengine.war.CubeWar;
import de.cubeisland.cubeengine.war.CubeWarConfiguration;
import de.cubeisland.cubeengine.war.groups.Group_old;
import de.cubeisland.cubeengine.war.groups.GroupControl_old;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Anselm
 */
public class InfluenceControl
{
    private static  TimerTask timerTask;
    private static CubeWarConfiguration config = CubeWar.getInstance().getConfiguration();
    
    public static void startTimer()
    {
        Timer timer = new Timer();
        
        timerTask = new TimerTask() 
        {
            @Override
            public void run()
            {
                CubeWar.debug("Influence-Control");
                Collection<User_old> users = CubeWar.getInstance().getUserControl().getUsers();
                Collection<Group_old> groups = GroupControl_old.get().getGroups();
                //ADD / REM Influence
                for (User_old user : users)
                {
                    if (user.getMode().equals(PlayerMode.PEACE))
                    {
                        if (!user.getTeam().isPeaceful())
                            return;//No Points for War-Team when Player is in PEACE-Mode
                    }
                    if (user.getOfflinePlayer().getLastPlayed() > System.currentTimeMillis() - config.afterDaysOffline * 24 * 60 * 60 * 1000)
                    {
                        user.addInfluence(config.influencePerMin);
                        if (user.getOfflinePlayer().isOnline())
                            user.addInfluence(config.influencePerMinOnline);
                    }
                    else
                        user.looseInfluence(config.loosePerMin);
                    CubeWar.getInstance().getUserDB().update(user);
                }
                //ADJUST GROUP influence
                for (Group_old group : groups)
                {
                    if (group.getId()>0)
                        group.adjustMaxInfluence();
                }
            }
        };
        timer.schedule(timerTask, 1000, 60000);
    }
    
    
}
