package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.exec.result.ExecResult;
import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.language.LanguageType;
import net.firiz.polyglotapi.project.Project;
import org.graalvm.polyglot.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public abstract class ContextExec implements IExec {

    @NotNull
    protected final LanguageType languageType;

    protected ContextExec(@NotNull LanguageType languageType) {
        this.languageType = languageType;
    }

    public final @NotNull PolyglotResult exec(@NotNull String code, @NotNull String[] bindData, @Nullable Project project) {
        try (final ByteArrayOutputStream contextStream = new ByteArrayOutputStream();
             final InputStream stdinStream = new ByteArrayInputStream(String.join(System.lineSeparator(), Arrays.asList(bindData)).getBytes(StandardCharsets.UTF_8))) {
            final String returnValue;
            final String error;
            try (final Context context = Context.newBuilder()
                    .in(stdinStream)
                    .out(contextStream)
                    .err(contextStream)
                    .allowAllAccess(true)
                    .build()) {
                final Object exec = exec(
                        code,
                        bindData,
                        context,
                        contextStream,
                        project
                );
                if (exec instanceof ExecResult) {
                    final ExecResult result = (ExecResult) exec;
                    if (result.isException()) {
                        return PolyglotResult.serverError(languageType, code, result);
                    } else {
                        returnValue = result.getReturnValue();
                        error = result.getError();
                    }
                } else if (exec instanceof PolyglotResult) {
                    return (PolyglotResult) exec;
                } else {
                    return PolyglotResult.serverError(languageType, code, new RuntimeException("lol error. Please tell the developer that this error has occurred."));
                }
            } catch (Exception e) {
                return PolyglotResult.serverError(languageType, code, e);
            }
            return new PolyglotResult(languageType, code, contextStream.toString(StandardCharsets.UTF_8), returnValue, error);
        } catch (IOException e) {
            return PolyglotResult.serverError(languageType, code, e);
        }
    }

    abstract Object exec(@NotNull final String code, @NotNull String[] bindData, @NotNull final Context context, @NotNull final ByteArrayOutputStream contextStream, @Nullable Project project);

}
