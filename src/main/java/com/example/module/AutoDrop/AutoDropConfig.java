package com.example.module.AutoDrop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.config.BaseConfig;
import com.example.util.StringUtil;

public class AutoDropConfig extends BaseConfig {
    public AutoDropConfig(String moduleName) {
        super(moduleName);
    }

    public boolean enabled = getDefaultEnabled();
    public int launchDelay = getDefaultLaunchDelay();
    public boolean showAttentionMsg = getDefaultShowAttentionMsg();
    public List<Integer> ingnoreSlots = new ArrayList<>();
    public String throwDirection = getDefaultThrowDirection();
    public int checkInterval = getDefaultCheckInterval();
    public List<Item> items = new ArrayList<>();
    
    public static boolean getDefaultEnabled() {
        return false;
    }

    public static int getDefaultLaunchDelay() {
        return 20 * 30; // 30s
    }

    public static boolean getDefaultShowAttentionMsg() {
        return true;
    }

    public static String getDefaultIngnoreSlot() {
        return "";
    }

    /**
     * 所有方向请参考 {@link Direction}
     */
    public static String getDefaultThrowDirection() {
        return Direction.LOOKING;
    }

    public static int getDefaultCheckInterval() {
        return 20;
    }

    public static List<String> getAllThrowDirections() {
        return List.of(Direction.DOWN, Direction.UP, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH, Direction.LOOKING);
    }

    public void addItems() {
        items.addFirst(new Item());
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public void addPresetItems() {
        Item bowPreset = new Item();
        bowPreset.enabled = true;
        bowPreset.name = "*";
        bowPreset.id = "minecraft:bow";
        bowPreset.tags.add("*");
        bowPreset.isAllEnchantment = true;
        bowPreset.enchantments.put("minecraft:infinity", 1);
        bowPreset.enchantments.put("minecraft:mending", 1);
        items.add(bowPreset);

        Item swordPreset = new Item();
        swordPreset.enabled = true;
        swordPreset.name = "*";
        swordPreset.id = "*";
        swordPreset.tags = List.of("#minecraft:swords", "#minecraft:axes");
        swordPreset.isAllEnchantment = false;
        swordPreset.enchantments.put("minecraft:sharpness", 5);
        swordPreset.enchantments.put("minecraft:smite", 5);
        swordPreset.enchantments.put("minecraft:bane_of_arthropods", 5);
        items.add(swordPreset);

        Item armorPreset = new Item();
        armorPreset.enabled = true;
        armorPreset.name = "*";
        armorPreset.id = "*";
        armorPreset.tags.add("#minecraft:enchantable/armor");
        armorPreset.isAllEnchantment = false;
        armorPreset.enchantments.put("minecraft:protection", 4);
        armorPreset.enchantments.put("minecraft:fire_protection", 4);
        armorPreset.enchantments.put("minecraft:projectile_protection", 4);
        armorPreset.enchantments.put("minecraft:blast_protection", 4);
        items.add(armorPreset);
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
            return "";
        }

        public static String getDefaultID() {
            return "";
        }

        public static String getDefaultTag() {
            return "";
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

        public void saveTags(String tagsStr) {
            tags.clear();
            tags = StringUtil.strToList(tagsStr);
            tags.removeIf(tag -> !tag.equals("*") && !tag.startsWith("#"));
        }

        public void saveEnchantList(List<String> enchantments) {
            this.enchantments.clear();
            for (String ent: enchantments) {
                ent = ent.replace(" ", "")
                         .replace("：", ":");  // 唉，为什么会有这东西呢
                if (ent.isEmpty() || ent.charAt(ent.length()-1) == ':') {
                    AutoDrop.LOGGER.warn("invalidFormat: `{}`", ent);
                    continue;
                }

                List<Integer> idx = new ArrayList<>();
                for (int i = 0; i < ent.length(); i++) {
                    if (ent.charAt(i) == ':') {
                        idx.add(i);
                    }
                }
                if (idx.size() != 1 && idx.size() != 2) {   // case => minecraft:mending:
                    AutoDrop.LOGGER.warn("invalidFormat: `{}`", ent);
                    continue;
                }
                // case => minecraft:mending
                if (idx.size() == 1) {
                    // level default to 1
                    this.enchantments.put(ent, 1);
                } else {
                    // case => minecraft:sharpness:5
                    String ID = ent.substring(0, idx.get(1));
                    int level = Integer.valueOf(ent.substring(idx.get(1) + 1));
                    this.enchantments.put(ID, level);
                }
            }
        }
    }

    public class Direction {
        public static final String DOWN = "down";
        public static final String UP = "up";
        public static final String EAST = "east";
        public static final String WEST = "west";
        public static final String NORTH = "north";
        public static final String SOUTH = "south";
        public static final String LOOKING = "looking";
    }
}
