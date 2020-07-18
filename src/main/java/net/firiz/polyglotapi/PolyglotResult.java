package net.firiz.polyglotapi;

import com.google.gson.annotations.Expose;
import net.firiz.polyglotapi.language.LanguageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PolyglotResult {

    @NotNull
    private final LanguageType language;
    @Nullable
    private final String code;
    @Expose
    @NotNull
    private final String log;
    @Expose
    @Nullable
    private final String returnValue;
    @Nullable
    private final String error;

    public PolyglotResult(@NotNull LanguageType language, @Nullable String code, @NotNull String log, @Nullable String returnValue, @Nullable String error) {
        this.language = language;
        this.code = code;
        this.log = log;
        this.returnValue = returnValue;
        this.error = error;
    }

    public LanguageType getLanguage() {
        return language;
    }

    public String getCode() {
        return code;
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

