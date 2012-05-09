package de.cubeisland.cubeengine.war.user;

/**
 *
 * @author Faithcaio
 */
public enum PlayerMode {
    NORMAL,//kill+1 death+1
    KILLRESET,//kill+1 death->kill=0
    HIGHLANDER,//kill+kills death perm
    PEACE,//No Damage No Kills
    DUEL//Kein Drop mode endet nach Tod
}
