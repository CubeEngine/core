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
package de.cubeisland.cubeengine.creeperball;

import java.util.HashMap;

import org.bukkit.block.Block;

import de.cubeisland.cubeengine.core.user.UserAttachment;
import de.cubeisland.cubeengine.core.util.clipboard.CuboidBlockClipboard;

public class CreeperBallAttachment extends UserAttachment
{
    // TODO: Ideas
    // instaKill when creeper does damage
    // define points for explosion etc.


    // Absolutely needed to define the arena:
    // Arena Area defined by those:
    private Block block1;
    private Block block2;
    // TODO disallow the spawned creepers to follow other players (in the region)
    // Middle line
    private Block blockMiddle1;
    private Block blockMiddle2;
    // TODO creepers will be spawned on the middle of that line OR on it
    // PlayerSpawns
    private Block player1Spawn;
    private Block player2Spawn;
    // When fixed locations can be declared to power redstone
    private boolean fixedMaxPoints;
    // Optional informations
    private Block[] pointBlocks; // TODO Locations where to power redstone for points

    public void handleBlockClick(Block block, boolean isLeft)
    {
    }
}
