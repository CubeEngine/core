/*
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
package org.cubeengine.libcube.service.matcher;

import java.util.HashMap;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.DyeColor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MaterialDataMatcher
{
    @Inject private StringMatcher stringMatcher;
    private HashMap<String, DyeColor> dyeColors;

    public void onEnable()
    {
        initDataValues();
    }

    private void initDataValues()
    {
        this.dyeColors = new HashMap<>();
        for (DyeColor dyeColor : Sponge.getRegistry().getAllOf(DyeColor.class))
        {
            dyeColors.put(dyeColor.getName(), dyeColor);
        }
    }

    /**
     * Matches a DyeColor
     *
     * @param data the data
     * @return the dye color
     */
    public DyeColor colorData(String data)
    {
        String match = stringMatcher.matchString(data, dyeColors.keySet());
        if (match == null)
        {
            return null;
        }
        return dyeColors.get(match);
    }
}
