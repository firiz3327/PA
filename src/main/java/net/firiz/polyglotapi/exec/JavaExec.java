package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.APIConstants;
import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.language.LanguageType;
import net.firiz.polyglotapi.project.Project;
import net.firiz.polyglotapi.utils.ISThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class JavaExec extends ConsoleExec {

    @Override
    public PolyglotResult exec(@NotNull String code, @NotNull String[] bindData, @Nullable Project project) {
        if (project == null) {
            return new PolyglotResult(LanguageType.JAVA, code, "", null, "project is null.");
        }
        final File javaFile = new File(project.getFolder(), "Main.java");
        try (final FileOutputStream fos = new FileOutputStream(javaFile);
             final PrintWriter printWriter = new PrintWriter(fos, true, StandardCharsets.UTF_8)
        ) {
            printWriter.print(code);
        } catch (IOException e) {
            return new PolyglotResult(LanguageType.JAVA, code, "", null, e.getLocalizedMessage());
        }

        final ConsoleResult buildResult = process(
                project.getFolder(),
                APIConstants.CONSOLE_COMPILE_TIME,
                "javac", javaFile.getName()
        );
        if (buildResult.hasException()) {
            if (buildResult.isCreateProcessError()) {
                return PolyglotResult.serverError(LanguageType.JAVA, code, buildResult.getException());
            } else {
                return new PolyglotResult(LanguageType.JAVA, code, "", null, buildResult.getException().getLocalizedMessage());
            }
        }
        final StringJoiner errorJoiner = new StringJoiner("\n");
        buildResult.getIst().getStringList().forEach(errorJoiner::add);
        if (errorJoiner.length() != 0) {
            return new PolyglotResult(LanguageType.JAVA, code, "", null, errorJoiner.toString());
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

}
