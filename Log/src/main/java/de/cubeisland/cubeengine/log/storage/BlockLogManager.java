/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.core.storage.database.Database;
import org.bukkit.block.BlockState;

/**
 *
 * @author Anselm Brehme
 */
public class BlockLogManager extends SingleKeyStorage<Long, BlockLogModel>
{
    public BlockLogManager(Database database, int revision)
    {
        super(database, BlockLogModel.class, revision);
    }
    
    
    
    
}
