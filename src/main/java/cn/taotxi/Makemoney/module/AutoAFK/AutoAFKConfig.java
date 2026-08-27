package cn.taotxi.Makemoney.module.AutoAFK;

import java.util.List;

import com.google.gson.JsonObject;

import cn.taotxi.Makemoney.config.ConfigManager;
import cn.taotxi.Makemoney.config.type.ConfigArray;
import cn.taotxi.Makemoney.config.type.ConfigBoolean;
import cn.taotxi.Makemoney.config.type.ConfigInteger;
import cn.taotxi.Makemoney.config.type.ConfigString;
import cn.taotxi.Makemoney.util.StringUtil;


public class AutoAFKConfig extends ConfigManager {
    private static AutoAFKConfig instance = null;
    
    public static AutoAFKConfig getInstance() {
        if (instance == null) {
            instance = new AutoAFKConfig(AutoAFK.MODULE_NAME);
        }
        return instance;
    }

    public AutoAFKConfig(String moduleName) {
        super(moduleName);
    }

    public final ConfigBoolean  autoAttackEnabled    = new ConfigBoolean("autoAttackEnabled", false, "是否启用自动攻击", this);
    public final ConfigInteger  attackInterval       = new ConfigInteger("attackInterval", 11, "攻击间隔", this);
    public final ConfigBoolean  durabilityCheck      = new ConfigBoolean("durabilityCheck", true, "防止工具损坏", this);
    public final ConfigBoolean  showInfo             = new ConfigBoolean("showInfo", false, "是否显示攻击周期", this);
    public final ConfigBoolean  attackMode           = new ConfigBoolean("attackMode", false, "是否为白名单模式", this);
    public final ConfigArray<String> attackList      = new ConfigArray<>("attackList",  List.of("player"), "攻击列表", this, String.class);

    public final ConfigBoolean  tpsCheckEnabled      = new ConfigBoolean("tpsCheckEnabled", false, "是否启用tps检查", this);
    public final ConfigInteger  safetyTpsThreshold   = new ConfigInteger("tpsThreshold", 8, "tps安全阈值", this);
    public final ConfigInteger  greenTpsThreshold    = new ConfigInteger("greenTpsThreshold", 16, "tps绿色阈值", this);
    public final ConfigString   triggerCommand       = new ConfigString("triggerCommand", "/spawn", "低于阈值时触发的命令", this);
    public final ConfigString   greenTriggerCommand  = new ConfigString("greenTriggerCommand", "/back", "绿色阈值时触发的命令", this);

    public final ConfigBoolean positionCheckEnabled  = new ConfigBoolean("positionCheckEnabled", false, "是否启用位置检查", this);
    public final ConfigInteger positionCheckInterval = new ConfigInteger("positionCheckInterval", 20 * 10, "位置检测周期（tick）", this);
    public final ConfigArray<PositionCheckItem> positionCheckItems = new ConfigArray<>("positionCheckItems", "位置检查项目列表", this, PositionCheckItem.class);
    

    public class PositionCheckItem {
        private boolean enabled = false;
        private String type = "ball";    // ball | cuboid
        private String world = "minecraft:overworld";
        private float x1 = 0;
        private float y1 = 0;
        private float z1 = 0;
        private float x2 = 0;
        private float y2 = 0;
        private float z2 = 0;
        private float radius = 5;
        private boolean isInner = true;
        private String triggerCmd = "";

        public PositionCheckItem() {
            // 默认构造函数
        }

        public PositionCheckItem(boolean enabled, String world, float x, float y, float z, float r, boolean isInner, String triggerCmd) {
            this.enabled = enabled;
            this.world = world;
            this.x1 = x;
            this.y1 = y;
            this.z1 = z;
            this.radius = r;
            this.isInner = isInner;
            this.triggerCmd = triggerCmd;
        }

        public PositionCheckItem(boolean enabled, String world, float x1, float y1, float z1, float x2, float y2, float z2, boolean isInner, String triggerCmd) {
            this.enabled = enabled;
            this.world = world;
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
            this.isInner = isInner;
            this.triggerCmd = triggerCmd;
        }

        public boolean isInSameWorld(String world) {
            if (this.world.equals("*")) {
                return true;
            }
            return this.world.equals(world);
        }

        public boolean isInArea(double px, double py, double pz) {
            if (type.equals("ball")) {
                return isInner == isInBallArea(px, py, pz);
            }
            return isInner == isInCuboidArea(px, py, pz);
        }
        
        private boolean isInBallArea(double px, double py, double pz) {
            double dx = px - x1;
            double dz = pz - z1;
            double dy = py - y1;
            double radiusSquared = radius * radius;
            double distance = dx * dx + dz * dz + dy * dy;

            return distance <= radiusSquared;
        }

        private boolean isInCuboidArea(double px, double py, double pz) {
            return
                px >= Math.min(x1, x2) &&
                px <= Math.max(x1, x2) &&
                py >= Math.min(y1, y2) &&
                py <= Math.max(y1, y2) &&
                pz >= Math.min(z1, z2) &&
                pz <= Math.max(z1, z2);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getCommand() {
            return triggerCmd;
        }

        public String getWorld() {
            return world;
        }

        public String getType() {
            return type;
        }

        public float getRadius() {
            return radius;
        }

        public String getPosition1() {
            return StringUtil.posToString(
                List.of(x1, y1, z1)
            );
        }

        public String getPosition2() {
            return StringUtil.posToString(
                List.of(x2, y2, z2)
            );
        }

        public boolean isInner() {
            return isInner;
        }
    }
    
    public PositionCheckItem getDefaultPositionCheckItem() {
        return new PositionCheckItem();
    }

    public void addPositionCheckItem() {
        addPositionCheckItem(getDefaultPositionCheckItem());
    }

    public void addPositionCheckItem(PositionCheckItem item) {
        positionCheckItems.addTop(item);
    }

    public void removePositionCheckItem(int index) {
        positionCheckItems.remove(index);
    }

    public List<PositionCheckItem> getPositionCheckItemLists() {
        return positionCheckItems.getValueAsList();
    }

    public boolean getPositionCheckItemEnabled(int index) {
        return positionCheckItems.getValueAsObject(index).get("enabled").getAsBoolean();
    }

    public void setPositionCheckItemEnabled(int index, boolean enabled) {
        JsonObject item = positionCheckItems.getValueAsObject(index);
        item.remove("enabled");
        item.addProperty("enabled", enabled);
    }

    public String getPositionCheckItemType(int index) {
        return positionCheckItems.getValueAsObject(index).get("type").getAsString();
    }

    public void setPositionCheckItemType(int index, String type) {
        JsonObject item = positionCheckItems.getValueAsObject(index);
        item.remove("type");
        item.addProperty("type", type);
    }

    public String getPositionCheckItemTriggerCmd(int index) {
        return positionCheckItems.getValueAsObject(index).get("triggerCmd").getAsString();
    }

    public void setPositionCheckItemTriggerCmd(int index, String triggerCmd) {
        JsonObject item = positionCheckItems.getValueAsObject(index);
        item.remove("triggerCmd");
        item.addProperty("triggerCmd", triggerCmd);
    }

    public String getPositionCheckItemPosition(int index, int p) {
        JsonObject item = positionCheckItems.getValueAsObject(index);
        return StringUtil.posToString(
            List.of(
                item.get("x" + p).getAsFloat(),
                item.get("y" + p).getAsFloat(),
                item.get("z" + p).getAsFloat()
            )
        );
    }

    public void setPositionCheckItemPosition(int index, int p, String position) {
        List<Float> coords = StringUtil.parseFloatPos(position);
        if (coords.size() < 3) return;

        setPositionCheckItemPosition(index, p, coords.get(0), coords.get(1), coords.get(2));
    }

    public void setPositionCheckItemPosition(int index, int p, float x, float y, float z) {
        JsonObject item = positionCheckItems.getValueAsObject(index);
        item.remove("x" + p);
        item.remove("y" + p);
        item.remove("z" + p);
        item.addProperty("x" + p, x);
        item.addProperty("y" + p, y);
        item.addProperty("z" + p, z);
    }

    public float getPositionCheckItemRadius(int index) {
        return positionCheckItems
            .getValueAsObject(index)
            .get("radius")
            .getAsFloat();
    }

    public void setPositionCheckItemRadius(int index, float radius) {
        JsonObject item = positionCheckItems.getValueAsObject(index);
        item.remove("radius");
        item.addProperty("radius", radius);
    }

    public boolean getPositionCheckItemIsInner(int index) {
        return positionCheckItems
            .getValueAsObject(index)
            .get("isInner")
            .getAsBoolean();
    }

    public void setPositionCheckItemIsInner(int index, boolean isInner) {
        JsonObject item = positionCheckItems.getValueAsObject(index);
        item.remove("isInner");
        item.addProperty("isInner", isInner);
    }

    public void setPositionCheckItemWorld(int index, String world) {
        JsonObject item = positionCheckItems.getValueAsObject(index);
        item.remove("world");
        item.addProperty("world", world);
    }

    public String getPositionCheckItemWorld(int index) {
        return positionCheckItems
            .getValueAsObject(index)
            .get("world")
            .getAsString();
    }
}


