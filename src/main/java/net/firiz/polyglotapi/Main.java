package net.firiz.polyglotapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.firiz.polyglotapi.exec.result.ExecResult;
import net.firiz.polyglotapi.language.LanguageType;
import org.graalvm.polyglot.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class Main {

    public static void main(String[] args) {
        new Main().start(args);
    }

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    /**
     * 渡されたコードを実行し結果を標準出力へ書き込みます<br>
     * <br>
     * コードの実行に成功した場合、標準出力にhtmlエスケープされたjson(log及びreturnValue)を書き込みます<br>
     * コードの実行に失敗した場合、標準エラー出力にエラー内容を書き込みます<br>
     * <br>
     * LLVM以外ではタイムアウト処理をしていないので、送信側で施してください<br>
     *
     * @param args プログラム実行時の引数
     */
    private void start(String[] args) {
        final LanguageType languageType = LanguageType.search(args[0]);
        final String code = new String(Base64.getDecoder().decode(args[1].getBytes()), StandardCharsets.UTF_8);
        final String[] stdin = args.length >= 3 ? Arrays.copyOfRange(args, 2, args.length) : new String[0];
        final PolyglotResult polyglotResult = ((Supplier<PolyglotResult>) () -> {
            if (!languageType.isSupported()) {
                return new PolyglotResult(languageType, code, "", null, "'" + languageType.getName() + "' language is not supported.");
            } else if (languageType == LanguageType.LLVM) {
                return lli(code, stdin); // deprecated
            }
            try (final ByteArrayOutputStream contextStream = new ByteArrayOutputStream();
                 final InputStream stdinStream = new ByteArrayInputStream(String.join(System.lineSeparator(), Arrays.asList(stdin)).getBytes(StandardCharsets.UTF_8))) {
                final String returnValue;
                final String error;
                try (final Context context = Context.newBuilder()
                        .in(stdinStream)
                        .out(contextStream)
                        .err(contextStream)
                        .allowAllAccess(true)
                        .build()) {
                    final ExecResult result = languageType.getExec().exec(
                            languageType,
                            code,
                            stdin,
                            context,
                            contextStream
                    );
                    if (result.isException()) {
                        return serverError(languageType, code, result);
                    } else {
                        returnValue = result.getReturnValue();
                        error = result.getError();
                    }
                } catch (Exception e) {
                    return serverError(languageType, code, e);
                }
                return new PolyglotResult(languageType, code, contextStream.toString(StandardCharsets.UTF_8), returnValue, error);
            } catch (IOException e) {
                return serverError(languageType, code, e);
            }
        }).get();
        if (polyglotResult.getError() != null && !polyglotResult.getError().isEmpty()) {
            System.err.println(polyglotResult.getError());
        } else {
            System.out.println(gson.toJson(polyglotResult));
        }
    }

    private PolyglotResult serverError(LanguageType languageType, String code, Exception e) {
        return serverError(languageType, code, new ExecResult(e, "", null, null));
    }

    private PolyglotResult serverError(LanguageType languageType, String code, ExecResult result) {
        if (result.isException()) {
            assert result.getException() != null;
            result.getException().printStackTrace();
        }
        return new PolyglotResult(languageType, code, result.getLog(), result.getReturnValue(), "server error.");
    }

    /**
     * @deprecated 20.1.0現在、contextにinputStream、outputStreamを設定してもllvmでは認識してくれないので、graalvmのlliコマンドを実行する
     */
    @Deprecated(forRemoval = true)
    private PolyglotResult lli(String code, String[] stdin) {
        final File llvmFolder = new File("llvmFiles");
        final String llvmUUID = UUID.randomUUID().toString();
        final File llvmFile = new File(llvmFolder, llvmUUID + ".c");
        try (final FileOutputStream fos = new FileOutputStream(llvmFile);
             final PrintWriter printWriter = new PrintWriter(fos, true, StandardCharsets.UTF_8)
        ) {
            printWriter.print(code);
        } catch (IOException e) {
            return new PolyglotResult(LanguageType.LLVM, code, "", null, e.getLocalizedMessage());
        }

        final Process clangProcess;
        try {
            clangProcess = new ProcessBuilder(
                    System.getProperty("java.home") + "/languages/llvm/native/bin/clang",
                    llvmFile.getName(), "-o", llvmUUID, "-w"
            ).directory(llvmFolder).start();
        } catch (IOException e) {
            return serverError(LanguageType.LLVM, code, e);
        }
        final ISThread buildIST = new ISThread();
        try {
            buildIST.setInputStream(clangProcess.getErrorStream());
            buildIST.start();
            clangProcess.waitFor(APIConstants.LLVM_COMPILE_TIME, TimeUnit.SECONDS);
            aliveDestroy(clangProcess);
            buildIST.join();
        } catch (InterruptedException e) {
            return new PolyglotResult(LanguageType.LLVM, code, "", null, e.getLocalizedMessage());
        } finally {
            aliveDestroy(clangProcess);
        }
        final StringJoiner errorJoiner = new StringJoiner("\n");
        buildIST.getStringList().forEach(errorJoiner::add);
        if (errorJoiner.length() != 0) {
            return new PolyglotResult(LanguageType.LLVM, code, "", null, errorJoiner.toString());
        }

        final Process process;
        try {
            process = new ProcessBuilder("lli", llvmUUID).directory(llvmFolder).start();
        } catch (IOException e) {
            return serverError(LanguageType.LLVM, code, e);
        }
        final ISThread ist = new ISThread();
        final ISThread est = new ISThread();
        final int exitValue;
        try {
            ist.setInputStream(process.getInputStream());
            est.setInputStream(process.getErrorStream());
            ist.start();
            est.start();
            final OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
            for (final String str : stdin) {
                writer.append(str).append("\n");
            }
            writer.flush();
            process.waitFor(APIConstants.LLVM_RUN_TIME, TimeUnit.SECONDS);
            aliveDestroy(process);
            ist.join();
            est.join();
            exitValue = process.exitValue();
        } catch (IOException | InterruptedException e) {
            return serverError(LanguageType.LLVM, code, e);
        } finally {
            aliveDestroy(process);
        }
        final StringJoiner resultJoiner = new StringJoiner("\n");
        final boolean isError = !est.getStringList().isEmpty();
        (isError ? est.getStringList() : ist.getStringList()).forEach(resultJoiner::add);
        return new PolyglotResult(
                LanguageType.LLVM,
                code,
                isError ? "" : resultJoiner.toString(),
                String.valueOf(exitValue),
                isError ? resultJoiner.toString() : null
        );
    }

    private void aliveDestroy(Process process) {
        if (process.isAlive()) {
            process.destroy();
        }
    }

}
