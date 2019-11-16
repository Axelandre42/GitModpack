package ovh.axelandre42.gitmodpack.utils;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogWriter extends Writer {

    private Logger logger;
    private Level level;
    private Queue<String> mem = new LinkedList<>();

    public LogWriter(Logger logger, Level level) {
	this.logger = logger;
	this.level = level;
    }

    public LogWriter(Logger logger) {
	this(logger, Level.INFO);
    }

    public LogWriter() {
	this(LogManager.getRootLogger());
    }

    public Logger getLogger() {
	return logger;
    }

    public void setLogger(Logger logger) {
	this.logger = logger;
    }

    public Level getLevel() {
	return level;
    }

    public void setLevel(Level level) {
	this.level = level;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
	mem.add(new String(cbuf, off, len));
    }

    @Override
    public void flush() throws IOException {
	for (String msg : mem) {
	    logger.log(level, msg);
	}
    }

    @Override
    public void close() throws IOException {

    }

}
