package de.cubeisland.cubeengine.core.i18n;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.i18n.geoip.LookupService;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
import de.cubeisland.cubeengine.core.util.Validate;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Phillip Schichtel
 */
public class I18n
{
    public static final String SOURCE_LANGUAGE = "en_US";
    private final LookupService lookupService;
    private final Map<String, String> countryMap;
    private final Map<String, Language> languageMap;
    private String defaultLanguage;

    public I18n(Core core, String defaultLanguage)
    {
        FileManager fileManager = core.getFileManager();
        try
        {
            this.lookupService = new LookupService(fileManager.getResourceFile(CoreResource.GEOIP_DATABASE));
        }
        catch (IOException e)
        {
            throw new RuntimeException("CubeCore failed to load the GeoIP database!", e);
        }

        this.countryMap = new THashMap<String, String>();
        this.languageMap = new THashMap<String, Language>();

        this.loadLanguages(fileManager.getLanguageDir());
    }

    public String getDefaultLanguage()
    {
        return this.defaultLanguage;
    }

    public void setDefaultLanguage(String language)
    {
        Validate.notNull(language, "The language must not be null!");

        language = normalizeLanguage(language);
        if (this.languageMap.containsKey(language))
        {
            this.defaultLanguage = language;
        }
    }

    private void loadLanguages(File languageDir)
    {
        Language language;
        for (File file : languageDir.listFiles((FileFilter)FileExtentionFilter.JSON))
        {
            try
            {
                String name = file.getName();
                name = name.substring(0, name.lastIndexOf('.'));
                language = new Language(name, languageDir);
                this.languageMap.put(language.getCode(), language);

                for (String country : language.getCountries())
                {
                    this.countryMap.put(country, name);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace(System.err);
            }
            catch (IllegalStateException e)
            {
                e.printStackTrace(System.err);
            }
        }
    }

    public String locateAddress(InetAddress address)
    {
        return this.lookupService.getCountry(address).getCode();
    }

    public String getLanguageFromCountry(String country)
    {
        return this.countryMap.get(country);
    }

    public String translate(String language, String category, String message, Object... params)
    {
        if (SOURCE_LANGUAGE.equalsIgnoreCase(language))
        {
            return message;
        }
        String translation = null;
        Language lang = this.languageMap.get(language);
        if (lang != null)
        {
            translation = lang.getTranslation(category, message);
        }
        return String.format(translation == null ? message : translation, params);
    }

    public void clean()
    {
        this.countryMap.clear();
        for (Language language : this.languageMap.values())
        {
            language.clean();
        }
        this.languageMap.clear();
    }

    public static String normalizeLanguage(String name)
    {
        if (name == null)
        {
            return null;
        }
        final int length = name.length();
        if (length >= 2 && length <= 5)
        {
            final int delimPos = name.indexOf('_');
            if (delimPos < 0 && length == 2)
            {
                return name.toLowerCase(Locale.ENGLISH);
            }
            else if (delimPos == 2 && delimPos == 5)
            {
                return name.substring(0, 1).toLowerCase(Locale.ENGLISH) + '_' + name.substring(3).toUpperCase(Locale.ENGLISH);
            }
        }
        return null;
    }
}