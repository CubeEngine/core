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
package de.cubeisland.engine.log.action.action_OLD;

public abstract class BlockActionType
{
    /*


    public String serializeData(EntityDamageEvent.DamageCause cause, Entity entity, DyeColor newColor)
    {// TODO from simpleLogActionType
        ObjectNode json = this.om.createObjectNode();
        if (cause != null)
        {
            json.put("dmgC", cause.name());
        }
        if (entity instanceof Player)
        {
            if (cause == null)
            {
                return null;
            }
            return json.toString(); // only save cause
        }
        if (entity instanceof Ageable)
        {
            json.put("isAdult", ((Ageable)entity).isAdult() ? 1 : 0);
        }
        if (entity instanceof Ocelot)
        {
            json.put("isSit", ((Ocelot)entity).isSitting() ? 1 : 0);
        }
        if (entity instanceof Wolf)
        {
            json.put("isSit", ((Wolf)entity).isSitting() ? 1 : 0);
            json.put("color", ((Wolf)entity).getCollarColor().name());
        }
        if (entity instanceof Sheep)
        {
            json.put("color", ((Sheep)entity).getColor().name());
        }
        if (entity instanceof Villager)
        {
            json.put("prof", ((Villager)entity).getProfession().name());
        }
        if (entity instanceof Tameable && ((Tameable)entity).isTamed())
        {
            if (((Tameable)entity).getOwner() != null)
            {
                json.put("owner", ((Tameable)entity).getOwner().getName());
            }
        }
        if (newColor != null)
        {
            json.put("nColor", newColor.name());
        }
        json.put("UUID", entity.getUniqueId().toString()); // TODO this makes rollback for dying etc possible
        return json.toString();
    }
     */
}
