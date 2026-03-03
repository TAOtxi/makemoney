package cn.taotxi.Makemoney.module.AutoAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.taotxi.Makemoney.config.BaseConfig;
import net.minecraft.world.inventory.ClickType;

public class AutoActionConfig extends BaseConfig {
    public AutoActionConfig(String moduleName) {
        super(moduleName);
    }

    private Map<String, List<Map<String, String>>> actionConfigList;

    public List<Action> loadActions(String name) {
        List<Map<String, String>> actionsConfig = actionConfigList.get(name);
        if (actionsConfig == null) {
            return null;
        }
        List<Action> actions = new ArrayList<>();
        for (Map<String, String> actionConfig : actionsConfig) {
            actions.add(configToAction(actionConfig));
        }
        return actions;
    }

    public List<String> getActionNames() {
        return new ArrayList<>(actionConfigList.keySet());
    }

    public void removeAction(String name) {
        var result = actionConfigList.remove(name);
        if (result != null) {
            save();
        }
    }

    public void toConfig(String name, List<Action> actions) {
        List<Map<String, String>> actionsConfig = new ArrayList<>();
        for (Action action : actions) {
            actionsConfig.add(actionToConfig(action));
        }
        actionConfigList.put(name, actionsConfig);
        save();
    }

    public Map<String, String> actionToConfig(Action action) {
        Map<String, String> data = new HashMap<>();
        data.put("type", action.getClass().getSimpleName());
        data.put("delay", String.valueOf(action.delay));
        if (action instanceof CommandAction cmd) {
            data.put("cmd", cmd.command);
        } else if (action instanceof ClickAction click) {
            data.put("slot", String.valueOf(click.slot));
            data.put("clickType", click.clickType.name());
        }
        return data;
    }

    public Action configToAction(Map<String, String> config) {
        if (config.get("type").equals(CommandAction.class.getSimpleName())) {
            return new CommandAction(config.get("cmd"), Integer.parseInt(config.get("delay")));

        } else if (config.get("type").equals(ClickAction.class.getSimpleName())) {
            int slot = Integer.parseInt(config.get("slot"));
            ClickType clickType = ClickType.valueOf(config.get("clickType"));
            return new ClickAction(slot, clickType, Integer.parseInt(config.get("delay")));

        } else if (config.get("type").equals(LoopAction.class.getSimpleName())) {
            return new LoopAction();

        } else if (config.get("type").equals(CutAction.class.getSimpleName())) {
            return new CutAction();
        }
        return null;

    }

    public static String getDefaultConfigVersion() {
        return "0.0.2";
    }
}