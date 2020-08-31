package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.exec.result.ExecResult;
import net.firiz.polyglotapi.json.PolyglotResult;
import org.jetbrains.annotations.NotNull;

public class NotSupportExec extends Exec {

    @Override
    public PolyglotResult exec(@NotNull String code, @NotNull String[] bindData) {
        throw new UnsupportedOperationException("not support language type.");
    }

}
