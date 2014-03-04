/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.log;

import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.Material;
import org.bukkit.block.BlockState;

import de.cubeisland.engine.core.module.service.Selector;
import de.cubeisland.engine.core.user.UserAttachment;
import de.cubeisland.engine.core.util.math.shape.Cuboid;
import de.cubeisland.engine.core.util.math.shape.Shape;
import de.cubeisland.engine.log.storage.Lookup;
import de.cubeisland.engine.log.storage.QueryParameter;
import de.cubeisland.engine.log.storage.ShowParameter;

public class LogAttachment extends UserAttachment
{
    private Lookup lastLookup; // always contains the last lookup worked on

    private Lookup generalLookup; // lookup with bedrock block
    private Lookup containerLookup; // lookup with chest block
    private Lookup killLookup; // lookup with soulsand block
    private Lookup playerLookup; // lookup with pumpkin block
    private Lookup blockLookup; // lookup with woodlog block
    private Lookup commandLookup; // lookup with command
    private final Queue<ShowParameter> showParameters = new LinkedList<>();
    private ShowParameter lastShowParameter;

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
        this.generalLookup = Lookup.general((Log)this.getModule());
        lastLookup = generalLookup;
        return this.generalLookup;
    }

    public Lookup createNewContainerLookup()
    {
        this.containerLookup = Lookup.container((Log)this.getModule());
        lastLookup = containerLookup;
        return this.containerLookup;
    }

    public Lookup createNewKillsLookup()
    {
        this.killLookup = Lookup.kills((Log)this.getModule());
        lastLookup = killLookup;
        return this.killLookup;
    }

    public Lookup createNewPlayerLookup()
    {
        this.playerLookup = Lookup.player((Log)this.getModule());
        lastLookup = playerLookup;
        return this.playerLookup;
    }

    public Lookup createNewBlockLookup()
    {
        this.playerLookup = Lookup.block((Log)this.getModule());
        lastLookup = playerLookup;
        return this.playerLookup;
    }

    public Lookup createNewCommandLookup()
    {
        this.commandLookup = Lookup.general((Log)this.getModule());
        lastLookup = commandLookup;
        return this.commandLookup;
    }

    public Lookup getCommandLookup()
    {
        if (commandLookup == null)
        {
            return this.createNewCommandLookup();
        }
        this.lastLookup = commandLookup;
        return commandLookup;
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
                return null;
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

    public void setLastLookup(Lookup lastLookup)
    {
        this.lastLookup = lastLookup;
    }

    private Log module;

    public boolean hasSelection()
    {
        Selector selector = this.module.getCore().getModuleManager().getServiceManager().getServiceImplementation(Selector.class);
        Shape selection = selector.getSelection(this.getHolder());
        return selection != null && selection instanceof Cuboid;
    }

    public boolean applySelection(QueryParameter parameter)
    {
        if (hasSelection())
        {
            Selector selector = this.module.getCore().getModuleManager().getServiceManager().getServiceImplementation(Selector.class);
            parameter.setLocationRange(selector.getFirstPoint(this.getHolder()), selector.getSecondPoint(this.getHolder()));
            return true;
        }
        return false;
    }

    @Override
    public void onAttach()
    {
        if (this.getModule() instanceof Log)
        {
            this.module = (Log)this.getModule();
            return;
        }
        throw new IllegalArgumentException("Only Log is allowed as module for LogAttachments!");
    }

    private Preview preview;

    public void addToPreview(BlockState state)
    {
        if (preview == null)
        {
            this.createNewPreview();
        }
        preview.add(state);
    }

    public void createNewPreview()
    {
        this.preview = new Preview();
    }

    public void sendPreview()
    {
        this.preview.send(this.getHolder());
    }

    public ShowParameter getShowParameter()
    {
        this.lastShowParameter = showParameters.poll();
        if (this.lastShowParameter == null)
        {
            return new ShowParameter();
        }
        return this.lastShowParameter;
    }

    public void queueShowParameter(ShowParameter show)
    {
        this.showParameters.add(show);
    }

    public ShowParameter getLastShowParameter()
    {
        if (this.lastShowParameter == null)
        {
            this.lastShowParameter = new ShowParameter();
        }
        return lastShowParameter;
    }

    public Lookup getLastLookup()
    {
        return this.lastLookup;
    }
}

