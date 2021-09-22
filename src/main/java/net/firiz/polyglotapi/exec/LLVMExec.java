package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.APIConstants;
import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.language.LanguageType;
import net.firiz.polyglotapi.project.Project;
import net.firiz.polyglotapi.utils.ISThread;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.StringJoiner;

/**
 * @deprecated 20.2.0現在、contextにinputStream、outputStreamを設定してもllvmでは認識してくれないので、graalvmのlliコマンドを実行する
 */
@Deprecated(forRemoval = true)
public class LLVMExec implements IExec, ConsoleRunner {

    @Override
    public @NotNull PolyglotResult exec(@NotNull String code, @NotNull String[] bindData, @NotNull Project project) {

        final String uuid = project.getUUID().toString();
        final File file = new File(project.getFolder(), uuid + ".c");
        final PolyglotResult buildResult = buildNonNullProject(LanguageType.LLVM, project, code, file, new String[]{
                (project.isPPAPSwing() ? "../../libs/graalvm" : System.getProperty("java.home")) + "/languages/llvm/native/bin/gcc",
                file.getName(),
                "-o",
                uuid,
                "-w"
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
                    project.isPPAPSwing() ? "../../libs/graalvm/languages/llvm/bin/lli" : "lli", uuid
            );
            if (project.isPPAPSwing()) {
                runResult.getGlobalQueue().forEach(System.out::println);
            } else if (runResult.hasException()) {
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
        return buildResult;
    }
}
