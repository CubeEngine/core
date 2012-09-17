package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.CubeEngine;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Wolfi
 */

public class RuleBookConfiguration
{
   private final static int NumberOfCharsPerPage = 260;
   private final static int NumberOfCharsPerLine = 20;
   
   Rulebook ruleBook;
   Map<String, String> ruleMap = new HashMap<String,String>();
   Map<String, List<String>> convertedRuleMap = new HashMap<String, List<String>>();
   
   public RuleBookConfiguration(Rulebook ruleBook)
   {
       this.ruleBook = ruleBook;
       for(String language : this.ruleBook.getCore().getI18n().getLanguages())
       {
           try 
           {
               loadRules(language);
               convertText(language);
           } 
           catch (Exception e) 
           {
               ruleBook.error("can't read the file " + language + ".txt", e);
           }
       }
   }

    private void loadRules(String language) throws FileNotFoundException, IOException 
    {
        File file = new File(ruleBook.getFolder().getAbsolutePath() + File.separator + language + ".txt");
        if(!file.exists())
        {
            try
            {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(CubeEngine._(language, "rulebook", "You have to write down your rules here."));
                writer.close();
            } 
            catch(IOException e)
            {
                ruleBook.error("The languagefile \"" + language + "\" could not be created", e);
            }
        }
        
        String text = "";
        String line = null;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while( (line = reader.readLine()) != null)
        {
            text += (line + "\n");
        }
        reader.close();
        
        this.ruleMap.put(language, text);
    }
      
    private int getNumberOfLines(String string)
    {
        return (int) Math.ceil((double)string.length() / (double)NumberOfCharsPerLine);
    } 
    
    private int getNumberOfLines(String[] strings)
    {
        int result = 0;
        for(String string : strings)
        {
            result += getNumberOfLines(string);
        }
        return result;
    }
    
    private void convertText(String language)
    {
        List<String> lines = createLines(this.getText(language));
        this.convertedRuleMap.put(language, createPages(lines));
    }
    
    private List<String> createLines(String text)
    {
        List<String> lines = new ArrayList<String>();
        for(String line : text.split("\n"))
        {
            line = line.trim();
            while(line.length() > NumberOfCharsPerPage)
            {
                int index = line.substring(0, NumberOfCharsPerPage).lastIndexOf(" ");
                if(index == -1) 
                {
                    index = NumberOfCharsPerPage;
                }
                lines.add(line.substring(0, index));
                line = line.substring(index).trim();
            }
            lines.add(line);
        }    
        return lines;
    }
    
    private List<String> createPages(List<String> lines) 
    {
        // TODO made to parts of the line and copy the first at the last page if there is enough space.  
        List<String> pages = new ArrayList<String>();
        pages.add("");
        int page = 0;
        
        for(String line : lines)
        {
            if( (getNumberOfLines(pages.get(page)) + getNumberOfLines(line) ) > 13)
            {
                page++;
                pages.add("");
            }
            if(pages.get(page).length() == 0)
            {
                pages.set(page, line);
            }
            else
            {
                pages.set(page, pages.get(page) + "\n" + line);
            }
        }
        
        return pages;
    }
    
    public Collection<String> getLanguages()
    {
        return this.ruleMap.keySet();
    }
    
    public String getText(String language)
    {
        return this.ruleMap.get(language);
    }
    
    public String[] getPages(String language)
    { 
        List<String> pageList = this.convertedRuleMap.get(language);
        if(pageList == null) 
        {
            return null;
        }
        return pageList.toArray(new String[pageList.size()]);
    }
}
