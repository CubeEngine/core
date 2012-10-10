package de.cubeisland.cubeengine.core.util.matcher;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Projectile;

/**
 *
 * @author Anselm Brehme
 */
public enum EntityType
{
    DROPPED_ITEM(org.bukkit.entity.EntityType.DROPPED_ITEM, false),
    EXPERIENCE_ORB(org.bukkit.entity.EntityType.EXPERIENCE_ORB, false),
    PAINTING(org.bukkit.entity.EntityType.PAINTING, false),
    ARROW(org.bukkit.entity.EntityType.ARROW, false),
    SNOWBALL(org.bukkit.entity.EntityType.SNOWBALL, false),
    FIREBALL(org.bukkit.entity.EntityType.FIREBALL, false),
    SMALL_FIREBALL(org.bukkit.entity.EntityType.SMALL_FIREBALL, false),
    ENDER_PEARL(org.bukkit.entity.EntityType.ENDER_PEARL, false),
    ENDER_SIGNAL(org.bukkit.entity.EntityType.ENDER_SIGNAL, false),
    THROWN_EXP_BOTTLE(org.bukkit.entity.EntityType.THROWN_EXP_BOTTLE, false),
    PRIMED_TNT(org.bukkit.entity.EntityType.PRIMED_TNT, false),
    FALLING_BLOCK(org.bukkit.entity.EntityType.FALLING_BLOCK, false),
    MINECART(org.bukkit.entity.EntityType.MINECART, false),
    BOAT(org.bukkit.entity.EntityType.BOAT, false),
    CREEPER(org.bukkit.entity.EntityType.CREEPER, true),
    SKELETON(org.bukkit.entity.EntityType.SKELETON, true),
    SPIDER(org.bukkit.entity.EntityType.SPIDER, true),
    GIANT(org.bukkit.entity.EntityType.GIANT, false),
    ZOMBIE(org.bukkit.entity.EntityType.ZOMBIE, true),
    SLIME(org.bukkit.entity.EntityType.SLIME, true),
    GHAST(org.bukkit.entity.EntityType.GHAST, true),
    PIG_ZOMBIE(org.bukkit.entity.EntityType.PIG_ZOMBIE, true),
    ENDERMAN(org.bukkit.entity.EntityType.ENDERMAN, true),
    CAVE_SPIDER(org.bukkit.entity.EntityType.CAVE_SPIDER, true),
    SILVERFISH(org.bukkit.entity.EntityType.SILVERFISH, true),
    BLAZE(org.bukkit.entity.EntityType.BLAZE, true),
    MAGMA_CUBE(org.bukkit.entity.EntityType.MAGMA_CUBE, true),
    ENDER_DRAGON(org.bukkit.entity.EntityType.ENDER_DRAGON, false),
    PIG(org.bukkit.entity.EntityType.PIG, true),
    SHEEP(org.bukkit.entity.EntityType.SHEEP, true),
    COW(org.bukkit.entity.EntityType.COW, true),
    CHICKEN(org.bukkit.entity.EntityType.CHICKEN, true),
    SQUID(org.bukkit.entity.EntityType.SQUID, true),
    WOLF(org.bukkit.entity.EntityType.WOLF, true),
    MUSHROOM_COW(org.bukkit.entity.EntityType.MUSHROOM_COW, true),
    SNOWMAN(org.bukkit.entity.EntityType.SNOWMAN, false),
    OCELOT(org.bukkit.entity.EntityType.OCELOT, true),
    IRON_GOLEM(org.bukkit.entity.EntityType.IRON_GOLEM, false),
    VILLAGER(org.bukkit.entity.EntityType.VILLAGER, true),
    ENDER_CRYSTAL(org.bukkit.entity.EntityType.ENDER_CRYSTAL, false),
    // These don't have an entity ID in nms.EntityTypes.
    SPLASH_POTION(org.bukkit.entity.EntityType.SPLASH_POTION, false),
    EGG(org.bukkit.entity.EntityType.EGG, false),
    FISHING_HOOK(org.bukkit.entity.EntityType.FISHING_HOOK, false),
    /**
     * Spawn with {@link World#strikeLightning(org.bukkit.Location)}.
     */
    LIGHTNING(org.bukkit.entity.EntityType.LIGHTNING, false),
    WEATHER(org.bukkit.entity.EntityType.WEATHER, false),
    PLAYER(org.bukkit.entity.EntityType.PLAYER, false),
    COMPLEX_PART(org.bukkit.entity.EntityType.COMPLEX_PART, false),
    /**
     * An unknown entity without an Entity Class
     */
    UNKNOWN(org.bukkit.entity.EntityType.UNKNOWN, false);
    ;

    private org.bukkit.entity.EntityType type;
    private boolean spawnEgg;
    private static final Map<EntityType, String> REVERSE_NAME_MAP = new EnumMap<EntityType, String>(EntityType.class);
    private static final Map<String, EntityType> NAME_MAP = new THashMap<String, EntityType>();
    private static final TShortObjectHashMap<EntityType> ID_MAP = new TShortObjectHashMap<EntityType>();

    static
    {
        for (EntityType type : values())
        {
            if (type.getBukkitType().getTypeId() > 0)
            {
                ID_MAP.put(type.getBukkitType().getTypeId(), type);
            }
        }
    }

    private EntityType(org.bukkit.entity.EntityType type, boolean spawnEgg)
    {
        this.type = type;
        this.spawnEgg = spawnEgg;
    }

    public boolean canBeSpawnedBySpawnEgg()
    {
        return spawnEgg;
    }

    public org.bukkit.entity.EntityType getBukkitType()
    {
        return this.type;
    }

    public static EntityType fromId(short id)
    {
        return ID_MAP.get(id);
    }

    public Class<? extends Entity> getEntityClass()
    {
        return this.type.getEntityClass();
    }

    public boolean isAlive()
    {
        return this.type.isAlive();
    }

    public boolean isMonster()
    {
        return Monster.class.isAssignableFrom(this.getEntityClass());
    }

    public boolean isFriendly()
    {
        return this.isAnimal() || NPC.class.isAssignableFrom(this.
            getEntityClass());
    }

    public boolean isAnimal()
    {
        return Animals.class.isAssignableFrom(this.getEntityClass());
    }

    public boolean isProjectile()
    {
        return Projectile.class.isAssignableFrom(this.getEntityClass());
    }

    public void registerName(List<String> names)
    {
        if (names.isEmpty())
        {
            return;
        }
        REVERSE_NAME_MAP.put(this, names.get(0));
        for (String name : names)
        {
            NAME_MAP.put(name.toLowerCase(Locale.ENGLISH), this);
        }
    }

    public static Map<String, EntityType> getNameSets()
    {
        return NAME_MAP;
    }

    @Override
    public String toString()
    {
         return REVERSE_NAME_MAP.get(this);
    }
}
