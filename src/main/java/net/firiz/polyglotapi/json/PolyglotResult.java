package net.firiz.polyglotapi.json;

import com.google.gson.annotations.Expose;
import net.firiz.polyglotapi.exec.result.ExecResult;
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
    private final String errorMsg;
    private final boolean error;

    public PolyglotResult(@NotNull LanguageType language, @Nullable String code, @NotNull String log, @Nullable String returnValue, @Nullable String errorMsg) {
        this.language = language;
        this.code = code;
        this.log = log;
        this.returnValue = returnValue;
        this.errorMsg = errorMsg;
        this.error = this.errorMsg != null && !this.errorMsg.isEmpty();
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

    public String getErrorMsg() {
        return errorMsg;
    }

    public boolean isError() {
        return error;
    }

    public static PolyglotResult serverError(LanguageType languageType, String code, Exception e) {
        return serverError(languageType, code, new ExecResult(e, "", null, null));
    }

    public static  PolyglotResult serverError(LanguageType languageType, String code, ExecResult result) {
        if (result.isException()) {
            assert result.getException() != null;
            result.getException().printStackTrace();
        }
        return new PolyglotResult(languageType, code, result.getLog(), result.getReturnValue(), "server error.");
    }
}

