package de.cubeisland.cubeengine.war;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class Util
{
    public Util()
    {
    }

    public static List<Material> convertListStringToMaterial(List<String> stringlist)
    {
        List<Material> temp = new ArrayList<Material>();
        while (stringlist.size() > 0)
        {
            temp.add(Material.matchMaterial(stringlist.get(0)));
            stringlist.remove(0);
        }
        return temp;
    }
}
