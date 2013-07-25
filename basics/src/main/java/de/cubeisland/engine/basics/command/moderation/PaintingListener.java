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
package de.cubeisland.engine.basics.command.moderation;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Art;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsPerm;

public class PaintingListener implements Listener
{
    private final Basics module;
    private Map<String, Painting> paintingChange;
    
    public PaintingListener(Basics module)
    {
        this.module = module;
        this.paintingChange = new HashMap<String, Painting>();
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if(event.getRightClicked().getType() == EntityType.PAINTING)
        {
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());

            if(!BasicsPerm.CHANGEPAINTING.isAuthorized( user ))
            {
                user.sendTranslated("&cYou are not allowed to change the painting.");
                return;
            }
            Painting painting = ( Painting ) event.getRightClicked();
            
            Painting playerPainting = this.paintingChange.get( user.getName() );
            if(playerPainting == null)
            {
                this.paintingChange.put( user.getName(), painting );
                user.sendTranslated("&aYou can now cycle through the paintings using your mousewheel.");
            }
            else
            {
                this.paintingChange.remove( user.getName() );
                user.sendTranslated("&aPainting is locked now");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemHeldChange(PlayerItemHeldEvent event)
    {
        if(!this.paintingChange.isEmpty())
        {
            Painting painting = this.paintingChange.get( event.getPlayer().getName() );
            
            if(painting != null)
            {
                User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
                final int maxDistanceSquared = this.module.getConfiguration().maxChangePaintingDistance * this.module.getConfiguration().maxChangePaintingDistance;
                
                if( painting.getLocation().toVector().distanceSquared( user.getLocation().toVector()) >  maxDistanceSquared)
                {
                    this.paintingChange.remove( user.getName() );
                    user.sendTranslated("&aPainting is locked now");
                    return;
                }
                
                int artNumber = painting.getArt().ordinal();
                do
                {
                    artNumber++;
                    if(artNumber >= Art.values().length )
                    {
                        artNumber = 0;
                    }
                }while(!painting.setArt( Art.values()[artNumber] ));
            }
        }       
    }
}
