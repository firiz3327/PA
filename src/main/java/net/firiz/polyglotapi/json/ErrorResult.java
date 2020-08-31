package net.firiz.polyglotapi.json;

import com.google.gson.annotations.Expose;

public class ErrorResult {

    @Expose
    private final boolean error = true;
    @Expose
    private final String log;

    public ErrorResult(String log) {
        this.log = log;
    }
}
