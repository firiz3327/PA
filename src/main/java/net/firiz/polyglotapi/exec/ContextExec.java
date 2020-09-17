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

    protected final LanguageType languageType;

    protected ContextExec(LanguageType languageType) {
        this.languageType = languageType;
    }

    public PolyglotResult exec(@NotNull String code, @NotNull String[] bindData, @Nullable Project project) {
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
                final ExecResult result = exec(
                        code,
                        bindData,
                        context,
                        contextStream,
                        project
                );
                if (result.isException()) {
                    return PolyglotResult.serverError(languageType, code, result);
                } else {
                    returnValue = result.getReturnValue();
                    error = result.getError();
                }
            } catch (Exception e) {
                return PolyglotResult.serverError(languageType, code, e);
            }
            return new PolyglotResult(languageType, code, contextStream.toString(StandardCharsets.UTF_8), returnValue, error);
        } catch (IOException e) {
            return PolyglotResult.serverError(languageType, code, e);
        }
    }

    abstract ExecResult exec(@NotNull final String code, @NotNull String[] bindData, @NotNull final Context context, @NotNull final ByteArrayOutputStream contextStream, @Nullable Project project);

    protected final ExecResult errorResult(Exception e, ByteArrayOutputStream contextStream) {
        return new ExecResult(e, contextStream.toString(StandardCharsets.UTF_8), null, e.getLocalizedMessage());
    }

}
