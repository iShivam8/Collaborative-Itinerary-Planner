package logs;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger class is used to log metadata throughout the application.
 */
public class Logger {

  private final String filename;
  private final String serverId;

  /**
   * Logger requires the filepath of the file to be used as log file.
   *
   * @param filename - filename of the file to be used as log file
   */
  public Logger(String filename, String serverId) {
    this.filename = filename;
    this.serverId = serverId;
  }

  /**
   * Logs message to log file as per the current timestamp and log type.
   *
   * @param type - type of log: error or debug
   * @param stdout - whether to print to stdout or not
   * @param args - string array of strings to be logged
   */
  private void log(String type, boolean stdout, String... args) {
    String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    StringBuilder logMessage = new StringBuilder();
    logMessage.append(currentTime).append("  [").append(type.toUpperCase()).append("]  ");

    for (String arg : args) {
      logMessage.append(" ").append(arg);
    }

    write(logMessage);

    if (stdout) {
      System.out.println(logMessage);
    }
  }

  private void write(StringBuilder logMessage) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
      writer.println(logMessage);
    } catch (IOException e) {
      System.out.println("Cannot open the file!");
    }
  }

  /**
   * Logs message to log file using ERROR log level.
   *
   * @param stdout - whether to print to stdout or not
   * @param args - string array of strings to be logged
   */
  public void error(boolean stdout, String... args) {
    log("error", stdout, args);
  }

  /**
   * Logs message to log file using DEBUG log level.
   *
   * @param stdout - whether to print to stdout or not
   * @param args - string array of strings to be logged
   */
  public void debug(boolean stdout, String... args) {
    log("debug", stdout, args);
  }
}