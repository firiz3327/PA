package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.exec.result.ExecResult;
import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.language.LanguageType;
import net.firiz.polyglotapi.project.Project;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JavaEspressoExec extends ContextExec implements ConsoleRunner {

    public JavaEspressoExec(@NotNull LanguageType languageType) {
        super(languageType);
    }

    @Override
    Object exec(@NotNull String code, @NotNull String[] bindData, @NotNull Context context, @NotNull ByteArrayOutputStream contextStream, @Nullable Project project) {
        final PolyglotResult buildResult = build(LanguageType.JAVA, project, code, "Main.java", new String[]{
                "javac", "Main.java"
        });
        if (buildResult == null) {
            if (project == null) {
                throw new AssertionError();
            }
            String result = null;
            String error = null;
            final Source source;
            try {
                source = Source.newBuilder(languageType.getName(), new File(project.getFolder(), "Main.class")).build();
            } catch (IOException e) {
                return PolyglotResult.serverError(languageType, code, e);
            }
            context.eval(source);
            final Value main = context.getBindings(languageType.getName()).getMember("main");
            main.execute();
            return new ExecResult(
                    contextStream.toString(StandardCharsets.UTF_8),
                    result,
                    error
            );
        }
        return buildResult;
    }

}
