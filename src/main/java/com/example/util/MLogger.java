package com.example.util;

import org.slf4j.Logger;

import com.example.Makemoney;

public class MLogger {
    private String moduleName;
    private Logger logger;

    public MLogger(String moduleName) {
        this.moduleName = moduleName;
        this.logger = Makemoney.LOGGER;
    }

    public void info(String message) {
        logger.info("[{}] " + message, moduleName);
    }

    public void info(String message, Object arg1) {
        logger.info("[{}] " + message, moduleName, arg1);
    }

    public void info(String message, Object arg1, Object arg2) {
        logger.info("[{}] " + message, moduleName, arg1, arg2);
    }

    public void info(String message, Object... args) {
        logger.info("[{}] " + message, moduleName, args);
    }
    
    public void error(String message) {
        logger.error("[{}] " + message, moduleName);
    }
    
    public void error(String message, Object arg1) {
        logger.error("[{}] " + message, moduleName, arg1);
    }
    
    public void error(String message, Object arg1, Object arg2) {
        logger.error("[{}] " + message, moduleName, arg1, arg2);
    }
    
    public void error(String message, Object... args) {
        logger.error("[{}] " + message, moduleName, args);
    }
}
