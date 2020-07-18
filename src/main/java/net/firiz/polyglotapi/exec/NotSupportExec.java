package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.exec.result.ExecResult;
import net.firiz.polyglotapi.language.LanguageType;
import org.graalvm.polyglot.Context;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;

public class NotSupportExec extends Exec {

    @Override
    public ExecResult exec(@NotNull LanguageType languageType, @NotNull String code, @NotNull String[] bindData, @NotNull Context context, @NotNull ByteArrayOutputStream contextStream) {
        throw new UnsupportedOperationException("not support language type.");
    }

}
