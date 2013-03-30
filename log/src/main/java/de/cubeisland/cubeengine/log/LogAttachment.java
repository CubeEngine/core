package de.cubeisland.cubeengine.log;

import org.bukkit.Material;

import de.cubeisland.cubeengine.core.user.UserAttachment;
import de.cubeisland.cubeengine.log.storage.Lookup;

public class LogAttachment extends UserAttachment
{
    private Lookup lastLookup; // always contains the last lookup worked on

    private Lookup generalLookup; // lookup with bedrock block
    private Lookup containerLookup; // lookup with chest block
    private Lookup killLookup; // lookup with soulsand block
    private Lookup playerLookup; // lookup with pumpkin block
    private Lookup blockLookup; // lookup with woodlog block
    private Lookup commandLookup; // lookup with command //TODO

    public void clearLookups()
    {
        lastLookup = null;
        generalLookup = null;
        containerLookup = null;
        killLookup = null;
        playerLookup = null;
        blockLookup = null;
        commandLookup = null;
    }

    public Lookup createNewGeneralLookup()
    {
        this.generalLookup = Lookup.general(this.getModule());
        lastLookup = generalLookup;
        return this.generalLookup;
    }

    public Lookup createNewContainerLookup()
    {
        this.containerLookup = Lookup.container(this.getModule());
        lastLookup = containerLookup;
        return this.containerLookup;
    }

    public Lookup createNewKillsLookup()
    {
        this.killLookup = Lookup.kills(this.getModule());
        lastLookup = killLookup;
        return this.killLookup;
    }

    public Lookup createNewPlayerLookup()
    {
        this.playerLookup = Lookup.player(this.getModule());
        lastLookup = playerLookup;
        return this.playerLookup;
    }

    public Lookup createNewBlockLookup()
    {
        this.playerLookup = Lookup.block(this.getModule());
        lastLookup = playerLookup;
        return this.playerLookup;
    }

    public Lookup createNewLookup(Material blockMaterial)
    {
        switch (blockMaterial)
        {
            case BEDROCK:
                return this.createNewGeneralLookup();
            case CHEST:
                return this.createNewContainerLookup();
            case PUMPKIN:
                return this.createNewPlayerLookup();
            case SOUL_SAND:
                return this.createNewKillsLookup();
            case LOG:
                return this.createNewBlockLookup();
            default:
                return null; //TODO command lookup
        }
    }

    public Lookup getLookup(Material blockMaterial)
    {
        Lookup lookup;
        switch (blockMaterial)
        {
            case BEDROCK:
                lookup = generalLookup;
                break;
            case CHEST:
                lookup = containerLookup;
                break;
            case PUMPKIN:
                lookup = playerLookup;
                break;
            case SOUL_SAND:
                lookup = killLookup;
                break;
            case LOG:
                lookup = blockLookup;
                break;
            default:
                return null;
        }
        if (lookup == null)
        {
            return this.createNewLookup(blockMaterial);
        }
        return lookup;
    }
}

