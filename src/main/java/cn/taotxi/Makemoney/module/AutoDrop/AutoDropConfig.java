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

import cn.taotxi.Makemoney.config.ConfigMaker;
import cn.taotxi.Makemoney.config.ConfigManager;
import cn.taotxi.Makemoney.util.StringUtil;

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

public class AutoDropConfig extends ConfigManager {
    private static AutoDropConfig instance = null;

    public static AutoDropConfig getInstance() {
        if (instance == null) {
            instance = new AutoDropConfig("autodrop");
        }
        return instance;
    }

    public AutoDropConfig(String moduleName) {
        super(moduleName);
    }

    @Override
    protected JsonObject createDefaultConfig() {
        AutoDropDefaultConfig defaultConfig = new AutoDropDefaultConfig();
        return ConfigMaker.gson.toJsonTree(defaultConfig).getAsJsonObject();
    }

    public AutoDropDefaultConfig getDefaultConfig() {
        return getGson().fromJson(defaultConfig, AutoDropDefaultConfig.class);
    }

    public boolean isShowAttentionMsg() {
        return getInstance().getBoolean("showAttentionMsg");
    }

    public void setShowAttentionMsg(boolean showAttentionMsg) {
        getInstance().setBoolean("showAttentionMsg", showAttentionMsg);
    }

    public List<Integer> getIgnoreSlots() {
        return getInstance().getIntList("ignoreSlots");
    }

    public void setIgnoreSlots(String slots) {
        setIgnoreSlots(StringUtil.strToIntList(slots));
    }

    public void setIgnoreSlots(List<Integer> ignoreSlots) {
        JsonArray jsonArray = new JsonArray();
        ignoreSlots.forEach(jsonArray::add);
        getInstance().set("ignoreSlots", jsonArray);
    }

    public ThrowWay getThrowWay() {
        return ThrowWay.valueOf(getInstance().getString("throwWay").toUpperCase());
    }

    public void setThrowWay(ThrowWay throwWay) {
        getInstance().setString("throwWay", throwWay.name());
    }

    public void setThrowWay(String throwWay) {
        getInstance().setString("throwWay", throwWay);
    }

    public Direction getThrowDirection() {
        return Direction.valueOf(getInstance().getString("throwDirection").toUpperCase());
    }

    public static List<String> getAllThrowDirections() {
        return Arrays.stream(Direction.values()).map(Enum::name).collect(Collectors.toList());
    }

    public void setThrowDirection(Direction throwDirection) {
        getInstance().setString("throwDirection", throwDirection.name());
    }

    public void setThrowDirection(String throwDirection) {
        getInstance().setString("throwDirection", throwDirection);
    }

    public float getThrowYaw() {
        return getInstance().getFloat("throwYaw");
    }

    public void setThrowYaw(float yaw) {
        getInstance().setFloat("throwYaw", yaw);
    }

    public float getThrowPitch() {
        return getInstance().getFloat("throwPitch");
    }

    public void setThrowPitch(float pitch) {
        getInstance().setFloat("throwPitch", pitch);
    }

    public boolean isTimeTrigger() {
        return getInstance().getBoolean("timeTrigger");
    }

    public void setTimeTrigger(boolean timeTrigger) {
        getInstance().setBoolean("timeTrigger", timeTrigger);
       }

    public int getTimeTriggerInterval() {
        return getInstance().getInt("timeTriggerInterval");
    }

    public void setTimeTriggerInterval(int interval) {
        getInstance().setInt("timeTriggerInterval", interval);
    }

    public boolean isPickUpItemTrigger() {
        return getInstance().getBoolean("pickUpItemTrigger");
    }

    public void setPickUpItemTrigger(boolean pickUpItemTrigger) {
        getInstance().setBoolean("pickUpItemTrigger", pickUpItemTrigger);
    }

    public String getTriggerItemId() {
        return getInstance().getString("triggerItemId");
    }

    public void setTriggerItemId(String triggerItemId) {
        getInstance().setString("triggerItemId", triggerItemId);
    }

    public boolean isTurnOffWhenChangeWorld() {
        return getInstance().getBoolean("turnOffWhenChangeWorld");
    }

    public void setTurnOffWhenChangeWorld(boolean turnOffWhenChangeWorld) {
        getInstance().setBoolean("turnOffWhenChangeWorld", turnOffWhenChangeWorld);
    }

    public int getTriggerMinCount() {
        return getInstance().getInt("triggerMinCount");
    }

    public void setTriggerMinCount(int minCount) {
        getInstance().setInt("triggerMinCount", minCount);
    }

    public boolean isStopWhenCrouch() {
        return getInstance().getBoolean("stopWhenCrouch");
    }

    public void setStopWhenCrouch(boolean stopWhenCrouch) {
        getInstance().setBoolean("stopWhenCrouch", stopWhenCrouch);
    }

    public boolean isStopWhenOpenContainer() {
        return getInstance().getBoolean("stopWhenOpenContainer");
    }

    public void setStopWhenOpenContainer(boolean stopWhenOpenContainer) {
        getInstance().setBoolean("stopWhenOpenContainer", stopWhenOpenContainer);
    }

    public boolean isStopWhenOpenConfigGui() {
        return getInstance().getBoolean("stopWhenOpenConfigGui");
    }

    public void setStopWhenOpenConfigGui(boolean stopWhenOpenConfigGui) {
        getInstance().setBoolean("stopWhenOpenConfigGui", stopWhenOpenConfigGui);
    }

    public boolean isStopWhenNotHoldingItem() {
        return getInstance().getBoolean("stopWhenNotHoldingItem");
    }

    public void setStopWhenNotHoldingItem(boolean stopWhenNotHoldingItem) {
        getInstance().setBoolean("stopWhenNotHoldingItem", stopWhenNotHoldingItem);
    }

    public String getStopWhenNotHoldingItemName() {
        return getInstance().getString("stopWhenNotHoldingItemName");
    }

    public void setStopWhenNotHoldingItemName(String name) {
        getInstance().setString("stopWhenNotHoldingItemName", name);
    }

    public String getStopWhenNotHoldingItemId() {
        return getInstance().getString("stopWhenNotHoldingItemId");
    }

    public void setStopWhenNotHoldingItemId(String id) {
        getInstance().setString("stopWhenNotHoldingItemId", id);
    }

    public List<Item> getMatchLists() {
        JsonArray jsonArray = getMatchListsJsonArray();
        return jsonToList(jsonArray, Item.class);
    }

    public JsonArray getMatchListsJsonArray() {
        return getInstance().getJsonArray("matchLists");
    }

    public void addMatchItem() {
        getMatchListsJsonArray().add(getGson().toJsonTree(new Item()));
    }

    public void removeMatchItem(int index) {
        getMatchListsJsonArray().remove(index);
    }

    public void setMatchItemEnabled(int index, boolean enabled) {
        JsonObject item = getMatchListsJsonArray().get(index).getAsJsonObject();
        item.remove("enabled");
        item.addProperty("enabled", enabled);
    }

    public boolean isMatchItemEnabled(int index) {
        return getMatchListsJsonArray().get(index).getAsJsonObject()
            .get("enabled").getAsBoolean();
    }

    public String getMatchItemName(int index) {
        return getMatchListsJsonArray().get(index).getAsJsonObject()
            .get("name").getAsString();
    }

    public void setMatchItemName(int index, String name) {
        JsonObject item = getMatchListsJsonArray().get(index).getAsJsonObject();
        item.remove("name");
        item.addProperty("name", name);
    }

    public String getMatchItemId(int index) {
        return getMatchListsJsonArray().get(index).getAsJsonObject()
            .get("id").getAsString();
    }

    public void setMatchItemId(int index, String id) {
        JsonObject item = getMatchListsJsonArray().get(index).getAsJsonObject();
        item.remove("id");
        item.addProperty("id", id);
    }

    public List<String> getMatchItemTags(int index) {
        JsonArray tags = getMatchListsJsonArray().get(index).getAsJsonObject()
            .get("tags").getAsJsonArray();
        return jsonArrayToListStr(tags);
    }

    public String getMatchTagsStr(int index) {
        return StringUtil.join(getMatchItemTags(index));
    }

    public void setMatchItemTags(int index, String tagsStr) {
        JsonArray tags = Item.parseTagsJsonArray(tagsStr);
        JsonObject item = getMatchListsJsonArray().get(index).getAsJsonObject();
        item.remove("tags");
        item.add("tags", tags);
    }

    public int getMatchItemMinEnchantRequir(int index) {
        return getMatchListsJsonArray().get(index).getAsJsonObject()
            .get("minEnchantRequir").getAsInt();
    }

    public void setMatchItemMinEnchantRequir(int index, int minEnchantRequir) {
        JsonObject item = getMatchListsJsonArray().get(index).getAsJsonObject();
        item.remove("minEnchantRequir");
        item.addProperty("minEnchantRequir", minEnchantRequir);
    }

    public Map<String, Integer> getMatchItemEnchantments(int index) {
        Map<String, JsonElement> enchantments = getMatchListsJsonArray().get(index).getAsJsonObject()
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
        JsonObject item = getMatchListsJsonArray().get(index).getAsJsonObject();
        item.remove("enchantments");
        item.add("enchantments", getGson().toJsonTree(enchantmentsMap));
    }

    public String getMatchItemDescription(int index) {
        return getMatchListsJsonArray().get(index).getAsJsonObject()
            .get("description").getAsString();
    }

    public void setMatchItemDescription(int index, String description) {
        JsonObject item = getMatchListsJsonArray().get(index).getAsJsonObject();
        item.remove("description");
        item.addProperty("description", description);
    }

    public void cleanMatchLists() {
        getInstance().set("matchLists", new JsonArray());
    }

    public void addPresetItems() {
        JsonArray jsonArray = getMatchListsJsonArray();

        Item swordPreset = new Item();
        swordPreset.description = "包含2个或以上冲突附魔的钻石、合金武器";
        swordPreset.name = "*";
        swordPreset.id = "/^minecraft:(?:diamond|netherite)_.*$/";
        swordPreset.tags = List.of("#enchantable/sharp_weapon");
        swordPreset.minEnchantRequir = 2;
        swordPreset.enchantments.put("sharpness", 5);
        swordPreset.enchantments.put("smite", 5);
        swordPreset.enchantments.put("bane_of_arthropods", 5);
        jsonArray.add(getGson().toJsonTree(swordPreset));

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
        jsonArray.add(getGson().toJsonTree(armorPreset));

        Item swordFullPreset = new Item();
        swordFullPreset.description = "三冲突附魔武器";
        swordFullPreset.name = "*";
        swordFullPreset.id = "*";
        swordFullPreset.tags = List.of("#enchantable/sharp_weapon");
        swordFullPreset.minEnchantRequir = -1;
        swordFullPreset.enchantments.put("sharpness", 5);
        swordFullPreset.enchantments.put("smite", 5);
        swordFullPreset.enchantments.put("bane_of_arthropods", 5);
        jsonArray.add(getGson().toJsonTree(swordFullPreset));

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
        jsonArray.add(getGson().toJsonTree(armorFullPreset));

        Item bowPreset = new Item();
        bowPreset.description = "冲突弓";
        bowPreset.name = "*";
        bowPreset.id = "bow";
        bowPreset.minEnchantRequir = -1;
        bowPreset.enchantments.put("infinity", 1);
        bowPreset.enchantments.put("mending", 1);
        jsonArray.add(getGson().toJsonTree(bowPreset));

        Item paperPreset = new Item();
        paperPreset.description = "匹配点卷、拾玖币、装备兑换卷（无法排除普通纸）";
        paperPreset.name = "*";
        paperPreset.id = "paper";
        jsonArray.add(getGson().toJsonTree(paperPreset));

        Item diamondPreset = new Item();
        diamondPreset.description = "钻石和钻石块";
        diamondPreset.name = "*";
        diamondPreset.id = "/^minecraft:diamond(?:_block)?$/";
        jsonArray.add(getGson().toJsonTree(diamondPreset));

        Item netheritePreset = new Item();
        netheritePreset.description = "所有的合金物品";
        netheritePreset.name = "*";
        netheritePreset.id = "/^minecraft:netherite.*$/";
        jsonArray.add(getGson().toJsonTree(netheritePreset));

        Item fishRodPreset = new Item();
        fishRodPreset.description = "鱼竿";
        fishRodPreset.name = "*";
        fishRodPreset.id = "fishing_rod";
        jsonArray.add(getGson().toJsonTree(fishRodPreset));
    }

    public static Item getDefaultMatchItem() {
        return new Item();
    }

    public class AutoDropDefaultConfig {
        public boolean showAttentionMsg = true;
        public List<Integer> ignoreSlots = new ArrayList<>();
        public String throwWay = ThrowWay.DIRECTION.name();
        public String throwDirection = Direction.LOOKING.name();
        public float throwYaw = 0.0f;
        public float throwPitch = 0.0f;

        public boolean timeTrigger = true;
        public int timeTriggerInterval = 20 * 20; // 20s
        public boolean pickUpItemTrigger = false;
        public String triggerItemId = "";

        public boolean turnOffWhenChangeWorld = true;
        public int triggerMinCount = 0;
        public boolean stopWhenCrouch = true;
        public boolean stopWhenOpenContainer = true;
        public boolean stopWhenOpenConfigGui = true;

        public boolean stopWhenNotHoldingItem = false;
        public String stopWhenNotHoldingItemName = "";
        public String stopWhenNotHoldingItemId = "";

        public List<Item> matchLists = new ArrayList<>();
    }

    static class Item {
        public static final Pattern enchantmentPattern = Pattern.compile("^(?:minecraft:)?([^:]+)(?::(\\d+))?$");
        public String description = "匹配组";
        public boolean enabled = true;
        public String name = "";
        public String id = "";
        public List<String> tags = new ArrayList<>();
        public int minEnchantRequir = -1;
        public Map<String, Integer> enchantments = new HashMap<>();

        public List<String> getEnchantList() {
            List<String> list = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                list.add(entry.getKey() + ":" + entry.getValue());
            }
            return list;
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
}