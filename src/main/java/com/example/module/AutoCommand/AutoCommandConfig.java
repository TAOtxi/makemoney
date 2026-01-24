package com.example.module.AutoCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.config.BaseConfig;

public class AutoCommandConfig extends BaseConfig {
    public AutoCommandConfig(String moduleName) {
        super(moduleName);
    }

    public boolean enabled = AutoCommandConfig.getDefaultEnabled();
    public List<CommandBlock> commandBlocks = new ArrayList<>();

    public void addCommandBlock(CommandBlock block) {
        commandBlocks.addFirst(block);
    }

    public void removeCommandBlock(CommandBlock block) {
        this.commandBlocks.remove(block);
    }

    public void addCommandBlock() {
        addCommandBlock(new CommandBlock());
    }

    public static boolean getDefaultEnabled() {
        return false;
    }

    public class CommandBlock {
        public String id = UUID.randomUUID().toString();
        public String name = getDefaultName();
        public String ip = getDefaultIp();
        public String worldName = getDefaultWorldName();
        public boolean enabled = getDefaultEnabled();
        public int runCounts = getDefaultRunCounts();
        public int delay = getDefaultDelay();
        public List<String> commands = new ArrayList<>();
        public transient int cmdPtr = 0;
        public transient boolean isUpdate = false;

        public static boolean getDefaultEnabled() {
            return false;
        }

        public static String getDefaultWorldName() {
            return "minecraft:overworld";
        }

        public static int getDefaultRunCounts() {
            return 1;
        }

        public static String getDefaultCommand() {
            return "/say I hate the world";
        }

        public static int getDefaultDelay() {
            return 1200;
        }

        public static String getDefaultIp() {
            return "2b2t.org";
        }

        public static String getDefaultName() {
            return "";
        }
    }
}