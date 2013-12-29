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
package de.cubeisland.engine.itemrepair.repair.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.module.service.Economy;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.itemrepair.Itemrepair;
import de.cubeisland.engine.itemrepair.material.BaseMaterial;
import de.cubeisland.engine.itemrepair.material.BaseMaterialContainer;
import de.cubeisland.engine.itemrepair.material.RepairItem;
import de.cubeisland.engine.itemrepair.material.RepairItemContainer;
import de.cubeisland.engine.itemrepair.repair.RepairBlockManager;
import de.cubeisland.engine.itemrepair.repair.RepairRequest;

public class RepairBlock
{
    private final BaseMaterialContainer priceProvider;
    protected final RepairItemContainer itemProvider;
    private final RepairBlockManager repairBlockManager;
    private final Permission permission;

    private Itemrepair module;

    private final Map<String, RepairBlockInventory> inventoryMap;

    private RepairBlockConfig config;

    private final Random rand;
    private final String name;

    private Economy economy;

    public RepairBlock(Itemrepair module, RepairBlockManager manager, String name, RepairBlockConfig config)
    {
        this.module = module;
        this.name = name;
        this.repairBlockManager = manager;
        this.itemProvider = repairBlockManager.getItemProvider();
        this.priceProvider = itemProvider.getPriceProvider();
        this.permission = this.module.getBasePermission().createAbstractChild("block").createChild(name);
        this.inventoryMap = new HashMap<>();
        this.rand = new Random(System.currentTimeMillis());
        this.config = config;
        this.economy = module.getCore().getModuleManager().getServiceManager().getServiceImplementation(Economy.class);
    }

    public final String getName()
    {
        return this.name;
    }

    public final String getTitle()
    {
        return this.config.title;
    }

    public final Permission getPermission()
    {
        return this.permission;
    }

    public final Material getMaterial()
    {
        return this.config.block;
    }

    public double calculatePrice(Iterable<ItemStack> items)
    {
        return this.calculatePrice(items, this.module.getConfig().price.enchantMultiplier.factor,
           this.module.getConfig().price.enchantMultiplier.base, this.config.costPercentage);
    }

    private double calculatePrice(Iterable<ItemStack> items, double enchantmentFactor, double enchantmentBase, float percentage)
    {
        double price = 0.0;

        Material type;
        RepairItem item;
        double currentPrice;
        for (ItemStack itemStack : items)
        {
            type = itemStack.getType();
            item = itemProvider.of(type);
            currentPrice = 0;
            for (Entry<BaseMaterial, Integer> entry : item.getBaseMaterials().entrySet())
            {
                currentPrice += entry.getKey().getPrice() * entry.getValue();
            }
            currentPrice *= (double)Math.min(itemStack.getDurability(), type.getMaxDurability()) / (double)type.getMaxDurability();
            currentPrice *= getEnchantmentMultiplier(itemStack, enchantmentFactor, enchantmentBase);

            price += currentPrice;
        }
        price *= percentage/100;
        return price;
    }

    public RepairBlockInventory removeInventory(final Player player)
    {
        return this.inventoryMap.remove(player.getName());
    }

    public RepairBlockInventory getInventory(final Player player)
    {
        if (player == null)
        {
            return null;
        }
        RepairBlockInventory inventory = this.inventoryMap.get(player.getName());
        if (inventory == null)
        {
            inventory = new RepairBlockInventory(Bukkit.createInventory(player, 9 * 4, this.config.title), player);
            this.inventoryMap.put(player.getName(), inventory);
        }
        return inventory;
    }

    public class RepairBlockInventory
    {
        public final Inventory inventory;
        public final Player player;

        public RepairBlockInventory(Inventory inventory, Player player)
        {
            this.inventory = inventory;
            this.player = player;
        }
    }

    public boolean withdrawPlayer(User user, double price)
    {
        economy.createPlayerAccount(user.getName()); // Make sure account exists
        if (economy.has(user.getName(), price) && economy.withdraw(user.getName(), price))
        {
            // TODO bankAccounts
            /*
            String account = this.plugin.getServerBank();
            if (eco.hasBankSupport() && !("".equals(account)))
            {
                eco.bankDeposit(account, amount);
            }
            else
            {
                account = this.plugin.getServerPlayer();
                if (!("".equals(account)) && eco.hasAccount(account))
                {
                    eco.depositPlayer(account, amount);
                }
            }
            */
            return true;
        }
        return false;

    }

    public RepairRequest requestRepair(RepairBlockInventory inventory)
    {
        User user = this.module.getCore().getUserManager().getUser(inventory.player.getName());
        Map<Integer, ItemStack> items = this.itemProvider.getRepairableItems(inventory.inventory);
        if (items.size() > 0)
        {
            Double price = calculatePrice(items.values());
            String format = economy.format(price);
            if (this.config.breakPercentage > 0)
            {
                user.sendTranslated("&cItems will break with a chance of &6%.2f%%",this.config.breakPercentage);
            }
            if (this.config.failPercentage > 0)
            {
                user.sendTranslated("&cItems will not repair with a chance of &6%.2f%%",this.config.failPercentage);
            }
            if (this.config.looseEnchantmentsPercentage > 0)
            {
                user.sendTranslated("&cItems will loose all enchantments with a chance of &6%.2f%%",this.config.looseEnchantmentsPercentage);
            }
            if (this.config.costPercentage > 100)
            {
                user.sendTranslated("&eThe repair would cost &b%s &e(&4+%.2f%%&e)",
                        format, this.config.costPercentage - 100);
            }
            else if (this.config.costPercentage < 100)
            {
                user.sendTranslated("&eThe repair would cost &b%s &e(&2-%.2f%%&e)",
                                    format, 100 - this.config.costPercentage);
            }
            else
            {
                user.sendTranslated("&eThe repair would cost &b%s", format);
            }
            user.sendTranslated("&eYou currently have &b%s", economy.format(user.getLocale(), economy.getBalance(user.getName())));
            user.sendTranslated("&bLeftclick&a again to repair all your damaged items.");
            return new RepairRequest(this, inventory, items, price);
        }
        else
        {
            user.sendTranslated("&cThere are no items to repair!");
        }
        return null;
    }

    public void repair(RepairRequest request)
    {
        double price = request.getPrice();
        RepairBlockInventory inventory = request.getInventory();
        User user = this.module.getCore().getUserManager().getExactUser(inventory.player.getName());
        if (withdrawPlayer(user, price))
        {
            boolean itemsBroken = false;
            boolean repairFail = false;
            boolean looseEnch = false;
            ItemStack item;
            int amount;
            for (Map.Entry<Integer, ItemStack> entry : request.getItems().entrySet())
            {
                item = entry.getValue();
                if (this.rand.nextInt(100) >= this.config.breakPercentage)
                {
                    if (this.rand.nextInt(100) >= this.config.failPercentage)
                    {
                        repairItem(entry.getValue());
                    }
                    else
                    {
                        repairFail = true;
                    }
                    if (!entry.getValue().getEnchantments().isEmpty())
                    {
                        if (this.rand.nextInt(100) < this.config.looseEnchantmentsPercentage)
                        {
                            looseEnch = true;
                            for (Enchantment enchantment : entry.getValue().getEnchantments().keySet())
                            {
                                entry.getValue().removeEnchantment(enchantment);
                            }
                        }
                    }
                }
                else
                {
                    itemsBroken = true;
                    amount = item.getAmount();
                    if (amount == 1)
                    {
                        inventory.inventory.clear(entry.getKey());
                    }
                    else
                    {
                        item.setAmount(amount - 1);
                        repairItem(item);
                    }
                }
            }
            if (itemsBroken)
            {
                user.sendTranslated("&cYou broke some of your items when repairing!");
                user.playSound(user.getLocation(),Sound.ANVIL_BREAK,1,0);
            }
            if (repairFail)
            {
                user.sendTranslated("&cYou failed to repair some of your items!");
                user.playSound(user.getLocation(),Sound.BURP,1,0);
            }
            if (looseEnch)
            {
                user.sendTranslated("&cYou feel that some of your items lost their magical power!");
                user.playEffect(user.getLocation(), Effect.GHAST_SHRIEK, 0);
            }
            user.sendTranslated("&aYou paid &b%s&a to repair your items!", economy.format(price));
            if (this.config.costPercentage > 100)
            {
                user.sendTranslated("&aThats %.2f%% of the normal price!", this.config.costPercentage);
            }
            else if (this.config.costPercentage < 100)
            {
                user.sendTranslated("&aThats %.2f%% less then the normal price", 100 - this.config.costPercentage);
            }
        }
        else
        {
           user.sendTranslated("&cYou don't have enough money to repair these items!");
        }
    }

    /*
     * Utilities
     */

    public static double getEnchantmentMultiplier(ItemStack item, double factor, double base)
    {
        double enchantmentLevel = 0;
        for (Integer level : item.getEnchantments().values())
        {
            enchantmentLevel += level;
        }

        if (enchantmentLevel > 0)
        {
            double enchantmentMultiplier = factor * Math.pow(base, enchantmentLevel);

            enchantmentMultiplier = enchantmentMultiplier / 100.0 + 1.0;

            return enchantmentMultiplier;
        }
        else
        {
            return 1.0;
        }
    }

    public static void repairItems(RepairRequest request)
    {
        repairItems(request.getItems().values());
    }

    public static void repairItems(Iterable<ItemStack> items)
    {
        repairItems(items, (short)0);
    }

    public static void repairItems(Iterable<ItemStack> items, short durability)
    {
        for (ItemStack item : items)
        {
            repairItem(item, durability);
        }
    }

    public static void repairItem(ItemStack item)
    {
        repairItem(item, (short)0);
    }

    public static void repairItem(ItemStack item, short durability)
    {
        if (item != null)
        {
            item.setDurability(durability);
        }
    }

    public static void removeHeldItem(Player player)
    {
        PlayerInventory inventory = player.getInventory();
        inventory.clear(inventory.getHeldItemSlot());
    }
}
