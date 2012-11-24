package de.cubeisland.cubeengine.rules.bookManagement;

import de.cubeisland.cubeengine.core.bukkit.BookItem;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.rules.Rules;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class RuleBookManager 
{
    private final Rules module;
    
    private Map<String, String[]> rulebooks;
    
    public RuleBookManager(Rules module)
    {
        this.module = module;
        
        this.rulebooks = new HashMap<String, String[]>();
        
        for(String language : this.module.getConfig().languages)
        {
            File file = RuleBookFile.loadFile(module.getFolder().getPath(), language + ".txt");
            try 
            {
                rulebooks.put(language, RuleBookFile.convertToPages(file));
            } 
            catch (IOException ex) 
            {
                this.module.getLogger().log(LogLevel.ERROR, "Can''t read the file {0}", file.getName());
            }
        }
    }
    
    public Collection<String> getLanguages()
    {
        return this.rulebooks.keySet();
    }
    
    public boolean contains(String language)
    {
        return this.rulebooks.containsKey(language);
    }
    
    public String[] getPages(String language)
    {
        return this.rulebooks.get(language);
    }
    
    public ItemStack getBook(String language)
    {
        if(this.contains(language))
        {
            BookItem ruleBook = new BookItem(new ItemStack(Material.WRITTEN_BOOK));

            ruleBook.setAuthor(Bukkit.getServerName());
            ruleBook.setTitle(_(language, "rulebook", "Rulebook"));
            ruleBook.setPages(this.getPages(language));

            return ruleBook.getItemStack();
        }
        return null;
    }
}
