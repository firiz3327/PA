package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.APIConstants;
import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.language.LanguageType;
import net.firiz.polyglotapi.project.Project;
import net.firiz.polyglotapi.utils.ISThread;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Stream;

@Deprecated(forRemoval = true)
public class JavaExec implements IExec, ConsoleRunner {

    @Override
    public @NotNull PolyglotResult exec(@NotNull String code, @NotNull String[] bindData, @NotNull Project project) {
        final PolyglotResult buildResult = build(LanguageType.JAVA, project, code, "Main.java", new String[]{
                project.isPPAPSwing() ? "../../libs/graalvm/bin/javac" : "javac", "Main.java"
        });
        if (buildResult == null) {
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
                    Stream.concat(Arrays.stream(new String[]{
                            project.isPPAPSwing() ? "../../libs/graalvm/bin/java" : "java",
                            "Main"
                    }), Arrays.stream(bindData)).toArray(String[]::new)
            );
            if (project.isPPAPSwing()) {
                runResult.getGlobalQueue().forEach(System.out::println);
            } else if (runResult.hasException()) {
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
