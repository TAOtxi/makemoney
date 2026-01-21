package com.example.module.AutoDrop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.config.BaseConfig;
import com.example.util.T;

public class AutoDropConfig extends BaseConfig {
    public AutoDropConfig(String moduleName) {
        super(moduleName);
    }

    public boolean enabled = getDefaultEnabled();
    public List<Integer> ingnoreSlots = new ArrayList<>();
    public String throwDirection = getDefaultThrowDirection();
    public int checkInterval = getDefaultCheckInterval();
    public List<Item> items = new ArrayList<>();
    
    public static boolean getDefaultEnabled() {
        return false;
    }

    public static String getDefaultIngnoreSlot() {
        return "";
    }

    /**
     * 所有方向请参考 {@link AutoDrop#getAllThrowDirections()}
     */
    public static String getDefaultThrowDirection() {
        return "down";
    }

    public static int getDefaultCheckInterval() {
        return 100;
    }

    public static List<String> getAllThrowDirections() {
        return List.of("up", "down", "east", "west", "north", "south", "looking");
    }

    public void addItems() {
        // 在开头插入
        items.add(0, new Item());
    }

    public class Item {
        public boolean enabled = getDefaultItemEnabled();
        public String name = getDefaultName();
        public String id = getDefaultID();
        public List<String> tags = new ArrayList<>();
        public boolean isAllEnchantment = getDefaultIsAllEnchantment();
        public Map<String, Integer> enchantments = new HashMap<>();

        public static boolean getDefaultItemEnabled() {
            return true;
        }

        public static String getDefaultName() {
            return "*";
        }

        public static String getDefaultID() {
            return "*";
        }

        public static String getDefaultTag() {
            return "*";
        }

        public static List<String> getDefaultEnchantments() {
            return new ArrayList<>();
        }

        public static boolean getDefaultIsAllEnchantment() {
            return false;
        }

        public List<String> getEnchantList() {
            List<String> list = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                list.add(entry.getKey() + ":" + entry.getValue());
            }
            return list;
        }

        public void saveEnchantList(List<String> enchantments) {
            this.enchantments.clear();
            for (int i=enchantments.size()-1; i>=0; i--) {
                String ent = enchantments.get(i);
                if (ent.isEmpty() || !ent.contains(":")) {
                    AutoDrop.LOGGER.warn("invalidFormat: `{}`", ent);
                    continue;
                }

                String[] entSplit = ent.split(":");
                if (entSplit.length != 2) {
                    AutoDrop.LOGGER.warn("invalidFormat: `{}`", ent);
                    enchantments.remove(i);
                    continue;
                }
                this.enchantments.put(entSplit[0].toLowerCase(), Integer.parseInt(entSplit[1]));
            }
        }
    }
}
