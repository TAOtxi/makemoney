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
        commandBlocks.add(block);
    }

    public void removeCommandBlock(CommandBlock block) {
        removeCommandBlock(block.id);
    }

    public void removeCommandBlock(String id) {
        commandBlocks.removeIf(b -> b.id.equals(id));
    }

    public void addCommandBlock() {
        addCommandBlock(new CommandBlock());
    }

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
        return "/me hhh";
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

    public class CommandBlock {
        public String id;
        public String name;
        public String ip;
        public String worldName;
        public boolean enabled;
        public int runCounts;
        public int delay;
        public List<String> commands;
        public transient int cmdPtr = 0;
        public transient boolean isUpdate = false;

        public CommandBlock() {
            this.name = AutoCommandConfig.getDefaultName();
            this.ip = AutoCommandConfig.getDefaultIp();
            this.id = UUID.randomUUID().toString();
            this.worldName = AutoCommandConfig.getDefaultWorldName();
            this.enabled = AutoCommandConfig.getDefaultEnabled();
            this.runCounts = AutoCommandConfig.getDefaultRunCounts();
            this.delay = AutoCommandConfig.getDefaultDelay();
            this.commands = new ArrayList<>();
            this.cmdPtr = 0;
            this.isUpdate = false;
        }
    }
}