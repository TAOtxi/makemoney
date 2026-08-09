package cn.taotxi.Makemoney.module.MenuClick;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cn.taotxi.Makemoney.config.ConfigManager;
import cn.taotxi.Makemoney.config.type.ConfigArray;
import net.minecraft.world.inventory.ContainerInput;

public class MenuClickConfig extends ConfigManager {
    private static MenuClickConfig instance = null;

    public static MenuClickConfig getInstance() {
        if (instance == null) {
            instance = new MenuClickConfig(MenuClick.MODULE_NAME);
        }
        return instance;
    }

    public MenuClickConfig(String moduleName) {
        super(moduleName);
    }

    public final ConfigArray<MenuClickTask> tasks = new ConfigArray<>("tasks", "任务", this, MenuClickTask.class);

    public void addClickTask() {
        MenuClickTask task = new MenuClickTask("TASK_" + System.currentTimeMillis());
        tasks.addTop(task);
    }

    public List<String> getTaskNameList() {
        List<String> nameList = new ArrayList<>();
        for (JsonElement element : tasks.getValue()) {
            JsonObject task = element.getAsJsonObject();
            nameList.add(task.get("name").getAsString());
        }
        return nameList;
       }

    public MenuClickTask getTask(String name) {
        for (JsonElement element : tasks.getValue()) {
            JsonObject task = element.getAsJsonObject();
            if (task.get("name").getAsString().equals(name)) {
                return MenuClickConfig.getGson().fromJson(task, MenuClickTask.class);
            }
        }
        return null;
    }

    public String getTaskName(int index) {
        return tasks.getRaw(index).getAsJsonObject().get("name").getAsString();
    }

    public void setTaskName(int index, String name) {
        JsonObject task = tasks.getRaw(index).getAsJsonObject();
        task.remove("name");
        task.addProperty("name", name);
    }

    public int getTaskStartDelay(int index) {
        return tasks.getRaw(index).getAsJsonObject().get("startDelay").getAsInt();
    }



    public void setTaskStartDelay(int index, int delay) {
        JsonObject task = tasks.getRaw(index).getAsJsonObject();
        task.remove("startDelay");
        task.addProperty("startDelay", delay);
    }

    public int getTaskDelay(int index) {
        return tasks.getRaw(index).getAsJsonObject().get("delay").getAsInt();
    }

    public void setTaskDelay(int index, int delay) {
        JsonObject task = tasks.getRaw(index).getAsJsonObject();
        task.remove("delay");
        task.addProperty("delay", delay);
    }

    public String getTaskDescription(int index) {
        return tasks.getRaw(index).getAsJsonObject().get("description").getAsString();
    }

    public void setTaskDescription(int index, String description) {
        JsonObject task = tasks.getRaw(index).getAsJsonObject();
        task.remove("description");
        task.addProperty("description", description);
    }

    public boolean getTaskIsLoop(int index) {
        return tasks.getRaw(index).getAsJsonObject().get("isLoop").getAsBoolean();
    }

    public void setTaskIsLoop(int index, boolean isLoop) {
        JsonObject task = tasks.getRaw(index).getAsJsonObject();
        task.remove("isLoop");
        task.addProperty("isLoop", isLoop);
    }

    public List<String> getTaskActions(int index) {
        JsonArray actions = tasks.getRaw(index).getAsJsonObject().get("actions").getAsJsonArray();
        List<String> actionList = MenuClickConfig.jsonArrayToListStr(actions);
        return actionList;
    }

    public void setTaskActions(int index, List<String> actions) {
        JsonObject task = tasks.getRaw(index).getAsJsonObject();
        task.remove("actions");
        
        JsonArray actionArray = new JsonArray();
        for (String action : actions) {
            actionArray.add(action);
        }
        task.add("actions", actionArray);
    }

    public void removeTask(String name) {
        for (int i = tasks.size() - 1; i >= 0; i--) {
            JsonObject task = tasks.getRaw(i).getAsJsonObject();
            if (task.get("name").getAsString().equals(name)) {
                tasks.remove(i);
                break;
            }
        }
    }

    public void removeTask(int index) {
        tasks.remove(index);
    }

    public void removeAllTasks() {
        tasks.clear();
    }

    public MenuClickTask getDefaultTask() {
        return new MenuClickTask("TASK_" + System.currentTimeMillis());
    }
}

class MenuClickTask {
    private static final Pattern PATTERN = Pattern.compile("(\\w+) (\\d+) (\\d+)( \\d+)?");
    public String description = "";
    public String name = "1";
    public boolean isLoop = false;
    public int startDelay = 40;
    public int delay = 10;

    List<String> actions = new ArrayList<>();

    MenuClickTask(String name) {
        this.name = name;
    }

    TaskAction getAction(int index) {
        String action = actions.get(index);
        if (action.startsWith("/")) {
            return new TaskAction(action);
        }
        Matcher matcher = PATTERN.matcher(action);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid action format: " + action);
        }

        String clickType = matcher.group(1).toLowerCase().replaceAll("_", "");
        int button = Integer.parseInt(matcher.group(2));
        int slot = Integer.parseInt(matcher.group(3));
        int delay = matcher.group(4) != null ? Integer.parseInt(matcher.group(4).trim()) : -1;

        if (slot < 0 || slot >= 54) {
            throw new IllegalArgumentException("Invalid action format: " + action);
        }

        if (clickType.equals("pickup")) {
            if (button != 0 && button != 1) {
                throw new IllegalArgumentException("Invalid action format: " + action);
            }
            return new TaskAction(ContainerInput.PICKUP, button, slot, delay);
        }
        if (clickType.equals("throw")) {
            if (button != 0 && button != 1) {
                throw new IllegalArgumentException("Invalid action format: " + action);
            }
            return new TaskAction(ContainerInput.THROW, button, slot, delay);
        }
        if (clickType.equals("swap")) {
            if (button != 40 && (button < 0 || button >= 9)) {
                throw new IllegalArgumentException("Invalid action format: " + action);
            }
            return new TaskAction(ContainerInput.SWAP, button, slot, delay);
        }
        if (clickType.equals("quickmove")) {
            if (button != 0 && button != 1) {
                throw new IllegalArgumentException("Invalid action format: " + action);
            }
            return new TaskAction(ContainerInput.QUICK_MOVE, button, slot, delay);
        }
        if (clickType.equals("clone")) {
            return new TaskAction(ContainerInput.CLONE, button, slot, delay);   
        }
        if (clickType.equals("pickupall")) {
            if (button != 0 && button != 1) {
                throw new IllegalArgumentException("Invalid action format: " + action);
            }
            return new TaskAction(ContainerInput.PICKUP_ALL, button, slot, delay);
        }
        if (clickType.equals("quickcraft")) {
            // TODO: 完善约束条件
            return new TaskAction(ContainerInput.QUICK_CRAFT, button, slot, delay);
        }
        throw new IllegalArgumentException("Unknown action type: " + clickType);
    }

}

class TaskAction {
    String command = "";
    ContainerInput clickType = null;
    int button = -1;
    int slot = -1;
    int delay = -1;

    TaskAction(ContainerInput type, int button, int slot) {
        this.clickType = type;
        this.button = button;
        this.slot = slot;
    }

    TaskAction(ContainerInput type, int button, int slot, int delay) {
        this.clickType = type;
        this.button = button;
        this.slot = slot;
        this.delay = delay;
    }

    TaskAction(String command) {
        this.command = command;
    }

    boolean isClick() {
        return clickType != null && slot != -1;
    }

    boolean isCommand() {
        return command != null && !command.isEmpty() && command.startsWith("/");
    }
}