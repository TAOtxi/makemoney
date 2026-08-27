package cn.taotxi.Makemoney.util.help;

/**
 * 帮助菜单中的一个条目。
 *
 * @param usage       指令用法。不以 / 开头时会自动拼接所属模块的根指令，
 *                    以 / 开头则视为绝对指令，原样展示。
 *                    为空字符串时表示模块根指令本身。
 * @param descKey     描述文本的翻译 key（不含 makemoney. 前缀）
 * @param clickMode   点击该行时的行为
 */
public record HelpEntry(String usage, String descKey, ClickMode clickMode) {

    public enum ClickMode {
        /** 点击后把指令填入聊天输入框，由玩家自行确认执行 */
        SUGGEST,
        /** 点击后立即执行指令，仅用于无副作用的指令 */
        RUN,
        /** 不可点击，用于需要玩家自行填写参数的说明行 */
        NONE
    }

    /** 默认使用 SUGGEST，避免误触发有副作用的指令 */
    public static HelpEntry of(String usage, String descKey) {
        return new HelpEntry(usage, descKey, ClickMode.SUGGEST);
    }

    /** 点击立即执行，仅用于打开界面、查看信息一类的安全指令 */
    public static HelpEntry run(String usage, String descKey) {
        return new HelpEntry(usage, descKey, ClickMode.RUN);
    }

    /** 纯说明行，不可点击 */
    public static HelpEntry text(String usage, String descKey) {
        return new HelpEntry(usage, descKey, ClickMode.NONE);
    }

    /** 该条目是否为绝对指令（跨模块跳转） */
    public boolean isAbsolute() {
        return usage.startsWith("/");
    }
}
