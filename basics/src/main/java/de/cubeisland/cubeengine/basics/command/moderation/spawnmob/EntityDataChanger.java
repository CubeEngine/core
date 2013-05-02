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
package de.cubeisland.cubeengine.basics.command.moderation.spawnmob;

import org.bukkit.DyeColor;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
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
import org.bukkit.inventory.ItemStack;

public class EntityDataChanger<E,T>
{
    private Class<E> clazz;
    private EntityChanger<E,T> changer;

    public static final EntityDataChanger<Pig,Boolean> PIGSADDLE =
            new EntityDataChanger<Pig,Boolean>(Pig.class,
                    new EntityChanger<Pig,Boolean>() {
                        @Override
                        public void applyEntity(Pig entity, Boolean value) {
                            entity.setSaddle(value);
                        }
                    });

    public static final EntityDataChanger<Ageable,Boolean> BABYAGEABLE =
            new EntityDataChanger<Ageable,Boolean>(Ageable.class,
                    new EntityChanger<Ageable,Boolean>() {
                        @Override
                        public void applyEntity(Ageable entity, Boolean value) {
                            if (value)
                            {
                                entity.setBaby();
                            }
                            else
                            {
                                entity.setAdult();
                            }
                        }
                    });

    public static final EntityDataChanger<Zombie,Boolean> BABYZOMBIE =
        new EntityDataChanger<Zombie,Boolean>(Zombie.class,
                      new EntityChanger<Zombie,Boolean>() {
                          @Override
                          public void applyEntity(Zombie entity, Boolean value) {
                              entity.setBaby(value);
                          }
                      });

    public static  final EntityDataChanger<Wolf,Boolean> ANGRYWOLF =
            new EntityDataChanger<Wolf,Boolean>(Wolf.class,
                    new EntityChanger<Wolf,Boolean>() {
                        @Override
                        public void applyEntity(Wolf entity, Boolean value) {
                            entity.setAngry(value);
                        }
                    });

    public static  final EntityDataChanger<PigZombie,Boolean> ANGRYPIGZOMBIE =
        new EntityDataChanger<PigZombie,Boolean>(PigZombie.class,
                          new EntityChanger<PigZombie,Boolean>() {
                              @Override
                              public void applyEntity(PigZombie entity, Boolean value) {
                                  entity.setAngry(value);
                              }
                          });

    public static final EntityDataChanger<Creeper,Boolean> POWERED =
            new EntityDataChanger<Creeper,Boolean>(Creeper.class,
                    new EntityChanger<Creeper,Boolean>() {
                        @Override
                        public void applyEntity(Creeper entity, Boolean value) {
                            entity.setPowered(value);
                        }
                    });

    public static final EntityDataChanger<Wolf,Boolean> SITTINGWOLF =
            new EntityDataChanger<Wolf,Boolean>(Wolf.class,
                    new EntityChanger<Wolf,Boolean>() {
                        @Override
                        public void applyEntity(Wolf entity, Boolean value) {
                            entity.setSitting(value);
                        }
                    });

    public static final EntityDataChanger<Ocelot,Boolean> SITTINGOCELOT =
        new EntityDataChanger<Ocelot,Boolean>(Ocelot.class,
                  new EntityChanger<Ocelot,Boolean>() {
                      @Override
                      public void applyEntity(Ocelot entity, Boolean value) {
                          entity.setSitting(value);
                      }
                  });

    public static  final EntityDataChanger<Tameable,AnimalTamer> TAME =
            new EntityDataChanger<Tameable,AnimalTamer>(Tameable.class,
                    new EntityChanger<Tameable,AnimalTamer>() {
                        @Override
                        public void applyEntity(Tameable entity, AnimalTamer value) {
                            entity.setTamed(true);
                            if (value != null)
                            {
                                entity.setOwner(value);
                            }
                        }
                    });

    public static  final EntityDataChanger<Sheep,DyeColor> SHEEP_COLOR =
            new EntityDataChanger<Sheep,DyeColor>(Sheep.class,
                    new EntityChanger<Sheep,DyeColor>() {
                        @Override
                        public void applyEntity(Sheep entity, DyeColor value) {
                            entity.setColor(value);
                        }
                    });

    public static  final EntityDataChanger<Slime,Integer> SLIME_SIZE =
            new EntityDataChanger<Slime,Integer>(Slime.class,
                    new EntityChanger<Slime,Integer>() {
                        @Override
                        public void applyEntity(Slime entity, Integer size) {
                            if (size > 0 && size <= 250)
                            {
                                entity.setSize(size);
                            }
                        }
                    });

    public static  final EntityDataChanger<Villager,Villager.Profession> VILLAGER_PROFESSION =
            new EntityDataChanger<Villager,Villager.Profession>(Villager.class,
                    new EntityChanger<Villager,Villager.Profession>() {
                        @Override
                        public void applyEntity(Villager entity, Villager.Profession value) {
                            entity.setProfession(value);
                        }
                    });

    public static final EntityDataChanger<Enderman,ItemStack> ENDERMAN_ITEM =
            new EntityDataChanger<Enderman,ItemStack>(Enderman.class,
                    new EntityChanger<Enderman,ItemStack>() {
                        @Override
                        public void applyEntity(Enderman entity, ItemStack value) {
                            if (value.getType().isBlock())
                            {
                                entity.setCarriedMaterial(value.getData());
                            }
                        }
                    });

    public static final EntityDataChanger<LivingEntity,Integer> HP =
            new EntityDataChanger<LivingEntity,Integer>(LivingEntity.class,
                    new EntityChanger<LivingEntity,Integer>() {
                        @Override
                        public void applyEntity(LivingEntity entity, Integer value) {
                            entity.setMaxHealth(value);
                            entity.setHealth(value);
                        }
                    });

    public static final EntityDataChanger<Zombie,Boolean> VILLAGER_ZOMBIE =
            new EntityDataChanger<Zombie,Boolean>(Zombie.class,
                    new EntityChanger<Zombie,Boolean>() {
                        @Override
                        public void applyEntity(Zombie entity, Boolean value) {
                            entity.setVillager(value);
                        }
                    });

    public static final EntityDataChanger<LivingEntity,ItemStack> EQUIP_ITEMINHAND =
            new EntityDataChanger<LivingEntity,ItemStack>(LivingEntity.class,
                    new EntityChanger<LivingEntity,ItemStack>() {
                        @Override
                        public void applyEntity(LivingEntity entity, ItemStack value) {
                            entity.getEquipment().setItemInHand(value);
                        }
                    });

    public static final EntityDataChanger<LivingEntity,ItemStack> EQUIP_HELMET =
            new EntityDataChanger<LivingEntity,ItemStack>(LivingEntity.class,
                    new EntityChanger<LivingEntity,ItemStack>() {
                        @Override
                        public void applyEntity(LivingEntity entity, ItemStack value) {
                            entity.getEquipment().setHelmet(value);
                        }
                    });

    public static final EntityDataChanger<LivingEntity,ItemStack> EQUIP_CHESTPLATE =
            new EntityDataChanger<LivingEntity,ItemStack>(LivingEntity.class,
                    new EntityChanger<LivingEntity,ItemStack>() {
                        @Override
                        public void applyEntity(LivingEntity entity, ItemStack value) {
                            entity.getEquipment().setChestplate(value);
                        }
                    });

    public static final EntityDataChanger<LivingEntity,ItemStack> EQUIP_LEGGINGS =
            new EntityDataChanger<LivingEntity,ItemStack>(LivingEntity.class,
                    new EntityChanger<LivingEntity,ItemStack>() {
                        @Override
                        public void applyEntity(LivingEntity entity, ItemStack value) {
                            entity.getEquipment().setLeggings(value);
                        }
                    });
    public static final EntityDataChanger<LivingEntity,ItemStack> EQUIP_BOOTS =
            new EntityDataChanger<LivingEntity,ItemStack>(LivingEntity.class,
                    new EntityChanger<LivingEntity,ItemStack>() {
                        @Override
                        public void applyEntity(LivingEntity entity, ItemStack value) {
                            entity.getEquipment().setBoots(value);
                        }
                    });

    public static final EntityDataChanger<Wolf,DyeColor> WOLF_COLLAR =
        new EntityDataChanger<Wolf,DyeColor>(Wolf.class,
                      new EntityChanger<Wolf,DyeColor>() {
                          @Override
                          public void applyEntity(Wolf entity, DyeColor value) {
                              entity.setCollarColor(value);
                          }
                      });

    public static final EntityDataChanger<Skeleton,SkeletonType> SKELETON_TYPE =
        new EntityDataChanger<Skeleton,SkeletonType>(Skeleton.class,
                 new EntityChanger<Skeleton,SkeletonType>() {
                     @Override
                     public void applyEntity(Skeleton entity, SkeletonType value) {
                         entity.setSkeletonType(value);
                     }
                 });

    public static final EntityDataChanger<Ocelot,Type> CAT_TYPE =
        new EntityDataChanger<Ocelot,Type>(Ocelot.class,
                 new EntityChanger<Ocelot,Type>() {
                     @Override
                     public void applyEntity(Ocelot entity, Type value) {
                         entity.setCatType(value);
                     }
                 });

    public static final EntityDataChanger<Sheep,Boolean> SHEEP_SHEARED =
        new EntityDataChanger<Sheep,Boolean>(Sheep.class,
                 new EntityChanger<Sheep,Boolean>() {
                     @Override
                     public void applyEntity(Sheep entity, Boolean value) {
                         entity.setSheared(value);
                     }
                 });

    private EntityDataChanger(Class<E> clazz, EntityChanger<E,T> changer)
    {
        this.clazz = clazz;
        this.changer = changer;
    }

    @SuppressWarnings("unchecked")
    public boolean applyTo(Entity entity, T value)
    {
        if (clazz.isAssignableFrom(entity.getClass()))
        {
            this.changer.applyEntity((E)entity, value);
            return true;
        }
        return false;
    }

    private static abstract class EntityChanger<E,T>
    {
        public abstract void applyEntity(E entity, T value);
    }
}
