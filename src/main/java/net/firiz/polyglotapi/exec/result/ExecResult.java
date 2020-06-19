package net.firiz.polyglotapi.exec.result;

public class ExecResult {

    private final Exception e;
    private final String log;
    private final String returnValue;
    private final String error;

    public ExecResult(String log, String returnValue, String error) {
        this(null, log, returnValue, error);
    }

    public ExecResult(Exception e, String log, String returnValue, String error) {
        this.e = e;
        this.log = log;
        this.returnValue = returnValue;
        this.error = error;
    }

    public boolean isException() {
        return e != null;
    }

    public Exception getException() {
        return e;
    }

    public String getLog() {
        return log;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public String getError() {
        return error;
    }
}
