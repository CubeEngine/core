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
package de.cubeisland.engine.powersigns;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.powersigns.signtype.LiftSign;
import de.cubeisland.engine.powersigns.signtype.SignType;
import de.cubeisland.engine.powersigns.signtype.SignTypeInfo;
import de.cubeisland.engine.powersigns.storage.PowerSignModel;
import gnu.trove.map.hash.THashMap;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.powersigns.storage.TablePowerSign.TABLE_POWER_SIGN;

public class SignManager implements Listener
{
    private Map<String,SignType> registerdSignTypes = new THashMap<>();

    private Map<Location,PowerSign> loadedPowerSigns = new THashMap<>();
    protected Powersigns module;

    public final DSLContext dsl;

    public SignManager(Powersigns module)
    {
        this.module = module;
        this.dsl = module.getCore().getDB().getDSL();
    }

    public void init()
    {
        this.module.getCore().getEventManager().registerListener(this.module,this);
        this.registerSignType(new LiftSign());
        Collection<PowerSignModel> powerSignModels = new HashSet<>();
        for (World world : this.module.getCore().getWorldManager().getWorlds())
        {
            for (Chunk chunk : world.getLoadedChunks())
            {
                powerSignModels.addAll(this.dsl.selectFrom(TABLE_POWER_SIGN).
                    where(TABLE_POWER_SIGN.CHUNKX.eq(chunk.getX()), TABLE_POWER_SIGN.CHUNKX.eq(chunk.getZ())).fetch());
            }
        }
        for (PowerSignModel powerSignModel : powerSignModels)
        {
            SignType signType = this.registerdSignTypes.get(powerSignModel.getPSID());
            SignTypeInfo info = signType.createInfo(powerSignModel);
            if (info == null)
            {
                continue;
            }
            PowerSign powerSign = new PowerSign(signType,info);
            this.loadedPowerSigns.put(powerSign.getLocation(),powerSign);
        }
    }

    public SignManager registerSignType(SignType<?,?> signType)
    {
        if (registerdSignTypes.put(signType.getPSID(),signType) != null)
        {
            throw new IllegalStateException("Already registered String!" + signType.getPSID());
        }
        this.module.getLog().debug("Registered SignType: {}", signType.getPSID());
        for (String name : signType.getNames().keySet())
        {
            if (registerdSignTypes.put(name,signType) != null)
            {
                throw new IllegalStateException("Already registered String! "+ name);
            }
        }
        signType.init(this.module);
        return this;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event)
    {
        long worldID = this.module.getCore().getWorldManager().getWorldId(event.getChunk().getWorld());
        Collection<PowerSignModel> powerSignModels = this.dsl.selectFrom(TABLE_POWER_SIGN).
            where(TABLE_POWER_SIGN.CHUNKX.eq(event.getChunk().getX()),
                  TABLE_POWER_SIGN.CHUNKX.eq(event.getChunk().getZ()),
                  TABLE_POWER_SIGN.WORLD.eq(UInteger.valueOf(worldID))).fetch();
        for (PowerSignModel powerSignModel : powerSignModels)
        {
            SignType signType = this.registerdSignTypes.get(powerSignModel.getPSID());
            SignTypeInfo info = signType.createInfo(powerSignModel);
            PowerSign<?, ?> powerSign = new PowerSign(signType,info);
            this.loadedPowerSigns.put(powerSign.getLocation(),powerSign);
        }
    }

    public void onChunkUnload(ChunkLoadEvent event)
    {
        //TODO
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event)
    {
        if (event.getLine(1).startsWith("[") && event.getLine(1).endsWith("]"))
        {
            String idLine = event.getLine(1);
            idLine = idLine.substring(1,idLine.length()-1);
            this.module.getLog().debug("IdentifierLine: {}" + idLine);
            idLine = idLine.toLowerCase();
            SignType signType = registerdSignTypes.get(idLine);
            if (signType == null)
            {
                return; //not valid -> ignore
            }
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            PowerSign powerSign = new PowerSign(signType,event.getBlock().getLocation(),user,event.getLines());
            this.loadedPowerSigns.put(powerSign.getLocation(),powerSign);
            powerSign.updateSignText();
            powerSign.getSignTypeInfo().saveData();
            event.setCancelled(true);
        }
        //TODO detect new signs
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event)
    {
        if (event.getClickedBlock() == null
            || event.getClickedBlock().getType().equals(Material.AIR)
            || !(event.getClickedBlock().getType().equals(Material.WALL_SIGN)
            || event.getClickedBlock().getType() == Material.SIGN_POST))
        {
            return;
        }
        Location location = event.getClickedBlock().getLocation();
        PowerSign powerSign = this.loadedPowerSigns.get(location);
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        if (powerSign == null)
        {
            String psid = getPSID(location);
            if (psid == null)
            {
               // event.getPlayer().sendMessage("[PowerSigns] No Sign here!");
                //TODO check if it could be a PowerSign
                //TODO create the sign if user has permission
                return;
            }
            //TODO load in sign from nbt!
            return;
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            if (event.getPlayer().isSneaking())
            {
                event.setCancelled(powerSign.getSignType().onSignShiftRightClick(user,powerSign));
            }
            else
            {
                event.setCancelled(powerSign.getSignType().onSignRightClick(user, powerSign));
            }
        }
        else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        {
            if (event.getPlayer().isSneaking())
            {
                event.setCancelled(powerSign.getSignType().onSignShiftLeftClick(user, powerSign));
            }
            else
            {
                event.setCancelled(powerSign.getSignType().onSignLeftClick(user, powerSign));
            }
        }
    }

    public String getPSID(Location location)
    {
        PowerSign powerSign = this.loadedPowerSigns.get(location);
        if (powerSign == null) return null;
        return powerSign.getSignType().getPSID();
    }

    public PowerSign getPowerSign(Location location)
    {
        return this.loadedPowerSigns.get(location);
    }

}
