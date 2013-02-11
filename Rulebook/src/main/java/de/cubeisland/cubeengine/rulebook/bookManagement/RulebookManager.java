package de.cubeisland.cubeengine.rulebook.bookManagement;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.i18n.Language;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.rulebook.Rulebook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static de.cubeisland.cubeengine.core.i18n.I18n._;
import java.util.List;
import org.bukkit.inventory.meta.BookMeta;

public final class RulebookManager
{

    private final Rulebook module;
    private Map<String, String[]> rulebooks;

    public RulebookManager(Rulebook module)
    {
        this.module = module;

        this.rulebooks = new HashMap<String, String[]>();

        for (File book : RuleBookFile.getLanguageFiles(this.module.getFolder()))
        {
            Language language = this.module.getCore().getI18n().searchLanguages(StringUtils.stripFileExtension(book.getName())).iterator().next();
            try
            {
                rulebooks.put(language.getName(), RuleBookFile.convertToPages(book));
            }
            catch (IOException ex)
            {
                this.module.getLogger().log(LogLevel.ERROR, "Can't read the file {0}", book.getName());
            }
        }
    }

    public Collection<String> getLanguages()
    {
        return this.rulebooks.keySet();
    }

    public boolean contains(String language)
    {
        return this.contains(language, 2);
    }

    public boolean contains(String language, int editDistance)
    {
        Set<Language> languages = this.module.getCore().getI18n().searchLanguages(language, editDistance);
        return languages.size() == 1 && this.rulebooks.containsKey(languages.iterator().next().getName());
    }

    public String[] getPages(String language)
    {
        return this.getPages(language, 2);
    }

    public String[] getPages(String language, int editDistance)
    {
        Set<Language> languages = this.module.getCore().getI18n().searchLanguages(language, editDistance);
        if (languages.size() == 1)
        {
            return this.rulebooks.get(languages.iterator().next().getName());
        }
        return null;
    }

    public ItemStack getBook(String language)
    {
        Set<Language> languages = CubeEngine.getI18n().searchLanguages(language);
        if (languages.size() != 1)
        {
            return null;
        }
        language = languages.iterator().next().getName();

        if (this.contains(language))
        {
            ItemStack ruleBook = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = ((BookMeta)ruleBook.getItemMeta());
            meta.setAuthor(Bukkit.getServerName());
            meta.setTitle(_(language, "rulebook", "Rulebook"));
            meta.setPages(this.getPages(language));
            ruleBook.setItemMeta(meta);
            // TODO this does not work anymore (thx Bukkit)
            /*
             NBTTagCompound tag = rulebook.getTag();
             tag.setBoolean("rulebook", true);
             tag.setString("language", language);
            
             */
            return ruleBook;
        }
        return null;
    }

    public boolean removeBook(String language) throws IOException
    {
        Set<Language> languages = this.module.getCore().getI18n().searchLanguages(language);
        boolean value = false;

        if (languages.size() == 1)
        {
            language = languages.iterator().next().getName();

            for (File file : RuleBookFile.getLanguageFiles(this.module.getFolder()))
            {
                Language fileLanguage = this.module.getCore().getI18n().searchLanguages(StringUtils.stripFileExtension(file.getName())).iterator().next();

                if (fileLanguage.getName().equalsIgnoreCase(language))
                {
                    value = file.delete();
                    if (!value)
                    {
                        throw new IOException("Can't delete the file " + file.getName());
                    }
                }
            }
            if (value)
            {
                this.rulebooks.remove(language);
            }
        }
        return value;
    }

    public void addBook(ItemStack book, String language)
    {
        Set<Language> languages = this.module.getCore().getI18n().searchLanguages(language);
        if (!this.contains(language) && languages.size() == 1)
        {
            Language lang = languages.iterator().next();
            try
            {
                File file = new File(this.module.getFolder().getAbsoluteFile(), lang.getName() + ".txt");
                List<String> pages = ((BookMeta)book.getItemMeta()).getPages();
                RuleBookFile.createFile(file, pages.toArray(new String[pages.size()]));

                this.rulebooks.put(language, RuleBookFile.convertToPages(file));
            }
            catch (IOException ex)
            {
                this.module.getLogger().log(LogLevel.ERROR, "Error by creating the book");
            }
        }
    }
}
