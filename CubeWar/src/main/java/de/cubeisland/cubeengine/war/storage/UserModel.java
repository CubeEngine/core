package de.cubeisland.cubeengine.war.storage;


import de.cubeisland.cubeengine.core.persistence.Model;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.war.CubeWar;
import de.cubeisland.cubeengine.war.CubeWarConfiguration;
import de.cubeisland.cubeengine.war.groups.Group;
import de.cubeisland.cubeengine.war.groups.GroupControl;
import de.cubeisland.cubeengine.war.user.PlayerMode;
import de.cubeisland.cubeengine.war.user.Rank;
import java.util.HashSet;

/**
 *
 * @author Anselm Brehme
 */
public class UserModel implements Model<User>
{
    private CubeWarConfiguration config = CubeWar.getInstance().getConfiguration();
    private GroupControl groups = GroupControl.get();
    private User cubeUser;
    private int death = 0;
    private int kills = 0;
    private int killpoints = 0;
    private double influence = 0;
    private PlayerMode mode = PlayerMode.NORMAL;
    private Group team = groups.getWildLand();
    //No DB saving, resets when joining anyway
    private Rank rank;
    private boolean respawning;
    private HashSet<String> bypasses = new HashSet<String>();
    private TeamPos teampos;

    public User getKey()
    {
        return this.getCubeUser();
    }

    public enum TeamPos
    {
        ADMIN,
        MODERATOR,
        MEMBER,
        NONE
    }

    /**
     * Loads in a UserModel
     *
     * @param cubeUser
     * @param death
     * @param kills
     * @param killpoints
     * @param influence
     * @param mode
     * @param team
     */
    public UserModel(User cubeUser, int death, int kills, int killpoints, double influence, PlayerMode mode, Group team, TeamPos teampos)
    {
        this.cubeUser = cubeUser;
        this.death = death;
        this.kills = kills;
        this.killpoints = killpoints;
        this.influence = influence;
        this.mode = mode;
        this.team = team;
        this.teampos = teampos;

        this.rank = Rank.newRank(this.killpoints);
        this.respawning = false;
    }

    /**
     * Creates a new UserModel
     *
     * @param cubeUser
     */
    public UserModel(User cubeUser)
    {
        this.cubeUser = cubeUser;
        this.death = 0;
        this.kills = 0;
        this.killpoints = 0;
        this.influence = 0;
        this.mode = PlayerMode.NORMAL;
        this.team = groups.getWildLand();
        this.teampos = TeamPos.NONE;
        this.rank = Rank.newRank(0);
        this.respawning = false;
    }

    /**
     * @return the config
     */
    public CubeWarConfiguration getConfig()
    {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(CubeWarConfiguration config)
    {
        this.config = config;
    }

    /**
     * @return the groups
     */
    public GroupControl getGroups()
    {
        return groups;
    }

    /**
     * @param groups the groups to set
     */
    public void setGroups(GroupControl groups)
    {
        this.groups = groups;
    }

    /**
     * @return the cubeUser
     */
    public User getCubeUser()
    {
        return cubeUser;
    }

    /**
     * @param cubeUser the cubeUser to set
     */
    public void setCubeUser(User cubeUser)
    {
        this.cubeUser = cubeUser;
    }

    /**
     * @return the death
     */
    public int getDeath()
    {
        return death;
    }

    /**
     * @param death the death to set
     */
    public void setDeath(int death)
    {
        this.death = death;
    }

    /**
     * @param death the death to add
     */
    public void addDeath(int death)
    {
        this.death += death;
    }

    /**
     * @return the kills
     */
    public int getKills()
    {
        return kills;
    }

    /**
     * @param kills the kills to set
     */
    public void setKills(int kills)
    {
        this.kills = kills;
    }

    /**
     * @param kills the kills to add
     */
    public void addKills(int kills)
    {
        this.kills += kills;
    }

    /**
     * @return the killpoints
     */
    public int getKillpoints()
    {
        return killpoints;
    }

    /**
     * @param killpoints the killpoints to set
     */
    public void setKillpoints(int killpoints)
    {
        this.killpoints = killpoints;
    }

    /**
     * @param killpoints the killpoints to add
     */
    public void addKillpoints(int killpoints)
    {
        this.killpoints += killpoints;
    }

    /**
     * @return the influence
     */
    public double getInfluence()
    {
        return influence;
    }

    /**
     * @param influence the influence to set
     */
    public void setInfluence(double influence)
    {
        this.influence = influence;
    }

    /**
     * @param influence the influence to add
     */
    public void addInfluence(double influence)
    {
        this.influence += influence;
    }

    /**
     * @return the mode
     */
    public PlayerMode getMode()
    {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(PlayerMode mode)
    {
        this.mode = mode;
    }

    /**
     * @return the team
     */
    public Group getTeam()
    {
        return team;
    }

    /**
     * @param team the team to set
     */
    public void setTeam(Group team)
    {
        this.team = team;
    }

    /**
     * @return the rank
     */
    public Rank getRank()
    {
        return rank;
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(Rank rank)
    {
        this.rank = rank;
    }

    /**
     * @return the respawning
     */
    public boolean isRespawning()
    {
        return respawning;
    }

    /**
     * @param respawning the respawning to set
     */
    public void setRespawning(boolean respawning)
    {
        this.respawning = respawning;
    }

    /**
     * @return the bypasses
     */
    public HashSet<String> getBypasses()
    {
        return bypasses;
    }

    /**
     * @param bypasses the bypasses to set
     */
    public void setBypasses(HashSet<String> bypasses)
    {
        this.bypasses = bypasses;
    }

    /**
     * @param bypasses the bypass to add
     */
    public void addBypass(String bypass)
    {
        this.bypasses.add(bypass);
    }

    /**
     * @param bypasses the bypass to add
     */
    public boolean removeBypass(String bypass)
    {
        return this.bypasses.remove(bypass);
    }

    /**
     * Resets the bypasses
     */
    public void resetBypasses()
    {
        this.bypasses.clear();
    }

    /**
     * @return the teampos
     */
    public TeamPos getTeampos()
    {
        return teampos;
    }

    /**
     * @param teampos the teampos to set
     */
    public void setTeampos(TeamPos teampos)
    {
        this.teampos = teampos;
    }
    
    
    public void setKey(User key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
