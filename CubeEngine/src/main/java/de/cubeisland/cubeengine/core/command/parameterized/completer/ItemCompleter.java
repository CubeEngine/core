package de.cubeisland.cubeengine.core.command.parameterized.completer;

import de.cubeisland.cubeengine.core.command.parameterized.ParamCompleter;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ItemCompleter extends ParamCompleter
{
    private final MaterialMatcher matcher;

    public ItemCompleter(MaterialMatcher matcher)
    {
        super(Material.class, ItemStack.class);
        this.matcher = matcher;
    }

    @Override
    public List<String> complete(User sender, String token)
    {
        return Arrays.asList(String.valueOf(this.matcher.material(token).getId()));
    }
}
