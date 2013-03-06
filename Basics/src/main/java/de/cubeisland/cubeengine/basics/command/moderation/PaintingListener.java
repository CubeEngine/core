package de.cubeisland.cubeengine.basics.command.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.math.Vector3;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

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
            User user = this.module.getUserManager().getExactUser( event.getPlayer());

            if(!BasicsPerm.CHANGEPAINTING.isAuthorized( user ))
            {
                user.sendMessage( "basics", "&cYou are not allowed to change the painting." );
                return;
            }
            Painting painting = ( Painting ) event.getRightClicked();
            
            Painting playerPainting = this.paintingChange.get( user.getName() );
            if(playerPainting == null)
            {
                this.paintingChange.put( user.getName(), painting );
                user.sendMessage( "basics", "&aYou can now cycle through the paintings using your mousewheel." );
            }
            else
            {
                this.paintingChange.remove( user.getName() );
                user.sendMessage( "basics", "&aPainting is locked now" );
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
                User user = this.module.getUserManager().getExactUser( event.getPlayer());
                
                if( this.squaredDistance( painting, user ) > this.module.getConfiguration().maxChangePaintingDistance * this.module.getConfiguration().maxChangePaintingDistance )
                {
                    this.paintingChange.remove( user.getName() );
                    user.sendMessage( "basics", "&aPainting is locked now" );
                    return;
                }
                while(!painting.setArt( Art.values()[ (int) (Math.random() * Art.values().length)] ));
            }
        }       
    }
    
    public double squaredDistance(Entity entity, Entity entity2)
    {
        Location loc = entity.getLocation();
        Location loc2 = entity2.getLocation();
        
        return Math.pow( loc2.getX() - loc.getX(), 2 ) + Math.pow( loc2.getY() - loc.getY() , 2 ) + Math.pow( loc2.getZ() - loc.getZ(), 2);
    }
}
