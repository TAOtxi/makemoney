package cn.taotxi.Makemoney.module.StrangeFunction;

public class StrangeFunctionInit {
    public static final String MODULE_NAME = "strangefunction";

    public static void initialize() {
        StrangeConfig.getInstance().loadConfig();
        AutoRide.initialize();
        IgnoreMessage.initialize();
    }
}
