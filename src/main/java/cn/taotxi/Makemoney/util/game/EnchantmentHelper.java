package cn.taotxi.Makemoney.util.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class EnchantmentHelper {
    // [ { minecraft:enchantment_name: level }, ..., {...} ]
    public static Map<String, Integer> getEnchantments(ItemStack item) {
        Map<String, Integer> enchantments = new HashMap<>();
        
        ItemEnchantments it = 
            item.is(Items.ENCHANTED_BOOK) ?
            item.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY) :
            item.getEnchantments();

        if (it.isEmpty()) return enchantments;

        for (Holder<Enchantment> enchantHolder: it.keySet()) {
            Optional<ResourceKey<Enchantment>> key = enchantHolder.unwrapKey();
            if (key.isPresent()) {
                enchantments.put(
                    key.get().identifier().toString(), 
                    it.getLevel(enchantHolder)
                );
            }
        }
        
        return enchantments;
    }

    // Argument `enchantment` must have namespace. e.g. "minecraft:mending"
    public static boolean hasEnchantment(ItemStack item, String enchantment) {
        ItemEnchantments it = 
            item.is(Items.ENCHANTED_BOOK) ?
            item.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY) :
            item.getEnchantments();

        if (it.isEmpty()) return false;

        for (Holder<Enchantment> enchantHolder: it.keySet()) {
            Optional<ResourceKey<Enchantment>> key = enchantHolder.unwrapKey();
            if (key.isPresent() && key.get().identifier().toString().equals(enchantment)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasEnchantment(ItemStack item, ResourceKey<Enchantment> enchantment) {
        ItemEnchantments it = 
            item.is(Items.ENCHANTED_BOOK) ?
            item.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY) :
            item.getEnchantments();

        if (it.isEmpty()) return false;

        for (Holder<Enchantment> enchantHolder: it.keySet()) {
            if (enchantHolder.is(enchantment)) {
                return true;
            }
        }

        return false;
    }
}
