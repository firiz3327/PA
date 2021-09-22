package net.firiz.polyglotapi.language;

import net.firiz.polyglotapi.APIConstants;
import net.firiz.polyglotapi.exec.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public enum LanguageType {
    UNKNOWN(t -> new NotSupportExec(), false, "unknown", ""),
    JS("js", "javascript"), // initial
    PYTHON("python", "py"), // graalpython
    RUBY(t -> new NotSupportExec(), false, "ruby"), // truffleruby 実行未確認
    R(t -> new NotSupportExec(), false, "R", "r"), // fastr 実行未確認
    LLVM(t -> new LLVMExec(), true, "llvm", "c", "c++", "cpp"), // llvm
    JAVA(t -> new JavaExec(), true, true, "java"),
    WASM(WASMExec::new, true, "wasm")
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
    private final boolean autoProject;

    LanguageType(@NotNull final Function<LanguageType, IExec> exec, boolean autoProject, final String... alias) {
        this(exec, APIConstants.INSTALLED_LANGUAGES.contains(alias[0]), autoProject, alias);
    }

    LanguageType(final String... alias) {
        this.exec = new ScriptExec(this);
        this.name = alias[0];
        this.alias = Arrays.asList(alias);
        this.supported = APIConstants.INSTALLED_LANGUAGES.contains(name);
        this.autoProject = false;
    }

    LanguageType(@NotNull final Function<LanguageType, IExec> exec, boolean supported, boolean autoProject, final String... alias) {
        this.exec = exec.apply(this);
        this.name = alias[0];
        this.alias = Arrays.asList(alias);
        this.supported = supported;
        this.autoProject = autoProject;
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

    public boolean isAutoProject() {
        return autoProject;
    }
}
