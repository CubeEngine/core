/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
    
    private void convertText(String language)
    {
        List<String> convertedText = new ArrayList<String>();
        int lineNumber = 0;
        convertedText.add("");
        
        for(String line : getText(language).split("\n"))
        {
            if(line.length() > 255)
            {
                while(line.length() > 255)
                {
                    // not supported yet
                }
            }
            if( (line.length() + convertedText.get(lineNumber).length()) > 255)
            {
                convertedText.add(line);
                lineNumber++;
            }
            else
            {
                convertedText.set(lineNumber, convertedText.get(lineNumber) + " " + line);
            }
        }
        this.convertedRuleMap.put(language, convertedText);
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
        
        String[] pages = new String[pageList.size()];
        
        for(int i = 0; i < pageList.size(); i++)
        {
            pages[i] = pageList.get(i);
        }
        return pages;
    }
}
