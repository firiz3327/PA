package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.exec.result.ExecResult;
import net.firiz.polyglotapi.language.LanguageType;
import org.graalvm.polyglot.Context;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public abstract class Exec {

    abstract public ExecResult exec(@NotNull final LanguageType languageType, @NotNull final String code, @NotNull final Context context, @NotNull final ByteArrayOutputStream contextStream);

    protected final ExecResult errorResult(Exception e, ByteArrayOutputStream contextStream) {
        return new ExecResult(e, contextStream.toString(StandardCharsets.UTF_8), null, e.getLocalizedMessage());
    }

}
