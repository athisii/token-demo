package com.cdac.tokendemo.logging;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.*;

/**
 * @author athisii, CDAC
 * Created on 17/12/22
 */
public class ApplicationLog {
    //Suppress default constructor for noninstantiability
    private ApplicationLog() {
        throw new AssertionError("The ApplicationLog methods should be accessed statically");
    }

    private static Handler handler;
    private static final Logger LOGGER = Logger.getLogger(ApplicationLog.class.getName());

    static {
        try {
            // /var/log/enrollment/enrollmentstationapp.log
            LocalDateTime now = LocalDateTime.now();
            String time = now.getDayOfMonth() + "-" + now.getMonth() + "-" + now.getYear() + "-" + now.getHour() + "-" + now.getMinute();
            String logfileName = "/var/log/enrollment/token-demo-" + time + ".log";
            handler = new FileHandler(logfileName, 10240000, 2); //1024000 is 10Mb - It will roll over after a file becomes 1Mb
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            LOGGER.addHandler(handler);
            LOGGER.setLevel(Level.INFO);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Exception in Application Log. Failed to add new handler. Shutting down.......");
            System.exit(-1);
        } catch (SecurityException ex) {
            LOGGER.log(Level.SEVERE, "Exception in Application Log. Security Exception. Shutting down.......");
            System.exit(-1);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error occurred in creating Application Log. Shutting down.......");
            System.exit(-1);
        }
    }

    public static Logger getLogger(Class<?> klass) {
        var logger = Logger.getLogger(klass.getName());
        logger.addHandler(handler);
        return logger;
    }
}
