package cn.taotxi.Makemoney.config;

import cn.taotxi.Makemoney.Makemoney;
import cn.taotxi.Makemoney.config.type.ConfigBoolean;
import cn.taotxi.Makemoney.config.type.ConfigString;


public class MakemoneyConfig extends ConfigManager {
    private static MakemoneyConfig instance = null;
    
    public static MakemoneyConfig getInstance() {
        if (instance == null) {
            instance = new MakemoneyConfig(Makemoney.MOD_ID);
        }
        return instance;
    }

    public MakemoneyConfig(String moduleName) {
        super(moduleName);
    }

    /******************* 更新检查 *******************/
    public final ConfigBoolean updateNotifyEnabled        = new ConfigBoolean("updateNotifyEnabled", false, "检测到模组更新后是否通知", this);
    /** 已被“不再通知”忽略掉的最新版本号，为空表示没有忽略过任何版本 */
    public final ConfigString  updateNotifyIgnoredVersion = new ConfigString("updateNotifyIgnoredVersion", "", "已忽略通知的版本号", this);
}
