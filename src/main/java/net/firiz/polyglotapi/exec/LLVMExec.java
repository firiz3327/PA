package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.APIConstants;
import net.firiz.polyglotapi.project.Project;
import net.firiz.polyglotapi.utils.ISThread;
import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.language.LanguageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * @deprecated 20.2.0現在、contextにinputStream、outputStreamを設定してもllvmでは認識してくれないので、graalvmのlliコマンドを実行する
 */
@Deprecated(forRemoval = true)
public class LLVMExec extends ConsoleExec {

    @Override
    public PolyglotResult exec(@NotNull String code, @NotNull String[] bindData, @Nullable Project project) {
        if (project == null) {
            return new PolyglotResult(LanguageType.LLVM, code, "", null, "project is null.");
        }
        final String uuid = project.getUUID().toString();
        final File llvmFile = new File(project.getFolder(), uuid + ".c");
        try (final FileOutputStream fos = new FileOutputStream(llvmFile);
             final PrintWriter printWriter = new PrintWriter(fos, true, StandardCharsets.UTF_8)
        ) {
            printWriter.print(code);
        } catch (IOException e) {
            return new PolyglotResult(LanguageType.LLVM, code, "", null, e.getLocalizedMessage());
        }

        final ConsoleResult buildResult = process(
                project.getFolder(),
                APIConstants.CONSOLE_COMPILE_TIME,
                System.getProperty("java.home") + "/languages/llvm/native/bin/clang", llvmFile.getName(), "-o", uuid, "-w"
        );
        if (buildResult.hasException()) {
            if (buildResult.isCreateProcessError()) {
                return PolyglotResult.serverError(LanguageType.LLVM, code, buildResult.getException());
            } else {
                return new PolyglotResult(LanguageType.LLVM, code, "", null, buildResult.getException().getLocalizedMessage());
            }
        }
        final StringJoiner errorJoiner = new StringJoiner("\n");
        buildResult.getIst().getStringList().forEach(errorJoiner::add);
        if (errorJoiner.length() != 0) {
            return new PolyglotResult(LanguageType.LLVM, code, "", null, errorJoiner.toString());
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
                "lli", uuid
        );
        if (runResult.hasException()) {
            return PolyglotResult.serverError(LanguageType.LLVM, code, runResult.getException());
        }
        final ISThread est = runResult.getEst();
        final StringJoiner resultJoiner = new StringJoiner("\n");
        final boolean isError = !est.getStringList().isEmpty();
        (isError ? est.getStringList() : runResult.getIst().getStringList()).forEach(resultJoiner::add);
        return new PolyglotResult(
                LanguageType.LLVM,
                code,
                isError ? "" : resultJoiner.toString(),
                String.valueOf(runResult.getExitValue()),
                isError ? resultJoiner.toString() : null
        );
    }
}
