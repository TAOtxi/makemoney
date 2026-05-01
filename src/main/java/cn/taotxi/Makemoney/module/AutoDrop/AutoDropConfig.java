package cn.taotxi.Makemoney.module.AutoDrop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.taotxi.Makemoney.config.BaseConfig;
import cn.taotxi.Makemoney.util.ItemStackUtil;
import cn.taotxi.Makemoney.util.StringUtil;

public class AutoDropConfig extends BaseConfig {
    public AutoDropConfig(String moduleName) {
        super(moduleName);
        CONFIG_VERSION = getDefaultConfigVersion();
    }

    public transient boolean enabled = getDefaultEnabled();
    public boolean showAttentionMsg = getDefaultShowAttentionMsg();
    public List<Integer> ingnoreSlots = new ArrayList<>();
    public String throwDirection = getDefaultThrowDirection();
    public int checkInterval = getDefaultCheckInterval();
    public List<Item> items = new ArrayList<>();

    @Override
    public String getDefaultConfigVersion() {
        return "0.0.3";
    }
    
    public static boolean getDefaultEnabled() {
        return false;
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
        return Direction.LOOKING.name();
    }

    public static int getDefaultCheckInterval() {
        return 20 * 20; // 20s
    }

    public static List<Direction> getAllThrowDirections() {
        return List.of(Direction.DOWN, Direction.UP, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH, Direction.LOOKING);
    }

    public void addItems() {
        items.addFirst(new Item());
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public void addPresetItems() {
        Item swordPreset = new Item();
        swordPreset.name = "*";
        swordPreset.id = "/^minecraft:(?:diamond|netherite)_.*$/";
        swordPreset.tags = List.of("#enchantable/sharp_weapon");
        swordPreset.minEnchantRequir = 2;
        swordPreset.enchantments.put("sharpness", 5);
        swordPreset.enchantments.put("smite", 5);
        swordPreset.enchantments.put("bane_of_arthropods", 5);
        items.add(swordPreset);

        Item armorPreset = new Item();
        armorPreset.name = "*";
        armorPreset.id = "/^minecraft:(?:diamond|netherite)_.*$/";
        armorPreset.tags.add("#enchantable/armor");
        armorPreset.minEnchantRequir = 3;
        armorPreset.enchantments.put("protection", 4);
        armorPreset.enchantments.put("fire_protection", 4);
        armorPreset.enchantments.put("projectile_protection", 4);
        armorPreset.enchantments.put("blast_protection", 4);
        items.add(armorPreset);

        Item bowPreset = new Item();
        bowPreset.name = "*";
        bowPreset.id = "bow";
        bowPreset.minEnchantRequir = 2;
        bowPreset.enchantments.put("infinity", 1);
        bowPreset.enchantments.put("mending", 1);
        items.add(bowPreset);

        Item paperPreset = new Item();
        paperPreset.name = "*";
        paperPreset.id = "paper";
        items.add(paperPreset);

        Item diamondPreset = new Item();
        diamondPreset.name = "*";
        diamondPreset.id = "/^minecraft:diamond(?:_block)?$/";
        items.add(diamondPreset);

        Item netheritePreset = new Item();
        netheritePreset.name = "*";
        netheritePreset.id = "/^minecraft:netherite.*$/";
        items.add(netheritePreset);

        Item fishRodPreset = new Item();
        fishRodPreset.name = "*";
        fishRodPreset.id = "fishing_rod";
        items.add(fishRodPreset);
    }

    public class Item {
        public boolean enabled = getDefaultItemEnabled();
        public String name = getDefaultName();
        public String id = getDefaultID();
        public List<String> tags = new ArrayList<>();
        public int minEnchantRequir = getDefaultMinEnchantRequir();
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

        public static int getDefaultMinEnchantRequir() {
            return 0;
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

            // minecraft:mending:1
            // minecraft:mending
            // mending:1
            // mending
            Pattern pattern = Pattern.compile("^(?:minecraft:)?([^:]+)(?::(\\d+))?$");

            for (String ent: enchantments) {
                Matcher matcher = pattern.matcher(ent);
                if (!matcher.find()) {
                    AutoDrop.LOGGER.warn("invalidFormat: `{}`", ent);
                    continue;
                }
                String name = matcher.group(1);
                String levelStr = matcher.group(2);
                int level = levelStr != null ? Integer.valueOf(levelStr) : 1;

                this.enchantments.put(name, level);
            }
        }
    }

    public enum Direction {
        DOWN, UP, EAST, WEST, NORTH, SOUTH, LOOKING
    }
}
