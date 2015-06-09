package com.socialmdm.util;

import org.apache.log4j.Logger;

/**
 * 
 * @author Naveen
 *
 */
public class LoggerUtil {
    
    /**
     * Logger for Debug
     * 
     * @param message
     * @param clazz
     */
    public static void writeDebug(String message, Class<?> clazz){
        Logger.getLogger(clazz).debug(message);
    }
    
    /**
     * Logger for info
     * 
     * @param message
     * @param clazz
     */
    public static void writeInfo(String message, Class<?> clazz){
        Logger.getLogger(clazz).info(message);
    }
    
    /**
     * Logger for warning
     * 
     * @param message
     * @param clazz
     */
    public static void writeWarn(String message, Class<?> clazz){
        Logger.getLogger(clazz).debug(message);
    }
    
    /**
     * Logger for error
     * 
     * @param message
     * @param clazz
     */
    public static void writeError(String message, Class<?> clazz){
        Logger.getLogger(clazz).error(message);
    }
    
}