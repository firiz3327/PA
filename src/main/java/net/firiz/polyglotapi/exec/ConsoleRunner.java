package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.APIConstants;
import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.language.LanguageType;
import net.firiz.polyglotapi.project.Project;
import net.firiz.polyglotapi.utils.EConsumer;
import net.firiz.polyglotapi.utils.ISThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

interface ConsoleRunner {

    default ConsoleResult process(File directory, int waitSecond, String... command) {
        return process(directory, waitSecond, process -> {
        }, command);
    }

    default @NotNull ConsoleResult process(File directory, int waitSecond, EConsumer<Process> processConsumer, String... command) {
        final Process process;
        try {
            process = new ProcessBuilder(command).directory(directory).start();
        } catch (IOException e) {
            return new ConsoleResult(e, true);
        }
        final ISThread ist = new ISThread();
        final ISThread est = new ISThread();
        final int exitValue;
        try {
            ist.setInputStream(process.getInputStream());
            est.setInputStream(process.getErrorStream());
            ist.start();
            est.start();
            processConsumer.accept(process);
            process.waitFor(waitSecond, TimeUnit.SECONDS);
            aliveDestroy(process);
            ist.join();
            est.join();
            exitValue = process.exitValue();
        } catch (Exception e) {
            return new ConsoleResult(e, false);
        } finally {
            aliveDestroy(process);
        }
        return new ConsoleResult(ist, est, exitValue);
    }

    default void aliveDestroy(Process process) {
        if (process.isAlive()) {
            process.destroy();
        }
    }

    default PolyglotResult build(@NotNull LanguageType languageType, @Nullable Project project, @NotNull String code, @NotNull String fileName, String[] command) {
        if (project == null) {
            return new PolyglotResult(languageType, code, "", null, "project is null.");
        }
        return buildNonNullProject(languageType, project, code, new File(project.getFolder(), fileName), command);
    }

    default PolyglotResult buildNonNullProject(@NotNull LanguageType languageType, @NotNull Project project, @NotNull String code, @NotNull File file, String[] command) {
        try (final FileOutputStream fos = new FileOutputStream(file);
             final PrintWriter printWriter = new PrintWriter(fos, true, StandardCharsets.UTF_8)
        ) {
            printWriter.print(code);
        } catch (IOException e) {
            return new PolyglotResult(languageType, code, "", null, e.getLocalizedMessage());
        }

        final ConsoleResult buildResult = process(
                project.getFolder(),
                APIConstants.CONSOLE_COMPILE_TIME,
                command
        );
        if (buildResult.hasException()) {
            if (buildResult.isCreateProcessError()) {
                return PolyglotResult.serverError(languageType, code, buildResult.getException());
            } else {
                return new PolyglotResult(languageType, code, "", null, buildResult.getException().getLocalizedMessage());
            }
        }
        final StringJoiner errorJoiner = new StringJoiner("\n");
        buildResult.getIst().getStringList().forEach(errorJoiner::add);
        if (errorJoiner.length() != 0) {
            return new PolyglotResult(languageType, code, "", null, errorJoiner.toString());
        }
        return null;
    }

    class ConsoleResult {
        private final ISThread ist;
        private final ISThread est;
        private final int exitValue;
        private final Exception e;
        private final boolean createProcessError;

        private ConsoleResult(ISThread ist, ISThread est, int exitValue) {
            this.ist = ist;
            this.est = est;
            this.exitValue = exitValue;
            this.e = null;
            this.createProcessError = false;
        }

        private ConsoleResult(Exception e, boolean createProcessError) {
            this.ist = null;
            this.est = null;
            this.exitValue = 1;
            this.e = e;
            this.createProcessError = createProcessError;
        }

        public boolean hasException() {
            return e != null;
        }

        public Exception getException() {
            return e;
        }

        public boolean isCreateProcessError() {
            return createProcessError;
        }

        public ISThread getIst() {
            return ist;
        }

        public ISThread getEst() {
            return est;
        }

        public int getExitValue() {
            return exitValue;
        }
    }
}
