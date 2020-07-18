package net.firiz.polyglotapi.exec.result;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExecResult {

    @Nullable
    private final Exception e;
    @NotNull
    private final String log;
    @Nullable
    private final String returnValue;
    @Nullable
    private final String error;

    public ExecResult(String log, String returnValue, String error) {
        this(null, log, returnValue, error);
    }

    public ExecResult(@Nullable Exception e, @NotNull String log, @Nullable String returnValue, @Nullable String error) {
        this.e = e;
        this.log = log;
        this.returnValue = returnValue;
        this.error = error;
    }

    public boolean isException() {
        return e != null;
    }

    @Nullable
    public Exception getException() {
        return e;
    }

    @NotNull
    public String getLog() {
        return log;
    }

    @Nullable
    public String getReturnValue() {
        return returnValue;
    }

    @Nullable
    public String getError() {
        return error;
    }
}
