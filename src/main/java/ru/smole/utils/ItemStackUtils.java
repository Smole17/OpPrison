package ru.smole.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public class ItemStackUtils {

    public static boolean hasEnchantment(ItemStack itemStack, Enchantment enchantment) {
        return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasEnchant(enchantment);
    }

    public static int getEnchantmentLevel(ItemStack itemStack, Enchantment enchantment) {
        return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasEnchant(enchantment) ? itemStack.getItemMeta().getEnchantLevel(enchantment) : 0;
    }

    public static boolean hasName(ItemStack itemStack) {
        return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName();
    }

    public static boolean hasName(ItemStack itemStack, String name) {
        return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().equals(name);
    }

    public static boolean nameStartsWith(ItemStack itemStack, String name) {
        return itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().startsWith(name);
    }
}
