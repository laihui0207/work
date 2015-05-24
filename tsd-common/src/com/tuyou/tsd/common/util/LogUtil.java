package com.tuyou.tsd.common.util;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.tuyou.tsd.common.TSDConst;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class LogUtil {
	static {
        final LogConfigurator logConfigurator = new LogConfigurator();
                
        logConfigurator.setFileName(TSDConst.LOG_FILE_PATH + File.separator + "xbot.log");
        logConfigurator.setRootLevel(Level.TRACE);
        logConfigurator.setFilePattern("%d - [%p::%c] - %m%n");
        // Set log level of a specific logger
        logConfigurator.setLevel("org.apache", Level.ERROR);
        logConfigurator.configure();
    }

    public static void v(String tag, String msg) {
    	Logger.getLogger(tag).trace(msg);
    }

    public static void d(String tag, String msg) {
    	Logger.getLogger(tag).debug(msg);
    }

    public static void i(String tag, String msg) {
    	Logger.getLogger(tag).info(msg);
    }

    public static void w(String tag, String msg) {
    	Logger.getLogger(tag).warn(msg);
    }

    public static void e(String tag, String msg) {
    	Logger.getLogger(tag).error(msg);
    }
}
