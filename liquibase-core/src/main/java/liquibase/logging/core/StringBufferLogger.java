package liquibase.logging.core;

import org.apache.commons.lang.StringUtils;

import liquibase.logging.LogLevel;

public class StringBufferLogger extends DefaultLogger {

  private static int LOGGER_BUFFER_MAX = 1000000;
  private static int LOGGER_BUFFER_LIMIT = LOGGER_BUFFER_MAX * 99 / 100;
  private static StringBuffer loggerBuffer = new StringBuffer(LOGGER_BUFFER_MAX);
  private static int deletedBufferSize = 0;
  private static boolean enabled = false;
  private static boolean hasSevereLog = false;

  @Deprecated // kept for compatibility
  public static void enable() {}

  static void initTest() {
    LOGGER_BUFFER_MAX = 100;
    LOGGER_BUFFER_LIMIT = 90;
    loggerBuffer = new StringBuffer(LOGGER_BUFFER_MAX);
  }
  
  public static synchronized void reset() {
    loggerBuffer.setLength(0);
    deletedBufferSize = 0;
    hasSevereLog = false;
    enabled = true;
  }

  public static boolean hasSevereLog() {
    return hasSevereLog;
  }

  private static final String DOT_LINE = StringUtils.leftPad("\n", 199, ".");
  
  public static synchronized String getLog(int start) {
    start = start - deletedBufferSize;
    int length = loggerBuffer.length();
    if (start > length) {
      return "";
    } else if (start < 0) {
      return StringUtils.leftPad("", -start, DOT_LINE);
    } else {
      return loggerBuffer.substring(start, length);
    }
  }

  @Override
  public int getPriority() {
    return super.getPriority() + 1;
  }

  private static synchronized void printStringBufferLog(String message) {
    loggerBuffer.append(message).append("\n");
    if (loggerBuffer.length() > LOGGER_BUFFER_LIMIT) {
      loggerBuffer.delete(0, LOGGER_BUFFER_MAX / 2);
      deletedBufferSize += LOGGER_BUFFER_MAX / 2;
    }
  }
  
  @Override
  protected void print(LogLevel logLevel, String message) {
    super.print(logLevel, message);
    if (enabled) {
      printStringBufferLog(message);
    }
  }

  @Override
  public void severe(String message) {
    super.severe(message);
    hasSevereLog = true;
  }

  @Override
  public void severe(String message, Throwable e) {
    super.severe(message, e);
    hasSevereLog = true;
  }

}