package com.example.module.AutoCommand;

import java.util.List;
import java.util.UUID;

import com.example.config.BaseConfig;


public class AutoCommandConfig extends BaseConfig {
    public AutoCommandConfig(String moduleName) {
        super(moduleName);
    }
    
    public boolean enabled = AutoCommandConfig.getDefaultEnabled();
    public List<CommandBlock> commandBlocks = List.of();


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
        return "/stp survival2";
    }

    public static int getDefaultCheckInterval() {
        return 1200;
    }

    
}

class CommandBlock {
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
        this.name = "";
        this.ip = "";
        this.id = UUID.randomUUID().toString();
        this.worldName = AutoCommandConfig.getDefaultWorldName();
        this.enabled = AutoCommandConfig.getDefaultEnabled();
        this.runCounts = AutoCommandConfig.getDefaultRunCounts();
        this.delay = AutoCommandConfig.getDefaultCheckInterval();
        this.commands = List.of();
        this.cmdPtr = 0;
        this.isUpdate = false;
    }
}