package de.cubeisland.cubeengine.core.i18n;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.i18n.geoip.LookupService;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
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

    public I18n(Core core)
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

    private void loadLanguages(File languageDir)
    {
        Language language;
        for (File file : languageDir.listFiles((FileFilter)FileExtentionFilter.JSON))
        {
            try
            {
                String name = file.getName();
                name = name.substring(0, name.lastIndexOf("."));
                language = new Language(name, languageDir);
                this.languageMap.put(language.getCode(), language);

                for (String country : language.getCountries())
                {
                    this.countryMap.put(country, name);
                }
            }
            catch (FileNotFoundException e)
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
}
