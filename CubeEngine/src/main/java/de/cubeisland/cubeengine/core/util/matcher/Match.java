package de.cubeisland.cubeengine.core.util.matcher;

import de.cubeisland.cubeengine.core.CubeEngine;

public class Match {

    private MaterialMatcher materialMatcher;
    private MaterialDataMatcher materialDataMatcher;
    private EnchantMatcher enchantMatcher;
    private ProfessionMatcher professionMatcher;
    private EntityMatcher entityMatcher;



    public Match() {
        this.materialDataMatcher = new MaterialDataMatcher();
        this.materialMatcher = new MaterialMatcher(materialDataMatcher);
        this.enchantMatcher = new EnchantMatcher();
        this.professionMatcher = new ProfessionMatcher();
        this.entityMatcher = new EntityMatcher();

    }

    public static MaterialMatcher material()
    {
        return CubeEngine.getCore().getMatcherManager().materialMatcher;
    }

    public static MaterialDataMatcher materialData()
    {
        return CubeEngine.getCore().getMatcherManager().materialDataMatcher;
    }

    public static EnchantMatcher enchant()
    {
        return CubeEngine.getCore().getMatcherManager().enchantMatcher;
    }

    public static ProfessionMatcher profession()
    {
        return CubeEngine.getCore().getMatcherManager().professionMatcher;
    }

    public static EntityMatcher entity()
    {
        return CubeEngine.getCore().getMatcherManager().entityMatcher;
    }
}
