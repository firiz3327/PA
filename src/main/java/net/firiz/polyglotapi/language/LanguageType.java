package net.firiz.polyglotapi.language;

import net.firiz.polyglotapi.APIConstants;
import net.firiz.polyglotapi.exec.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public enum LanguageType {
    UNKNOWN(new NotSupportExec(), "unknown", ""),
    JS("js", "javascript"), // initial
    PYTHON("python", "py"), // graalpython
    RUBY(new NotSupportExec(), "ruby"), // truffleruby 実行未確認
    R(new NotSupportExec(), "R", "r"), // fastr 実行未確認
    LLVM(new LLVMExec(), "llvm", "c", "c++", "cpp"), // llvm
    JAVA(new JavaExec(), true, "java") // javassist
    ;

    // 実際の実行環境のインストールは下記参照
    // https://www.graalvm.org/docs/getting-started/

    @NotNull
    private final IExec exec;
    @NotNull
    private final String name;
    @NotNull
    private final List<String> alias;
    private final boolean supported;

    LanguageType(@NotNull final IExec exec, final String... alias) {
        this(exec, APIConstants.INSTALLED_LANGUAGES.contains(alias[0]), alias);
    }

    LanguageType(final String... alias) {
        this.exec = new ScriptExec(this);
        this.name = alias[0];
        this.alias = Arrays.asList(alias);
        this.supported = APIConstants.INSTALLED_LANGUAGES.contains(name);
    }

    LanguageType(@NotNull final IExec exec, boolean supported, final String... alias) {
        this.exec = exec;
        this.name = alias[0];
        this.alias = Arrays.asList(alias);
        this.supported = supported;
    }

    @NotNull
    public static LanguageType search(@Nullable final String name) {
        if (name == null) {
            return UNKNOWN;
        }
        return Arrays.stream(values()).filter(type -> type.alias.contains(name.toLowerCase())).findFirst().orElse(UNKNOWN);
    }

    @NotNull
    public IExec getExec() {
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
