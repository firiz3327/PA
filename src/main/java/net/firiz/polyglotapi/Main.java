package net.firiz.polyglotapi;

import net.firiz.polyglotapi.exec.result.ExecResult;
import net.firiz.polyglotapi.language.LanguageType;
import org.graalvm.polyglot.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Supplier;

public final class Main {

    public static void main(String[] args) {
        new Main().start(args);
    }

    private void start(String[] args) {
        final LanguageType languageType = LanguageType.search(args[0]);
        final String code = new String(Base64.getDecoder().decode(args[1].getBytes()), StandardCharsets.UTF_8);
        final String[] stdin = args.length >= 3 ? Arrays.copyOfRange(args, 2, args.length) : new String[0];
        if (languageType == LanguageType.LLVM) {
            lli(code, stdin);
            return;
        }
        final PolyglotResult polyglotResult = ((Supplier<PolyglotResult>) () -> {
            try (final ByteArrayOutputStream contextStream = new ByteArrayOutputStream();
                 final InputStream stdinStream = new ByteArrayInputStream(String.join(System.lineSeparator(), Arrays.asList(stdin)).getBytes(StandardCharsets.UTF_8))) {
                final String returnValue;
                final String error;
                if (!languageType.isSupported()) {
                    returnValue = null;
                    error = "'" + languageType.getName() + "' language is not supported.";
                } else {
                    // llvmを実行したときのコンソールログがcontextStreamに出力されていない？
                    try (final Context context = Context.newBuilder()
                            .arguments(languageType.getName(), stdin)
                            .in(stdinStream)
                            .out(contextStream)
                            .err(contextStream)
                            .allowAllAccess(true)
                            .build()) {
                        final ExecResult result = languageType.getExec().exec(
                                languageType,
                                code,
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
                }
                return new PolyglotResult(languageType, code, contextStream.toString(StandardCharsets.UTF_8), returnValue, error);
            } catch (IOException e) {
                return serverError(languageType, code, e);
            }
        }).get();
        if (polyglotResult.getError() != null && !polyglotResult.getError().isEmpty()) {
            System.err.println(polyglotResult.getError());
        } else {
            System.out.println(polyglotResult.getReturnValue());
//            if (languageType != LanguageType.LLVM) {
            System.out.println(polyglotResult.getLog());
//            }
        }
    }

    private PolyglotResult serverError(LanguageType languageType, String code, Exception e) {
        return serverError(languageType, code, new ExecResult(e, null, null, null));
    }

    private PolyglotResult serverError(LanguageType languageType, String code, ExecResult result) {
        result.getException().printStackTrace();
        return new PolyglotResult(languageType, code, result.getLog(), result.getReturnValue(), "server error.");
    }

    /**
     * まじでinputstreamとoutputstream設定してもllvmだと機能しないのどうにかしてくれgraalvmさんよ、、なんか方法ある？
     * @param code
     * @param stdin
     */
    @Deprecated(forRemoval = true)
    private void lli(String code, String[] stdin) {
        final File llvmFolder = new File("llvmFiles");
        final String llvmUUID = UUID.randomUUID().toString();
        final File llvmFile = new File(llvmFolder, llvmUUID + ".c");
        try (final FileOutputStream fos = new FileOutputStream(llvmFile);
             final PrintWriter printWriter = new PrintWriter(fos, true, StandardCharsets.UTF_8)
        ) {
            printWriter.print(code);
        } catch (IOException e) {
            System.err.println("server error 1");
            return;
        }

        final Runtime runtime = Runtime.getRuntime();
        final String clangFile = System.getProperty("java.home") + "/languages/llvm/native/bin/clang";
        Process clangExec;
        try {
            clangExec = runtime.exec(new String[]{clangFile, llvmFile.getName(), "-o", llvmUUID, "-w"}, null, llvmFolder);
            clangExec.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("server error 2");
            return;
        }

        final StringJoiner joiner = new StringJoiner("\n");
        try (final InputStreamReader sr = new InputStreamReader(clangExec.getErrorStream());
             final BufferedReader br = new BufferedReader(sr)) {
            String str;
            while ((str = br.readLine()) != null) {
                joiner.add(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (joiner.length() != 0) {
            System.err.println("server error 3");
            return;
        }

        final ProcessBuilder pb = new ProcessBuilder("lli", llvmUUID);
        pb.directory(llvmFolder);
        final ISThread ist = new ISThread();
        final ISThread est = new ISThread();
        final Process process;
        try {
            process = pb.start();
            ist.setInputStream(process.getInputStream());
            est.setInputStream(process.getErrorStream());
            ist.start();
            est.start();
            final OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
            for(final String str : stdin) {
                writer.append(str).append("\n");
            }
            writer.flush();
            process.waitFor();
            ist.join();
            est.join();
            System.out.println(process.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        final StringJoiner j = new StringJoiner("\n");
        (est.getStringList().isEmpty() ? ist.getStringList() : est.getStringList()).forEach(j::add);
        System.out.println(j);
    }

}
