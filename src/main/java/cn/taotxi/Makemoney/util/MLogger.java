package cn.taotxi.Makemoney.util;

import org.slf4j.Logger;

import cn.taotxi.Makemoney.Makemoney;

public class MLogger {
    private String moduleName;
    private Logger logger;
    private boolean isDebug;

    public void setDebug(boolean bool) {
        isDebug = bool;
    }

    public MLogger(String moduleName) {
        this.moduleName = moduleName;
        this.logger = Makemoney.LOGGER;
    }

    public void info(String message) {
        if (!isDebug) {
            return;
        }
        logger.info("[{}] " + message, moduleName);
    }

    public void info(String message, Object arg1) {
        if (!isDebug) {
            return;
        }
        logger.info("[{}] " + message, moduleName, arg1);
    }

    public void info(String message, Object arg1, Object arg2) {
        if (!isDebug) {
            return;
        }
        logger.info("[{}] " + message, moduleName, arg1, arg2);
    }

    public void info(String message, Object... args) {
        if (!isDebug) {
            return;
        }
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

    public void warn(String message) {
        logger.warn("[{}] " + message, moduleName);
    }

    public void warn(String message, Object arg1) {
        logger.warn("[{}] " + message, moduleName, arg1);
    }

    public void warn(String message, Object arg1, Object arg2) {
        logger.warn("[{}] " + message, moduleName, arg1, arg2);
    }

    public void warn(String message, Object... args) {
        logger.warn("[{}] " + message, moduleName, args);
    }
}
