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
package de.cubeisland.engine.basics.command.moderation.spawnmob;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.bukkit.BukkitUtils;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.matcher.Match;

import static org.bukkit.entity.Villager.Profession;

public class EntityDataChanger<EntityInterface>
{
    private final Class<EntityInterface> clazz;
    protected final EntityChanger<EntityInterface, ?> changer;
    public static final Set<EntityDataChanger> entityDataChangers = new HashSet<>();

    public static final EntityDataChanger<Pig> PIG_SADDLE =
            new EntityDataChanger<>(Pig.class,
                new BoolEntityChanger<Pig>("saddled")
                {
                    @Override
                    public void applyEntity(Pig entity, Boolean input)
                    {
                        entity.setSaddle(input);
                    }
                });

    public static final EntityDataChanger<Horse> HORSE_SADDLE =
        new EntityDataChanger<>(Horse.class,
                                new BoolEntityChanger<Horse>("saddled")
                                {
                                    @Override
                                    public void applyEntity(Horse entity, Boolean input)
                                    {
                                        entity.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                                    }
                                });

    public static final EntityDataChanger<Ageable> AGEABLE_BABY =
            new EntityDataChanger<>(Ageable.class,
                    new BoolEntityChanger<Ageable>("baby") {
                        @Override
                        public void applyEntity(Ageable entity, Boolean input)
                        {
                            if (input) entity.setBaby();
                            else entity.setAdult();
                        }
                    });

    public static final EntityDataChanger<Zombie> ZOMBIE_BABY =
        new EntityDataChanger<>(Zombie.class,
            new BoolEntityChanger<Zombie>("baby") {
                @Override
                public void applyEntity(Zombie entity, Boolean input)
                {
                    entity.setBaby(input);
                }
            });

    public static final EntityDataChanger<Zombie> ZOMBIE_VILLAGER =
        new EntityDataChanger<>(Zombie.class,
          new BoolEntityChanger<Zombie>("villager") {
              @Override
              public void applyEntity(Zombie entity, Boolean input) {
                  entity.setVillager(input);
              }
          });

    public static final EntityDataChanger<Wolf> WOLF_ANGRY =
            new EntityDataChanger<>(Wolf.class,
                    new BoolEntityChanger<Wolf>("angry") {
                        @Override
                        public void applyEntity(Wolf entity, Boolean input) {
                            entity.setAngry(input);
                        }
                    });

    public static final EntityDataChanger<PigZombie> PIGZOMBIE_ANGRY =
        new EntityDataChanger<>(PigZombie.class,
                          new BoolEntityChanger<PigZombie>("angry") {
                              @Override
                              public void applyEntity(PigZombie entity, Boolean input) {
                                  entity.setAngry(input);
                              }
                          });

    public static final EntityDataChanger<Creeper> CREEPER_POWERED =
            new EntityDataChanger<>(Creeper.class,
                    new BoolEntityChanger<Creeper>("powered", "power", "charged") {
                        @Override
                        public void applyEntity(Creeper entity, Boolean input) {
                            entity.setPowered(input);
                        }
                    });

    public static final EntityDataChanger<Wolf> WOLF_SIT =
            new EntityDataChanger<>(Wolf.class,
                    new BoolEntityChanger<Wolf>("sitting", "sit") {
                        @Override
                        public void applyEntity(Wolf entity, Boolean input) {
                            entity.setSitting(input);
                        }
                    });

    public static final EntityDataChanger<Ocelot> OCELOT_SIT =
        new EntityDataChanger<>(Ocelot.class,
         new BoolEntityChanger<Ocelot>("sitting", "sit") {
             @Override
             public void applyEntity(Ocelot entity, Boolean input) {
                 entity.setSitting(input);
             }
         });

    public static final EntityDataChanger<Skeleton> SKELETON_TYPE =
        new EntityDataChanger<>(Skeleton.class,
         new BoolEntityChanger<Skeleton>("wither") {
             @Override
             public void applyEntity(Skeleton entity, Boolean input)
             {
                 if (input) entity.setSkeletonType(SkeletonType.WITHER);
                 else entity.setSkeletonType(SkeletonType.NORMAL);
             }
         });

    public static final EntityDataChanger<Sheep> SHEEP_SHEARED =
        new EntityDataChanger<>(Sheep.class,
                     new BoolEntityChanger<Sheep>("sheared") {
                         @Override
                         public void applyEntity(Sheep entity, Boolean input)
                         {
                            entity.setSheared(input);
                         }
                     });


    public static final EntityDataChanger<Ocelot> OCELOT_TYPE =
        new EntityDataChanger<>(Ocelot.class,
              new MappedEntityChanger<Ocelot, Type>() {
                  @Override
                  void fillValues()
                  {
                      this.map.put("black", Type.BLACK_CAT);
                      this.map.put("red", Type.RED_CAT);
                      this.map.put("orange", Type.RED_CAT);
                      this.map.put("white", Type.SIAMESE_CAT);
                      this.map.put("siamese", Type.SIAMESE_CAT);
                  }

                  @Override
                  public void applyEntity(Ocelot entity, Type input)
                  {
                      entity.setCatType(input);
                  }
              });

    public static  final EntityDataChanger<Colorable> SHEEP_COLOR =
            new EntityDataChanger<>(Colorable.class,
                    new EntityChanger<Colorable, DyeColor>() {
                        @Override
                        public void applyEntity(Colorable entity, DyeColor input)
                        {
                            entity.setColor(input);
                        }
                        @Override
                        public DyeColor getTypeValue(String input)
                        {
                            return Match.materialData().colorData(input);
                        }
                    });

    public static  final EntityDataChanger<Colorable> SHEEP_COLOR_RANDOM =
        new EntityDataChanger<>(Colorable.class,
                 new BoolEntityChanger<Colorable>("random") {
                     private final Random random = new Random(System.nanoTime());
                     @Override
                     public void applyEntity(Colorable entity, Boolean input)
                     {
                         if (input) entity.setColor(DyeColor.getByWoolData((byte)this.random.nextInt(16)));
                     }
                 });


    public static final EntityDataChanger<Wolf> WOLF_COLLAR =
        new EntityDataChanger<>(Wolf.class,
                   new EntityChanger<Wolf, DyeColor>() {
                         @Override
                         public void applyEntity(Wolf entity, DyeColor input) {
                             entity.setCollarColor(input);
                         }
                         @Override
                         public DyeColor getTypeValue(String input)
                         {
                             return Match.materialData().colorData(input);
                         }
                     });

    public static  final EntityDataChanger<Villager> VILLAGER_PROFESSION =
            new EntityDataChanger<>(Villager.class,
                    new EntityChanger<Villager, Profession>() {
                        @Override
                        public void applyEntity(Villager entity, Profession input)
                        {
                            entity.setProfession(input);
                        }

                        @Override
                        public Profession getTypeValue(String input)
                        {
                            return Match.profession().profession(input);
                        }
                    });

    public static final EntityDataChanger<Enderman> ENDERMAN_ITEM =
            new EntityDataChanger<>(Enderman.class,
                    new EntityChanger<Enderman, ItemStack>() {
                        @Override
                        public void applyEntity(Enderman entity, ItemStack value) {
                            if (value.getType().isBlock())
                            {
                                entity.setCarriedMaterial(value.getData());
                            }
                        }

                        @Override
                        public ItemStack getTypeValue(String input)
                        {
                            return Match.material().itemStack(input);
                        }
                    });

    public static final EntityDataChanger<Slime> SLIME_SIZE =
        new EntityDataChanger<>(Slime.class,
                                             new EntityChanger<Slime,Integer>() {
                                                 @Override
                                                 public void applyEntity(Slime entity, Integer input)
                                                 {
                                                     entity.setSize(input);
                                                 }

                                                 @Override
                                                 public Integer getTypeValue(String input)
                                                 {
                                                     String match = Match.string().matchString(input, "tiny", "small", "big");
                                                     if (match != null)
                                                     {
                                                         switch (match)
                                                         {
                                                             case "tiny":
                                                                 return 0;
                                                             case "small":
                                                                 return 2;
                                                             case "big":
                                                                 return 4;
                                                         }
                                                     }
                                                     try
                                                     {
                                                         Integer parsed = Integer.parseInt(input);
                                                         return (parsed > 0 && parsed <= 250) ? parsed : null;
                                                     }
                                                     catch (NumberFormatException ex)
                                                     {
                                                         return null;
                                                     }
                                                 }
                                             });

    public static final EntityDataChanger<LivingEntity> HP =
            new EntityDataChanger<>(LivingEntity.class,
                    new EntityChanger<LivingEntity, Integer>() {
                        @Override
                        public void applyEntity(LivingEntity entity, Integer input)
                        {
                            entity.setMaxHealth(input);
                            entity.setHealth(input);
                        }

                        @Override
                        public Integer getTypeValue(String input)
                        {
                            if (input.endsWith("hp"))
                            {
                                try
                                {
                                    return Integer.parseInt(input.substring(0,input.length()-2));
                                }
                                catch (NumberFormatException ignored)
                                {}
                            }
                            return null;
                        }
                    });

    public static final EntityDataChanger<Horse> HORSE_JUMP =
        new EntityDataChanger<>(Horse.class,
                                new EntityChanger<Horse, Integer>() {
                                    @Override
                                    public void applyEntity(Horse entity, Integer input)
                                    {
                                        entity.setJumpStrength(input);

                                    }

                                    @Override
                                    public Integer getTypeValue(String input)
                                    {
                                        if (input.startsWith("jump"))
                                        {
                                            try
                                            {
                                                int jump = Integer.parseInt(input.substring(4, input.length()));
                                                if (jump >= 0 && jump <= 2)
                                                {
                                                    return jump;
                                                }
                                            }
                                            catch (NumberFormatException ignored)
                                            {}
                                        }
                                        return null;
                                    }
                                });

    // TODO EntitySpeed using Bukkit-API #WaitForBukkit
    public static final EntityDataChanger<LivingEntity> ENTITY_SPEED =
        new EntityDataChanger<>(LivingEntity.class,
                                new EntityChanger<LivingEntity, Double>() {
                                    @Override
                                    public void applyEntity(LivingEntity entity, Double input)
                                    {
                                        BukkitUtils.setEntitySpeed(entity, input);
                                    }

                                    @Override
                                    public Double getTypeValue(String input)
                                    {
                                        if (input.startsWith("speed"))
                                        {
                                            try
                                            {
                                                double speed = Double.parseDouble(input.substring(5, input.length()));
                                                if (speed >= 0 && speed <= 2)
                                                {
                                                    return speed;
                                                }
                                            }
                                            catch (NumberFormatException ignored)
                                            {}
                                        }
                                        return null;
                                    }
                                });

    /*
    BukkitUtils.setEntitySpeed(entity, input);
    Default speed for horse:
                                        return (0.44999998807907104D +
                                            this.random.nextDouble() * 0.3D +
                                            this.random.nextDouble() * 0.3D
                                            + this.random.nextDouble() * 0.3D)
                                            * 0.25D;
     */

    public static final EntityDataChanger<LivingEntity> ENTITY_NAME =
        new EntityDataChanger<>(LivingEntity.class,
                                new EntityChanger<LivingEntity, String>() {
                                    @Override
                                    public void applyEntity(LivingEntity entity, String input)
                                    {
                                        entity.setCustomName(ChatFormat.parseFormats(input));
                                    }

                                    @Override
                                    public String getTypeValue(String input)
                                    {
                                        if (input.startsWith("name_"))
                                        {
                                            return input.substring(5, input.length());
                                        }
                                        return null;
                                    }
                                });

    public static final EntityDataChanger<Tameable> TAMEABLE =
        new EntityDataChanger<>(Tameable.class,
                        new BoolEntityChanger<Tameable>("tamed") {
                            @Override
                            public void applyEntity(Tameable entity, Boolean value) {
                                entity.setTamed(value);
                            }
                        });

    public static final EntityDataChanger<Tameable> TAMER =
        new EntityDataChanger<>(Tameable.class,
                                new EntityChanger<Tameable, AnimalTamer>() {
                                    @Override
                                    public void applyEntity(Tameable entity, AnimalTamer value) {
                                        entity.setOwner(value);
                                    }

                                    @Override
                                    public AnimalTamer getTypeValue(String input)
                                    {
                                        if (input.startsWith("tamer_"))
                                        {
                                            String userName = input.substring(6, input.length());
                                            User user = CubeEngine.getUserManager().findUser(userName);
                                            return user;
                                        }
                                        return null;
                                    }
                                });

    public static final EntityDataChanger<LivingEntity> ARMOR_CHESTPLATE =
        new EntityDataChanger<>(LivingEntity.class,
                                new EntityChanger<LivingEntity, ItemStack>() {
                                    @Override
                                    public void applyEntity(LivingEntity entity, ItemStack value) {
                                        entity.getEquipment().setChestplate(value);
                                    }

                                    @Override
                                    public ItemStack getTypeValue(String input)
                                    {
                                        if (input.startsWith("armor_") || input.startsWith("chest_"))
                                        {
                                            return Match.material().itemStack(input.substring(6, input.length()));
                                        }
                                        return null;
                                    }
                                });

    public static final EntityDataChanger<LivingEntity> ARMOR_LEG =
        new EntityDataChanger<>(LivingEntity.class,
                                new EntityChanger<LivingEntity, ItemStack>() {
                                    @Override
                                    public void applyEntity(LivingEntity entity, ItemStack value) {
                                        entity.getEquipment().setLeggings(value);
                                    }

                                    @Override
                                    public ItemStack getTypeValue(String input)
                                    {
                                        if (input.startsWith("leg_"))
                                        {
                                            return Match.material().itemStack(input.substring(4, input.length()));
                                        }
                                        return null;
                                    }
                                });

    public static final EntityDataChanger<LivingEntity> ARMOR_BOOT =
        new EntityDataChanger<>(LivingEntity.class,
                                new EntityChanger<LivingEntity, ItemStack>() {
                                    @Override
                                    public void applyEntity(LivingEntity entity, ItemStack value) {
                                        entity.getEquipment().setBoots(value);
                                    }

                                    @Override
                                    public ItemStack getTypeValue(String input)
                                    {
                                        if (input.startsWith("boot_"))
                                        {
                                            return Match.material().itemStack(input.substring(5, input.length()));
                                        }
                                        return null;
                                    }
                                });

    public static final EntityDataChanger<LivingEntity> ARMOR_HELMET =
        new EntityDataChanger<>(LivingEntity.class,
                                new EntityChanger<LivingEntity, ItemStack>() {
                                    @Override
                                    public void applyEntity(LivingEntity entity, ItemStack value) {
                                        entity.getEquipment().setHelmet(value);
                                    }

                                    @Override
                                    public ItemStack getTypeValue(String input)
                                    {
                                        if (input.startsWith("helmet_"))
                                        {
                                            return Match.material().itemStack(input.substring(7, input.length()));
                                        }
                                        return null;
                                    }
                                });

    public static final EntityDataChanger<LivingEntity> EQUIP_HAND =
        new EntityDataChanger<>(LivingEntity.class,
                                new EntityChanger<LivingEntity, ItemStack>() {
                                    @Override
                                    public void applyEntity(LivingEntity entity, ItemStack value) {
                                        entity.getEquipment().setItemInHand(value);
                                    }

                                    @Override
                                    public ItemStack getTypeValue(String input)
                                    {
                                        if (input.startsWith("inhand_"))
                                        {
                                            return Match.material().itemStack(input.substring(7, input.length()));
                                        }
                                        return null;
                                    }
                                });

    public static final EntityDataChanger<LivingEntity> DO_DROP_EQUIP =
        new EntityDataChanger<>(LivingEntity.class,
                                new BoolEntityChanger<LivingEntity>("dropEquip") {
                                    @Override
                                    public void applyEntity(LivingEntity entity, Boolean value) {
                                        if (value)
                                        {
                                            EntityEquipment equipment = entity.getEquipment();
                                            equipment.setBootsDropChance(1.1F);
                                            equipment.setLeggingsDropChance(1F);
                                            equipment.setChestplateDropChance(1.1F);
                                            equipment.setHelmetDropChance(1F);
                                            equipment.setItemInHandDropChance(1F);
                                        }
                                    }
                                });

    public static final EntityDataChanger<LivingEntity> DO_NOT_DROP_EQUIP =
        new EntityDataChanger<>(LivingEntity.class,
                                new BoolEntityChanger<LivingEntity>("dropNoEquip") {
                                    @Override
                                    public void applyEntity(LivingEntity entity, Boolean value) {
                                        if (value)
                                        {
                                            EntityEquipment equipment = entity.getEquipment();
                                            equipment.setBootsDropChance(0F);
                                            equipment.setLeggingsDropChance(0F);
                                            equipment.setChestplateDropChance(0F);
                                            equipment.setHelmetDropChance(0F);
                                            equipment.setItemInHandDropChance(0F);
                                        }
                                    }
                                });

    public static final EntityDataChanger<Horse> HORSE_COLOR =
        new EntityDataChanger<>(Horse.class,
                                new MappedEntityChanger<Horse, Horse.Color>() {
                                    @Override
                                    void fillValues()
                                    {
                                        this.map.put("white", Horse.Color.WHITE);
                                        this.map.put("creamy", Horse.Color.CREAMY);
                                        this.map.put("chestnut", Horse.Color.CHESTNUT);
                                        this.map.put("brown", Horse.Color.BROWN);
                                        this.map.put("black", Horse.Color.BLACK);
                                        this.map.put("gray", Horse.Color.GRAY);
                                        this.map.put("darkbrown", Horse.Color.DARK_BROWN);
                                    }

                                    @Override
                                    public void applyEntity(Horse entity, Horse.Color input)
                                    {
                                        entity.setColor(input);
                                    }
                                });

    public static final EntityDataChanger<Horse> HORSE_VARIANT =
        new EntityDataChanger<>(Horse.class,
                                new MappedEntityChanger<Horse, Horse.Variant>() {
                                    @Override
                                    void fillValues()
                                    {
                                        this.map.put("horse", Variant.HORSE);
                                        this.map.put("donkey", Variant.DONKEY);
                                        this.map.put("mule", Variant.MULE);
                                        this.map.put("undead", Variant.UNDEAD_HORSE);
                                        this.map.put("skeleton", Variant.SKELETON_HORSE);
                                    }

                                    @Override
                                    public void applyEntity(Horse entity, Horse.Variant input)
                                    {
                                        entity.setVariant(input);
                                    }
                                });

    public static final EntityDataChanger<Horse> HORSE_STYLE =
        new EntityDataChanger<>(Horse.class,
                                new MappedEntityChanger<Horse, Horse.Style>() {
                                    @Override
                                    void fillValues()
                                    {
                                        this.map.put("stylenone", Style.NONE);
                                        this.map.put("stylewhite", Style.WHITE);
                                        this.map.put("whitefield", Style.WHITEFIELD);
                                        this.map.put("whitedots", Style.WHITE_DOTS);
                                        this.map.put("blackdots", Style.BLACK_DOTS);
                                    }

                                    @Override
                                    public void applyEntity(Horse entity, Style input)
                                    {
                                        entity.setStyle(input);
                                    }
                                });


    private EntityDataChanger(Class<EntityInterface> clazz, EntityChanger<EntityInterface, ?> changer)
    {
        this.clazz = clazz;
        this.changer = changer;
        entityDataChangers.add(this);
    }

    @SuppressWarnings("unchecked")
    public boolean applyTo(Entity entity, String value)
    {
        if (canApply(entity))
        {
            this.changer.applyEntity((EntityInterface)entity, value);
            return true;
        }
        return false;
    }

    public boolean canApply(Entity entity)
    {
        return clazz.isAssignableFrom(entity.getClass());
    }

    public static abstract class EntityChanger<E,T>
    {
        public void applyEntity(E entity, String input)
        {
            T typeValue = this.getTypeValue(input);
            if (typeValue != null) this.applyEntity(entity, typeValue);
        }
        public abstract void applyEntity(E entity, T convertedInput);
        public abstract T getTypeValue(String input);
    }

    private static abstract class BoolEntityChanger<E> extends EntityChanger<E, Boolean>
    {
        private final List<String> names;
        protected BoolEntityChanger(String... names)
        {
            this.names = Arrays.asList(names);
        }

        @Override
        public Boolean getTypeValue(String input)
        {
            for (String name : names)
            {
                if (name.equalsIgnoreCase(input))
                {
                    return true;
                }
            }
            return null;
        }
    }

    private static abstract class MappedEntityChanger<E, T> extends EntityChanger<E, T>
    {
        protected final Map<String, T> map = new HashMap<>();

        protected MappedEntityChanger()
        {
            this.fillValues();
        }

        @Override
        public T getTypeValue(String input)
        {
            return map.get(input);
        }

        abstract void fillValues();
    }
}
