package cn.taotxi.Makemoney.module.NineteenWorld;

import cn.taotxi.Makemoney.config.ConfigManager;
import cn.taotxi.Makemoney.config.type.ConfigArray;
import cn.taotxi.Makemoney.config.type.ConfigBoolean;
import cn.taotxi.Makemoney.config.type.ConfigInteger;
import cn.taotxi.Makemoney.config.type.ConfigString;

public class NineteenWorldConfig extends ConfigManager {
    private static NineteenWorldConfig instance = null;

    public static NineteenWorldConfig getInstance() {
        if (instance == null) {
            instance = new NineteenWorldConfig(NineteenWorld.MODULE_NAME);
        }
        return instance;
    }

    public NineteenWorldConfig(String moduleName) {
        super(moduleName);
    }

    /******************* AutoRide *******************/
    public final ConfigString   autoRideTargetPlayer         = new ConfigString("autoRideTargetPlayer", "", "需要黏住的目标目标玩家", this);
    public final ConfigInteger  autoRideRunInterval          = new ConfigInteger("autoRideRunInterval", 5, "检测周期", this);
    public final ConfigBoolean  autoRideEnableShakeOffPlayer = new ConfigBoolean("autoRideEnableShakeOffPlayer", false, "是否启用光滑的头顶", this);

    /******************* Ignore *******************/
    public final ConfigBoolean ignoreEnabled                 = new ConfigBoolean("ignoreEnabled", false, "是否启用屏蔽消息功能", this);
    public final ConfigArray<String>   ignoreList            = new ConfigArray<String>("ignoreList", "屏蔽规则", this, String.class);

    /******************* RightClickRide *******************/
    public final ConfigBoolean rightClickRideEnabled         = new ConfigBoolean("rightClickRideEnabled", true, "是否启用右键骑乘功能", this);
    
    public final ConfigBoolean rightClickOpenShulkerBoxEnabled = new ConfigBoolean("rightClickOpenShulkerBoxEnabled", false, "是否启用右键打开潜影盒功能", this);

    public final ConfigBoolean autorespawnEnabled            = new ConfigBoolean("autorespawnEnabled", false, "死亡后是否自动重生", this);
    public final ConfigBoolean runCommandWhenRespawn         = new ConfigBoolean("runCommandWhenRespawn", false, "重生后是否执行命令", this);
    public final ConfigString  commandWhenRespawnToRun       = new ConfigString("commandWhenRespawnToRun", "/back", "重生后自动执行的命令", this);
}
