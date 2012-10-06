package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.log.listeners.BlockBreakLogger;
import de.cubeisland.cubeengine.log.listeners.BlockBurnListener;
import de.cubeisland.cubeengine.log.listeners.BlockFadeListener;
import de.cubeisland.cubeengine.log.listeners.BlockFormListener;
import de.cubeisland.cubeengine.log.listeners.BlockPlaceListener;
import de.cubeisland.cubeengine.log.listeners.ChatListener;
import de.cubeisland.cubeengine.log.listeners.ContainerAccessListener;
import de.cubeisland.cubeengine.log.listeners.EndermanListener;
import de.cubeisland.cubeengine.log.listeners.ExplosionListener;
import de.cubeisland.cubeengine.log.listeners.FluidFlowListener;
import de.cubeisland.cubeengine.log.listeners.KillListener;
import de.cubeisland.cubeengine.log.listeners.LeavesDecayListener;
import de.cubeisland.cubeengine.log.listeners.LogListener;
import de.cubeisland.cubeengine.log.listeners.PlayerInteractListener;
import de.cubeisland.cubeengine.log.listeners.SignChangeListener;
import de.cubeisland.cubeengine.log.listeners.StructureGrowListener;

public enum LogAction
{
    BLOCKPLACE(BlockPlaceListener.class),
    PLAYER_BLOCKBREAK(BlockBreakLogger.class),
    SIGNTEXT(SignChangeListener.class),
    EXPLOSION_TNT(ExplosionListener.class),
    EXPLOSION_CREEPER(ExplosionListener.class),
    EXPLOSION_GHASTFIREBALL(ExplosionListener.class),
    EXPLOSION_ENDERDRAGON(ExplosionListener.class),
    EXPLOSION_MISC(ExplosionListener.class),
    FIRE(BlockBurnListener.class),
    ENDERMEN(EndermanListener.class),
    LEAVESDECAY(LeavesDecayListener.class),
    LAVAFLOW(FluidFlowListener.class),
    WATERFLOW(FluidFlowListener.class),
    DISPENSERACCESS(ContainerAccessListener.class),
    CHESTACCESS(ContainerAccessListener.class),
    FURNACEACCESS(ContainerAccessListener.class),
    BREWINGSTANDACCESS(ContainerAccessListener.class),
    KILL(KillListener.class),
    CHAT(ChatListener.class),
    COMMAND(ChatListener.class),
    CONSOLE(ChatListener.class),
    SNOWFORM(BlockFormListener.class),
    SNOWFADE(BlockFadeListener.class),
    ICEFORM(BlockFormListener.class),
    ICEFADE(BlockFadeListener.class),
    DOORINTERACT(PlayerInteractListener.class),
    SWITCHINTERACT(PlayerInteractListener.class),
    CAKEEAT(PlayerInteractListener.class),
    NOTEBLOCKINTERACT(PlayerInteractListener.class),
    DIODEINTERACT(PlayerInteractListener.class),
    NATURALSTRUCTUREGROW(StructureGrowListener.class),
    BONEMEALSTRUCTUREGROW(StructureGrowListener.class),;
    private final Class<? extends LogListener> listenerClass;

    private LogAction(Class<? extends LogListener> listenerClass)
    {
        this.listenerClass = listenerClass;
    }

    public Class<? extends LogListener> getListenerClass()
    {
        return listenerClass;
    }

}