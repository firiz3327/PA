package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.exec.result.ExecResult;
import net.firiz.polyglotapi.language.LanguageType;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class ScriptExec extends Exec {

    @Override
    public ExecResult exec(@NotNull LanguageType languageType, @NotNull String code, @NotNull Context context, @NotNull ByteArrayOutputStream contextStream) {
        Value value = null;
        String error = null;
        try {
            final Value eval = context.eval(languageType.getName(), code);
            value = eval;
            if (eval.hasMember("main")) {
                value.getMember("main").execute();
            }
        } catch (Exception e) {
            error = e.getLocalizedMessage();
        }
        return new ExecResult(
                contextStream.toString(StandardCharsets.UTF_8),
                value == null ? null : value.toString(),
                error
        );
    }

}
