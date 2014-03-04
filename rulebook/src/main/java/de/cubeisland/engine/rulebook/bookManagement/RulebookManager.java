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
package de.cubeisland.engine.rulebook.bookManagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import de.cubeisland.engine.core.i18n.Language;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.rulebook.Rulebook;

public final class RulebookManager
{
    private final Rulebook module;
    private final Map<Locale, String[]> rulebooks;

    public RulebookManager(Rulebook module)
    {
        this.module = module;

        this.rulebooks = new HashMap<>();

        for(Path book : RuleBookFile.getLanguageFiles(this.module.getFolder()))
        {
            Language language = this.getLanguage(StringUtils.stripFileExtension(book.getFileName().toString()));
            try
            {
                rulebooks.put(language.getLocale(), RuleBookFile.convertToPages(book));
            }
            catch(IOException ex)
            {
                this.module.getLog().error("Can't read the file {}", book.getFileName());
            }
        }
    }

    public Language getLanguage(String name)
    {
        return this.getLanguage(name, 2);
    }

    public Language getLanguage(String name, int difference)
    {
        Set<Language> languages = this.module.getCore().getI18n().searchLanguages(name, difference);
        if(languages.size() == 1)
        {
            return languages.iterator().next();
        }
        return null;
    }

    public Collection<Locale> getLocales()
    {
        return this.rulebooks.keySet();
    }

    public boolean contains(String languageName)
    {
        return this.contains(languageName, 2);
    }

    public boolean contains(String languageName, int editDistance)
    {
        Language language = this.getLanguage(languageName, editDistance);
        return language != null && this.contains(language.getLocale());
    }

    public boolean contains(Locale locale)
    {
        return this.rulebooks.containsKey(locale);
    }

    public String[] getPages(Locale locale)
    {
        return this.rulebooks.get(locale);
    }

    public ItemStack getBook(Locale locale)
    {
        if(this.contains(locale))
        {
            ItemStack ruleBook = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = ((BookMeta) ruleBook.getItemMeta());
            meta.setAuthor(Bukkit.getServerName());
            meta.setTitle(this.module.getCore().getI18n().translate(locale, "rulebook", "Rulebook"));
            meta.setPages(this.getPages(locale));

            List<String> lore = new ArrayList<>();
            lore.add(locale.getLanguage());

            meta.setLore(lore);
            ruleBook.setItemMeta(meta);

            return ruleBook;
        }
        return null;
    }

    public void removeBook(Locale locale) throws IOException
    {
        for(Path file : RuleBookFile.getLanguageFiles(this.module.getFolder()))
        {
            Locale fileLocale = this.getLanguage(StringUtils.stripFileExtension(file.getFileName().toString())).getLocale();
            if(fileLocale.equals(locale))
            {
                Files.delete(file);
            }
        }

        this.rulebooks.remove(locale);
    }

    public void addBook(ItemStack book, Locale locale)
    {
        if(!this.contains(locale))
        {
            try
            {
                Path file = this.module.getFolder().resolve(locale.getDisplayLanguage() + ".txt");
                List<String> pages = ((BookMeta) book.getItemMeta()).getPages();
                RuleBookFile.createFile(file, pages.toArray(new String[pages.size()]));

                this.rulebooks.put(locale, RuleBookFile.convertToPages(file));
            }
            catch(IOException ex)
            {
                this.module.getLog().error(ex, "Error when creating the book!");
            }
        }
    }
}
