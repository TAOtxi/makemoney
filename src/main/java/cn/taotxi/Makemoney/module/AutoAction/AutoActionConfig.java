package cn.taotxi.Makemoney.module.AutoAction;

import java.util.ArrayList;
import java.util.List;

import cn.taotxi.Makemoney.config.BaseConfig;
import net.minecraft.world.inventory.ClickType;

public class AutoActionConfig extends BaseConfig {
    public AutoActionConfig(String moduleName) {
        super(moduleName);
    }

    public List<ActionsConfig> actionConfigList = new ArrayList<>();

    public List<Action> loadActions(String name) {
        for (ActionsConfig config : actionConfigList) {
            if (config.name.equals(name)) {
                List<Action> actions = new ArrayList<>();
                for (ActionConfig actionConfig : config.actions) {
                    actions.add(createActions(actionConfig));
                }
                return actions;
            }
        }
        return null;
    }

    public List<String> getActionNames() {
        List<String> names = new ArrayList<>();
        for (ActionsConfig config : actionConfigList) {
            names.add(config.name);
        }
        return names;
    }

    public void removeAction(String name) {
        actionConfigList.removeIf(config -> config.name.equals(name));
        save();
    }

    public void toConfig(String name, List<Action> actions) {
        actionConfigList.removeIf(config -> config.name.equals(name));

        ActionsConfig config = new ActionsConfig();
        config.name = name;
        config.actions = new ArrayList<>();
        for (Action action : actions) {
            config.actions.add(createActionConfig(action));
        }
        actionConfigList.add(config);
        save();
    }

    public ActionConfig createActionConfig(Action action) {
        ActionConfig config = new ActionConfig();
        config.type = action.getClass().getSimpleName();
        config.delay = action.delay;
        if (action instanceof CommandAction cmd) {
            config.data = cmd.command;
        } else if (action instanceof ClickAction click) {
            config.data = click.slot + ", " + click.clickType.name();
        } else {
            config.data = "";
        }
        return config;
    }

    public Action createActions(ActionConfig config) {
        if (config.type.equals(CommandAction.class.getSimpleName())) {
            return new CommandAction(config.data, config.delay);

        } else if (config.type.equals(ClickAction.class.getSimpleName())) {
            String[] clickData = config.data.replace(" ", "").split(",");
            int slot = Integer.parseInt(clickData[0]);
            ClickType clickType = ClickType.valueOf(clickData[1]);
            return new ClickAction(slot, clickType, config.delay);

        } else if (config.type.equals(LoopAction.class.getSimpleName())) {
            return new LoopAction();

        } else if (config.type.equals(CutAction.class.getSimpleName())) {
            return new CutAction();
        }
        return null;

    }

    public static String getDefaultConfigVersion() {
        return "0.0.1";
    }
}

class ActionsConfig {
    public String name;
    public List<ActionConfig> actions;
}

class ActionConfig {
    public String type;
    public String data;
    public int delay;
}
