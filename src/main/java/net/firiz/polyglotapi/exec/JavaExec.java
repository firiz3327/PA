package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.APIConstants;
import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.language.LanguageType;
import net.firiz.polyglotapi.project.Project;
import net.firiz.polyglotapi.utils.ISThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Stream;

@Deprecated(forRemoval = true)
public class JavaExec implements IExec, ConsoleRunner {

    @Override
    public @NotNull PolyglotResult exec(@NotNull String code, @NotNull String[] bindData, @Nullable Project project) {
        final PolyglotResult buildResult = build(LanguageType.JAVA, project, code, "Main.java", new String[]{
                "javac", "Main.java"
        });
        if (buildResult == null) {
            if (project == null) {
                throw new AssertionError();
            }
            final ConsoleResult runResult = process(
                    project.getFolder(),
                    APIConstants.CONSOLE_RUN_TIME,
                    process -> {
                        final OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
                        for (final String str : bindData) {
                            writer.append(str).append("\n");
                        }
                        writer.flush();
                    },
                    Stream.concat(Arrays.stream(new String[]{"java", "Main"}), Arrays.stream(bindData)).toArray(String[]::new)
            );
            if (runResult.hasException()) {
                return PolyglotResult.serverError(LanguageType.JAVA, code, runResult.getException());
            }
            final ISThread est = runResult.getEst();
            final StringJoiner resultJoiner = new StringJoiner("\n");
            final boolean isError = !est.getStringList().isEmpty();
            (isError ? est.getStringList() : runResult.getIst().getStringList()).forEach(resultJoiner::add);
            return new PolyglotResult(
                    LanguageType.JAVA,
                    code,
                    isError ? "" : resultJoiner.toString(),
                    String.valueOf(runResult.getExitValue()),
                    isError ? resultJoiner.toString() : null
            );
        }
        return buildResult;
    }

}
