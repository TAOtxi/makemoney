package cn.taotxi.Makemoney.module.AutoDrop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cn.taotxi.Makemoney.config.type.ConfigArray;
import cn.taotxi.Makemoney.config.type.ConfigBoolean;
import cn.taotxi.Makemoney.config.type.ConfigFloat;
import cn.taotxi.Makemoney.config.type.ConfigInteger;
import cn.taotxi.Makemoney.config.type.ConfigString;

import cn.taotxi.Makemoney.config.ConfigManager;
import cn.taotxi.Makemoney.util.StringUtil;
import cn.taotxi.Makemoney.util.game.ItemStackUtil;

public class AutoDropConfig extends ConfigManager {
    private static AutoDropConfig instance = null;

    public static AutoDropConfig getInstance() {
        if (instance == null) {
            instance = new AutoDropConfig(AutoDrop.MODULE_NAME);
        }
        return instance;
    }

    public AutoDropConfig(String moduleName) {
        super(moduleName);
    }

    public ConfigBoolean  isShowAttentionMsg        = new ConfigBoolean("isShowAttentionMsg", true, "是否显示注意信息", this);
    public ConfigArray    ignoreSlots               = new ConfigArray("ignoreSlots", new JsonArray(), "忽略的背包槽位列表", this);
    public ConfigString   throwWay                  = new ConfigString("throwWay", ThrowWay.DIRECTION.name(), "丢弃方式", this);
    public ConfigString   throwDirection            = new ConfigString("throwDirection", Direction.LOOKING.name(), "丢弃方向", this);
    public ConfigFloat    throwYaw                  = new ConfigFloat("throwYaw", 0.0f, "yaw", this);
    public ConfigFloat    throwPitch                = new ConfigFloat("throwPitch", 0.0f, "pitch", this);

    public ConfigBoolean  dropWhenOpenContainer     = new ConfigBoolean("dropWhenOpenContainer", false, "打开容器时是否丢弃容器内物品", this);
    public ConfigBoolean  isTimeTrigger             = new ConfigBoolean("isTimeTrigger", true, "定时触发丢弃功能开关", this);
    public ConfigInteger  timeTriggerInterval       = new ConfigInteger("timeTriggerInterval", 180 * 20, "定时触发时间间隔（tick）", this);
    public ConfigBoolean  isPickUpItemTrigger       = new ConfigBoolean("isPickUpItemTrigger", false, "拾取到指定掉落物触发丢弃功能开关", this);
    public ConfigString   triggerItemId             = new ConfigString("triggerItemId", "", "拾取到掉落物ID", this);

    public ConfigBoolean  turnOffWhenChangeWorld    = new ConfigBoolean("turnOffWhenChangeWorld", true, "切换世界时是否关闭自动丢弃功能", this);
    public ConfigInteger  triggerMinCount           = new ConfigInteger("triggerMinCount", 0, "允许丢弃所需最少物品槽位数量", this);
    public ConfigBoolean  stopWhenCrouch            = new ConfigBoolean("stopWhenCrouch", true, "潜行时是否禁用丢弃功能", this);
    public ConfigBoolean  stopWhenOpenConfigGui     = new ConfigBoolean("stopWhenOpenConfigGui", true, "打开配置GUI时是否禁用丢弃功能", this);   

    public ConfigBoolean  stopWhenNotHoldingItem    = new ConfigBoolean("stopWhenNotHoldingItem", false, "未手持指定物品时是否禁用丢弃功能", this);
    public ConfigString   stopWhenNotHoldingItemName= new ConfigString("stopWhenNotHoldingItemName", "", "指定手持指定物品的名字", this);
    public ConfigString   stopWhenNotHoldingItemId  = new ConfigString("stopWhenNotHoldingItemId", "", "指定手持指定物品的ID", this);

    public ConfigBoolean  whiteListMode             = new ConfigBoolean("whiteListMode", true, "是否开启白名单模式", this);
    public ConfigArray    matchItemLists            = new ConfigArray("matchItemLists", new JsonArray(), "物品匹配条件列表", this);

    public List<String> getAllThrowDirections() {
        return Arrays.stream(Direction.values()).map(Enum::name).collect(Collectors.toList());
    }

    public Item getDefaultMatchItem() {
        return new Item();
    }

    public void addMatchItem() {
        Item defaultItem = getDefaultMatchItem();
        JsonObject item = getGson().toJsonTree(defaultItem).getAsJsonObject();

        // add to top
        JsonArray newMatchLists = new JsonArray();
        newMatchLists.add(item);
        newMatchLists.addAll(matchItemLists.getValue());
        matchItemLists.setValue(newMatchLists);

        // add to tail
        // matchLists.getValue().add(item);
    }

    public Item getStdMatchItem(int index) {
        Item matchItem = getGson().fromJson(matchItemLists.getValue().get(index), Item.class);
        if (!matchItem.id.equals("*") && !StringUtil.isRegex(matchItem.id)) {
            matchItem.id = ItemStackUtil.withDefaultNamespace(matchItem.id);
        }
        Map<String, Integer> withDefaultNameSpaceMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry: matchItem.enchantments.entrySet()) {
            String enchantmentName = ItemStackUtil.withDefaultNamespace(entry.getKey());
            withDefaultNameSpaceMap.put(enchantmentName, entry.getValue());
        }
        matchItem.enchantments = withDefaultNameSpaceMap;
        
        if (matchItem.tags.contains("*")) {
            matchItem.tags.clear();
        } else {
            List<String> withNamespaceTags = new ArrayList<>(matchItem.tags.size());
            for (String tag: matchItem.tags) {
                if (!tag.startsWith("#")) {
                    throw new IllegalArgumentException("Tag must start with #, but got " + tag);
                };
                withNamespaceTags.add(ItemStackUtil.tagWithDefaultNamespace(tag));
            }
            matchItem.tags = withNamespaceTags;
        }

        return matchItem;
    }

    public List<Item> getStdMatchItemLists() {
        int size = matchItemLists.getValue().size();
        List<Item> lists = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            lists.add(getStdMatchItem(i));
        }
        
        return lists;
    }

    public void removeMatchItem(int index) {
        matchItemLists.remove(index);
    }

    public void addPresetItems() {
        JsonArray newMatchLists = new JsonArray();

        Item swordPreset = new Item();
        swordPreset.description = "包含2个或以上冲突附魔的钻石、合金武器";
        swordPreset.name = "*";
        swordPreset.id = "/^minecraft:(?:diamond|netherite)_.*$/";
        swordPreset.tags = List.of("#enchantable/sharp_weapon");
        swordPreset.minEnchantRequir = 2;
        swordPreset.enchantments.put("sharpness", 5);
        swordPreset.enchantments.put("smite", 5);
        swordPreset.enchantments.put("bane_of_arthropods", 5);
        newMatchLists.add(getGson().toJsonTree(swordPreset));

        Item armorPreset = new Item();
        armorPreset.description = "包含3个或以上冲突附魔的钻石、合金盔甲";
        armorPreset.name = "*";
        armorPreset.id = "/^minecraft:(?:diamond|netherite)_.*$/";
        armorPreset.tags.add("#enchantable/armor");
        armorPreset.minEnchantRequir = 3;
        armorPreset.enchantments.put("protection", 4);
        armorPreset.enchantments.put("fire_protection", 4);
        armorPreset.enchantments.put("projectile_protection", 4);
        armorPreset.enchantments.put("blast_protection", 4);
        newMatchLists.add(getGson().toJsonTree(armorPreset));

        Item swordFullPreset = new Item();
        swordFullPreset.description = "三冲突附魔武器";
        swordFullPreset.name = "*";
        swordFullPreset.id = "*";
        swordFullPreset.tags = List.of("#enchantable/sharp_weapon");
        swordFullPreset.minEnchantRequir = -1;
        swordFullPreset.enchantments.put("sharpness", 5);
        swordFullPreset.enchantments.put("smite", 5);
        swordFullPreset.enchantments.put("bane_of_arthropods", 5);
        newMatchLists.add(getGson().toJsonTree(swordFullPreset));

        Item armorFullPreset = new Item();
        armorFullPreset.description = "四冲突附魔盔甲";
        armorFullPreset.name = "*";
        armorFullPreset.id = "*";
        armorFullPreset.tags.add("#enchantable/armor");
        armorFullPreset.minEnchantRequir = -1;
        armorFullPreset.enchantments.put("protection", 4);
        armorFullPreset.enchantments.put("fire_protection", 4);
        armorFullPreset.enchantments.put("projectile_protection", 4);
        armorFullPreset.enchantments.put("blast_protection", 4);
        newMatchLists.add(getGson().toJsonTree(armorFullPreset));

        Item bowPreset = new Item();
        bowPreset.description = "冲突弓";
        bowPreset.name = "*";
        bowPreset.id = "bow";
        bowPreset.minEnchantRequir = -1;
        bowPreset.enchantments.put("infinity", 1);
        bowPreset.enchantments.put("mending", 1);
        newMatchLists.add(getGson().toJsonTree(bowPreset));

        Item paperPreset = new Item();
        paperPreset.description = "点卷、拾玖币、装备兑换卷（无法排除普通纸）";
        paperPreset.name = "*";
        paperPreset.id = "paper";
        newMatchLists.add(getGson().toJsonTree(paperPreset));

        Item diamondPreset = new Item();
        diamondPreset.description = "钻石和钻石块";
        diamondPreset.name = "*";
        diamondPreset.id = "/^minecraft:diamond(?:_block)?$/";
        newMatchLists.add(getGson().toJsonTree(diamondPreset));

        Item netheritePreset = new Item();
        netheritePreset.description = "所有的合金物品";
        netheritePreset.name = "*";
        netheritePreset.id = "/^minecraft:netherite.*$/";
        newMatchLists.add(getGson().toJsonTree(netheritePreset));

        Item fishRodPreset = new Item();
        fishRodPreset.description = "鱼竿";
        fishRodPreset.name = "*";
        fishRodPreset.id = "fishing_rod";
        newMatchLists.add(getGson().toJsonTree(fishRodPreset));

        JsonArray originMatchLists = matchItemLists.getValue();
        newMatchLists.addAll(originMatchLists);
        matchItemLists.setValue(newMatchLists);
    }

    public void setMatchItemEnabled(int index, boolean enabled) {
        JsonObject item = matchItemLists.getValue().get(index).getAsJsonObject();
        item.remove("enabled");
        item.addProperty("enabled", enabled);
    }

    public boolean isMatchItemEnabled(int index) {
        return matchItemLists.getValue().get(index).getAsJsonObject()
            .get("enabled").getAsBoolean();
    }

    public String getMatchItemName(int index) {
        return matchItemLists.getValue().get(index).getAsJsonObject()
            .get("name").getAsString();
    }

    public void setMatchItemName(int index, String name) {
        JsonObject item = matchItemLists.getValue().get(index).getAsJsonObject();
        item.remove("name");
        item.addProperty("name", name);
    }

    public String getMatchItemId(int index) {
        return matchItemLists.getValue().get(index).getAsJsonObject()
            .get("id").getAsString();
    }

    public void setMatchItemId(int index, String id) {
        JsonObject item = matchItemLists.getValue().get(index).getAsJsonObject();
        item.remove("id");
        item.addProperty("id", id);
    }

    public List<String> getMatchItemTags(int index) {
        JsonArray tags = matchItemLists.getValue().get(index).getAsJsonObject()
            .get("tags").getAsJsonArray();
        return jsonArrayToListStr(tags);
    }

    public String getMatchTagsStr(int index) {
        return StringUtil.join(getMatchItemTags(index));
    }

    public void setMatchItemTags(int index, String tagsStr) {
        JsonArray tags = Item.parseTagsJsonArray(tagsStr);
        JsonObject item = matchItemLists.getValue().get(index).getAsJsonObject();
        item.remove("tags");
        item.add("tags", tags);
    }

    public int getMatchItemMinEnchantRequir(int index) {
        return matchItemLists.getValue().get(index).getAsJsonObject()
            .get("minEnchantRequir").getAsInt();
    }

    public void setMatchItemMinEnchantRequir(int index, int minEnchantRequir) {
        JsonObject item = matchItemLists.getValue().get(index).getAsJsonObject();
        item.remove("minEnchantRequir");
        item.addProperty("minEnchantRequir", minEnchantRequir);
    }

    public Map<String, Integer> getMatchItemEnchantments(int index) {
        Map<String, JsonElement> enchantments = matchItemLists.getValue().get(index).getAsJsonObject()
            .get("enchantments").getAsJsonObject().asMap();
        Map<String, Integer> enchantmentsMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry: enchantments.entrySet()) {
            enchantmentsMap.put(entry.getKey(), entry.getValue().getAsInt());
        }
        return enchantmentsMap;
    }

    public List<String> getEnchantList(int index) {
        Map<String, Integer> enchantmentsMap = getMatchItemEnchantments(index);
        return Item.getEnchantmentsListStr(enchantmentsMap);
    } 

    public void setMatchItemEnchantments(int index, List<String> enchantments) {
        Map<String, Integer> enchantmentsMap = Item.parseEnchantments(enchantments);
        JsonObject item = matchItemLists.getValue().get(index).getAsJsonObject();
        item.remove("enchantments");
        item.add("enchantments", getGson().toJsonTree(enchantmentsMap));
    }

    public String getMatchItemDescription(int index) {
        return matchItemLists.getValue().get(index).getAsJsonObject()
            .get("description").getAsString();
    }

    public void setMatchItemDescription(int index, String description) {
        JsonObject item = matchItemLists.getValue().get(index).getAsJsonObject();
        item.remove("description");
        item.addProperty("description", description);
    }
}

class Item {
    public static final Pattern enchantmentPattern = Pattern.compile("^(?:minecraft:)?([^:]+)(?::(\\d+))?$");
    public String description = "匹配组";
    public boolean enabled = true;
    public String name = "";
    public String id = "";
    public List<String> tags = new ArrayList<>();
    public int minEnchantRequir = -1;
    public Map<String, Integer> enchantments = new HashMap<>();

    public Item(boolean enabled, String description, String name, String id, List<String> tags, int minEnchantRequir, Map<String, Integer> enchantments) {
        this.enabled = enabled;
        this.description = description;
        this.name = name;
        this.id = id;
        this.tags = tags;
        this.minEnchantRequir = minEnchantRequir;
        this.enchantments = enchantments;
    }

    public Item() {
    }

    public static List<String> parseTags(String tagsStr) {
        List<String> tags = StringUtil.strToList(tagsStr);
        tags.removeIf(tag -> !tag.equals("*") && !tag.startsWith("#"));
        return tags;
    }

    public static JsonArray parseTagsJsonArray(String tagsStr) {
        List<String> tags = StringUtil.strToList(tagsStr);
        JsonArray jsonArray = new JsonArray();
        for (String tag : tags) {
            if (tag.equals("*") || tag.startsWith("#")) {
                jsonArray.add(tag);
            }
        }
        return jsonArray;
    }

    public static Map<String, Integer> parseEnchantments(List<String> enchantments) {
        Map<String, Integer> enchantmentsMap = new HashMap<>();
        for (String ent: enchantments) {
            Matcher matcher = enchantmentPattern.matcher(ent);
            if (!matcher.find()) {
                AutoDrop.LOGGER.warn("invalidFormat: `{}`", ent);
                continue;
            }
            String name = matcher.group(1);
            String levelStr = matcher.group(2);
            int level = levelStr != null ? Integer.valueOf(levelStr) : 1;

            enchantmentsMap.put(name, level);
        }
        return enchantmentsMap;
    }

    public static List<String> getEnchantmentsListStr(Map<String, Integer> enchantments) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
            list.add(entry.getKey() + ":" + entry.getValue());
        }
        return list;
    }
}

enum ThrowWay {
    ROTATION,
    DIRECTION,
    PLAYER
}

enum Direction {
    UP, DOWN,
    EAST, WEST, NORTH, SOUTH,
    LOOKING
}