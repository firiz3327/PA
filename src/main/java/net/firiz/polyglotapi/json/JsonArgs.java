package net.firiz.polyglotapi.json;

import com.google.gson.annotations.Expose;
import net.firiz.polyglotapi.language.LanguageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JsonArgs {

    @Expose
    @NotNull
    private final LanguageType languageType;
    @Expose
    @NotNull
    private final String code64;
    @Expose
    @NotNull
    private final String[] stdin;

    @Expose
    @Nullable
    private final String fileName;
    @Expose
    @Nullable
    private final String file64;

    private String code;

    public JsonArgs(@NotNull LanguageType languageType, @NotNull String code64, @NotNull String[] stdin, @Nullable String fileName, @Nullable String file64) {
        this.languageType = languageType;
        this.code64 = code64;
        this.stdin = stdin;
        this.fileName = fileName;
        this.file64 = file64;
    }

    public void init() {
        this.code = new String(Base64.getDecoder().decode(code64.getBytes()), StandardCharsets.UTF_8);
    }

    @NotNull
    public LanguageType getLanguageType() {
        return languageType;
    }

    @NotNull
    public String getCode() {
        if (code == null) {
            throw new IllegalStateException("The init method has not been executed.");
        }
        return code;
    }

    @NotNull
    public String[] getStdin() {
        return stdin;
    }

    @Nullable
    public String getFileName() {
        return fileName;
    }

    @Nullable
    public String getFile64() {
        return file64;
    }

    public boolean isAutoProject() {
        return getFileName() != null;
    }
}
