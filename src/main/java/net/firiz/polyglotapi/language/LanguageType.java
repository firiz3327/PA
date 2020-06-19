package net.firiz.polyglotapi.language;

import net.firiz.polyglotapi.APIConstants;
import net.firiz.polyglotapi.exec.Exec;
import net.firiz.polyglotapi.exec.LLVMExec;
import net.firiz.polyglotapi.exec.NotSupportExec;
import net.firiz.polyglotapi.exec.ScriptExec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public enum LanguageType {
    UNKNOWN(new NotSupportExec(), "unknown", ""),
    JS(new ScriptExec(), "js", "javascript"), // initial
    PYTHON(new ScriptExec(), "python", "py"), // graalpython
    RUBY(new NotSupportExec(), "ruby"), // truffleruby 実行未確認
    R(new NotSupportExec(), "R", "r"), // fastr 実行未確認
    LLVM(new LLVMExec(), "llvm", "c", "c++", "cpp"); // llvm

    // 実際の実行環境のインストールは下記参照
    // https://www.graalvm.org/docs/getting-started/

    @NotNull
    private final Exec exec;
    @NotNull
    private final String name;
    @NotNull
    private final List<String> alias;
    private final boolean supported;

    LanguageType(@NotNull final Exec exec, final String... alias) {
        this.exec = exec;
        this.name = alias[0];
        this.alias = Arrays.asList(alias);
        this.supported = APIConstants.INSTALLED_LANGUAGES.contains(name);
    }

    @NotNull
    public static LanguageType search(@Nullable final String name) {
        if (name == null) {
            return UNKNOWN;
        }
        return Arrays.stream(values()).filter(type -> type.alias.contains(name.toLowerCase())).findFirst().orElse(UNKNOWN);
    }

    @NotNull
    public Exec getExec() {
        return exec;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public boolean isSupported() {
        return supported;
    }

}
