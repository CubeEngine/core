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
package de.cubeisland.engine.powersigns.signtype;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.powersigns.PowerSign;
import de.cubeisland.engine.powersigns.Powersigns;
import de.cubeisland.engine.powersigns.signtype.LiftSign.LiftSignInfo;
import de.cubeisland.engine.powersigns.storage.PowerSignModel;

public class LiftSign extends SignType<LiftSign,LiftSignInfo>
{
    public LiftSign()
    {
        super("Lift","Lift Up","Lift Down");
    }

    @Override
    public String getPSID()
    {
        return "PS0001";
    }

    @Override
    public boolean onSignLeftClick(User user, PowerSign<LiftSign,LiftSignInfo> sign)
    {
        return false;
    }

    @Override
    public boolean onSignRightClick(User user, PowerSign<LiftSign,LiftSignInfo> sign)
    {
        LiftSignInfo signTypeInfo = sign.getSignTypeInfo();
        if (signTypeInfo.up == null)
        {
            user.sendTranslated(MessageType.NEGATIVE, "You can not depart from this sign but only arrive!");
            return true;
        }
        Location targetLocation = signTypeInfo.findLiftSign(signTypeInfo.destFloor);
        if (targetLocation == null)
        {
            user.sendTranslated(MessageType.NEGATIVE, "Could not find any other sign to lift to! Perhaps it got destroyed?");
            return true;
        }
        int y = targetLocation.getBlockY();
        user.getLocation(targetLocation);
        targetLocation.setY(y);
        //TODO check for obstruction/floor
        user.teleport(targetLocation, TeleportCause.PLUGIN);
        return true;
    }

    @Override
    public boolean onSignShiftRightClick(User user, PowerSign<LiftSign,LiftSignInfo> sign)
    {
        //TODO showSignInfo
        return true;
    }

    @Override
    public boolean onSignShiftLeftClick(User user, PowerSign<LiftSign,LiftSignInfo> sign)
    {
        LiftSignInfo signTypeInfo = sign.getSignTypeInfo();
        if (signTypeInfo.up == null)
        {
            user.sendTranslated(MessageType.NEGATIVE, "You can not depart from this sign but only arrive!");
            return true;
        }
        if (signTypeInfo.findNextLiftSign() == null)
        {
            if (signTypeInfo.destFloor == 1)
            {
                user.sendTranslated(MessageType.NEGATIVE, "There are no more LiftSigns here!");
                return true;
            }
            if (signTypeInfo.findFirstLiftSign() == null)
            {
                user.sendTranslated(MessageType.NEGATIVE, "Could not find any other LiftSign!");
                return true;
            }
        }
        LiftSignInfo attached = signTypeInfo.getAttachedLiftSign();
        if (signTypeInfo.up)
        {
            user.sendTranslated(MessageType.POSITIVE, "Changed destination to {input} floors up! Floorname: {input}", signTypeInfo.destFloor, attached.floorName);
        }
        else
        {
            user.sendTranslated(MessageType.POSITIVE, "Changed destination to {amount} floors down! Floorname: {input}", signTypeInfo.destFloor, attached.floorName);
        }
        return true;
    }

    @Override
    public LiftSignInfo createInfo(long owner, Location location, String line1, String line2, String line3, String line4)
    {
        int amount = 1;
        try
        {
            amount = Integer.valueOf(line4);
        }
        catch (NumberFormatException ignore){}
        Boolean up;
        line2 = ChatFormat.stripFormats(line2).toLowerCase();
        if (line2.equals("[" + this.getPSID()+ "]") || line2.equals("[lift]"))
        {
            up = null;
        }
        else if (line2.equals("[lift down]"))
        {
            up = false;
        }
        else if (line2.equals("[lift up]"))
        {
            up = true;
        }
        else
        {
            throw new IllegalArgumentException();
        }
        return new LiftSignInfo(this.module,location,owner,line1,up,amount);
    }

    @Override
    public LiftSignInfo createInfo(PowerSignModel model)
    {
        Location location = new Location(this.module.getCore().getWorldManager().getWorld(model.getWorldId().longValue()),model.getX(),model.getY(),model.getZ());
        BlockState state = location.getBlock().getState();
        if (state instanceof Sign)
        {
            Sign sign = (Sign)(location.getBlock().getState());
            return this.createInfo(model.getOwnerId().longValue(),location,sign.getLine(0),sign.getLine(1),sign.getLine(2),sign.getLine(3));
        }
        this.module.getLog().warn("Expected a sign which was not found, at {}:{}:{} in {} ", state.getX(), state.getY(),
                                  state.getZ(), state.getWorld().getName());
        return null;
    }

    public class LiftSignInfo extends SignTypeInfo<LiftSign>
    {
        private String floorName; // 1st line
        private Boolean up; // 2nd Line | Lift | Lift Up | Lift Down
        // 3rd line is destination floor
        private int destFloor; // 4th Line

        private Location destination;


        public LiftSignInfo(Powersigns module, Location location, long creator, String floorName, Boolean up, int amount)
        {
            super(module,location,LiftSign.this,creator);
            this.floorName = floorName;
            this.up = up;
            this.destFloor = amount;
        }

        public Location findLiftSign(int floors)
        {
            if (this.destination != null) // saved destination ?
            {
                Block block = destination.getBlock();
                if (block != null && (block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN))) //found a sign
                {
                    String psid = this.manager.getPSID(destination);
                    if (psid != null && psid.equals(this.signType.getPSID())) // is LiftSign
                    {
                        this.module.getLog().debug("Valid saved dest-loc");
                        return destination.clone(); // return valid
                    }
                }
                else // invalid
                {
                    this.module.getLog().debug("Invalid saved dest-loc");
                    this.destination = null;
                    return this.findLiftSign(floors);
                }
            }
            if (this.up == null) return null;
            Location searchLocation = this.getLocation().clone();
            while (searchLocation.getBlockY() <= searchLocation.getWorld().getMaxHeight() && searchLocation.getBlockY() >= 0)
            {
                searchLocation.add(0,this.up ? 1 : -1,0);
                Block block = searchLocation.getBlock();
                if (block != null && (block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN))) //found a sign
                {
                    String psid = this.manager.getPSID(searchLocation);
                    if (psid != null && psid.equals(this.signType.getPSID())) // is LiftSign
                    {
                        floors--;
                        if (floors == 0) // reached actual floor
                        {
                            this.module.getLog().debug("Valid found dest-loc");
                            this.destination = searchLocation.clone();
                            PowerSign<LiftSign,LiftSignInfo> powerSign = this.manager.getPowerSign(destination);
                            powerSign.getSignTypeInfo().updateSignText();
                            this.updateSignText();
                            return searchLocation;
                        }
                    }
                }
            }
            this.module.getLog().debug("No found dest-loc");
            return null;
        }

        public Location findNextLiftSign()
        {
            this.destination = null;
            Location loc = this.findLiftSign(this.destFloor + 1);
            if (loc != null)
            {
                this.destFloor++;
            }
            this.updateSignText();
            return loc;
        }

        @Override
        public void updateSignText()
        {
            Sign sign = this.getSign();
            sign.setLine(0,this.floorName);
            if (this.up == null)
            {
                sign.setLine(1, ChatFormat.DARK_BLUE + "[Lift]");
            }
            else if (this.up)
            {
                sign.setLine(1, ChatFormat.DARK_BLUE + "[Lift Up]");
            }
            else
            {
                sign.setLine(1, ChatFormat.DARK_BLUE + "[Lift Down]");
            }
            Location connectedLift = this.findLiftSign(this.destFloor);
            if (connectedLift == null)
            {
                sign.setLine(2, ChatFormat.DARK_RED + "No Floor");
            }
            else
            {
                sign.setLine(2,this.getAttachedLiftSign().floorName);
            }
            sign.setLine(3,String.valueOf(this.destFloor));
            sign.update(true);
        }

        public Location findFirstLiftSign()
        {
            this.destination = null;
            Location loc = this.findLiftSign(1);
            if (loc != null)
            {
                this.destFloor = 1;
            }
            this.updateSignText();
            return loc;
        }

        public LiftSignInfo getAttachedLiftSign()
        {
            Location location = this.findLiftSign(this.destFloor);
            if (location == null) return null;
            return (LiftSignInfo)this.manager.getPowerSign(location).getSignTypeInfo();
        }

        @Override
        public String serializeData()
        {
            return null;
        }
    }
}
