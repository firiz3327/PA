package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.APIConstants;
import net.firiz.polyglotapi.ISThread;
import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.language.LanguageType;
import org.jetbrains.annotations.NotNull;

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
    public PolyglotResult exec(@NotNull String code, @NotNull String[] bindData) {
        final String uuid = UUID.randomUUID().toString();
        final File llvmFile = new File(APIConstants.LLVM_FOLDER, uuid + ".c");
        try (final FileOutputStream fos = new FileOutputStream(llvmFile);
             final PrintWriter printWriter = new PrintWriter(fos, true, StandardCharsets.UTF_8)
        ) {
            printWriter.print(code);
        } catch (IOException e) {
            return new PolyglotResult(LanguageType.LLVM, code, "", null, e.getLocalizedMessage());
        }

        final ConsoleResult buildResult = process(
                APIConstants.LLVM_FOLDER,
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
                APIConstants.LLVM_FOLDER,
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

//        final Process clangProcess;
//        try {
//            clangProcess = new ProcessBuilder(
//                    System.getProperty("java.home") + "/languages/llvm/native/bin/clang",
//                    llvmFile.getName(), "-o", llvmUUID, "-w"
//            ).directory(APIConstants.LLVM_FOLDER).start();
//        } catch (IOException e) {
//            return PolyglotResult.serverError(LanguageType.LLVM, code, e);
//        }
//        final ISThread buildIST = new ISThread();
//        try {
//            buildIST.setInputStream(clangProcess.getErrorStream());
//            buildIST.start();
//            clangProcess.waitFor(APIConstants.CONSOLE_COMPILE_TIME, TimeUnit.SECONDS);
//            aliveDestroy(clangProcess);
//            buildIST.join();
//        } catch (InterruptedException e) {
//            return new PolyglotResult(LanguageType.LLVM, code, "", null, e.getLocalizedMessage());
//        } finally {
//            aliveDestroy(clangProcess);
//        }
//        final StringJoiner errorJoiner = new StringJoiner("\n");
//        buildIST.getStringList().forEach(errorJoiner::add);
//        if (errorJoiner.length() != 0) {
//            return new PolyglotResult(LanguageType.LLVM, code, "", null, errorJoiner.toString());
//        }
//
//        final Process process;
//        try {
//            process = new ProcessBuilder("lli", llvmUUID).directory(APIConstants.LLVM_FOLDER).start();
//        } catch (IOException e) {
//            return PolyglotResult.serverError(LanguageType.LLVM, code, e);
//        }
//        final ISThread ist = new ISThread();
//        final ISThread est = new ISThread();
//        final int exitValue;
//        try {
//            ist.setInputStream(process.getInputStream());
//            est.setInputStream(process.getErrorStream());
//            ist.start();
//            est.start();
//            final OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
//            for (final String str : bindData) {
//                writer.append(str).append("\n");
//            }
//            writer.flush();
//            process.waitFor(APIConstants.CONSOLE_RUN_TIME, TimeUnit.SECONDS);
//            aliveDestroy(process);
//            ist.join();
//            est.join();
//            exitValue = process.exitValue();
//        } catch (IOException | InterruptedException e) {
//            return PolyglotResult.serverError(LanguageType.LLVM, code, e);
//        } finally {
//            aliveDestroy(process);
//        }
//        final StringJoiner resultJoiner = new StringJoiner("\n");
//        final boolean isError = !est.getStringList().isEmpty();
//        (isError ? est.getStringList() : ist.getStringList()).forEach(resultJoiner::add);
//        return new PolyglotResult(
//                LanguageType.LLVM,
//                code,
//                isError ? "" : resultJoiner.toString(),
//                String.valueOf(exitValue),
//                isError ? resultJoiner.toString() : null
//        );
    }
}
