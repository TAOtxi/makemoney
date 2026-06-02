package cn.taotxi.Makemoney.module.StrangeFunction;

import com.google.gson.JsonArray;

import cn.taotxi.Makemoney.config.ConfigManager;
import cn.taotxi.Makemoney.config.type.ConfigArray;
import cn.taotxi.Makemoney.config.type.ConfigBoolean;
import cn.taotxi.Makemoney.config.type.ConfigFloat;
import cn.taotxi.Makemoney.config.type.ConfigInteger;
import cn.taotxi.Makemoney.config.type.ConfigString;

public class StrangeConfig extends ConfigManager {
    private static StrangeConfig instance = null;

    public static StrangeConfig getInstance() {
        if (instance == null) {
            instance = new StrangeConfig(StrangeFunctionInit.MODULE_NAME);
        }
        return instance;
    }

    public StrangeConfig(String moduleName) {
        super(moduleName);
    }

    /******************* AutoRide *******************/
    public ConfigString   autoRideTargetPlayer         = new ConfigString("autoRideTargetPlayer", "", "需要黏住的目标目标玩家", this);
    public ConfigInteger  autoRideRunInterval          = new ConfigInteger("autoRideRunInterval", 5, "检测周期", this);
    public ConfigBoolean  autoRideEnableShakeOffPlayer = new ConfigBoolean("autoRideEnableShakeOffPlayer", false, "是否启用光滑的头顶", this);
    public ConfigFloat    autoRideMinDistance          = new ConfigFloat("autoRideMinDistance", 6, "检测范围最小距离", this);

    /******************* Ignore *******************/
    public ConfigBoolean ignoreEnabled                 = new ConfigBoolean("ignoreEnabled", false, "是否启用屏蔽消息功能", this);
    public ConfigArray   ignoreList                    = new ConfigArray("ignoreList", new JsonArray(), "屏蔽规则", this);

    /******************* RightClickRide *******************/
    public ConfigBoolean rightClickRideEnabled         = new ConfigBoolean("rightClickRideEnabled", true, "是否启用右键骑乘功能", this);
}
