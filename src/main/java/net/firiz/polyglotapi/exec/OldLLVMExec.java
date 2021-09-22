package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.APIConstants;
import net.firiz.polyglotapi.exec.result.ExecResult;
import net.firiz.polyglotapi.language.LanguageType;
import net.firiz.polyglotapi.project.Project;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.UUID;

@Deprecated(forRemoval = true)
public class OldLLVMExec extends ContextExec {

    public OldLLVMExec() {
        super(LanguageType.LLVM);
    }

    public @NotNull ExecResult exec(@NotNull final String code, @NotNull String[] bindData, @NotNull final Context context, @NotNull final ByteArrayOutputStream contextStream, @Nullable Project project) {
        /*
        final String llvmUUID = UUID.randomUUID().toString();
        final File llvmFile = new File(APIConstants.LLVM_FOLDER, llvmUUID + ".c");
        try (final FileOutputStream fos = new FileOutputStream(llvmFile);
             final PrintWriter printWriter = new PrintWriter(fos, true, StandardCharsets.UTF_8)
        ) {
            printWriter.print(code);
        } catch (IOException e) {
            return errorResult(e, contextStream);
        }

        final Runtime runtime = Runtime.getRuntime();
        final String clangFile = System.getProperty("java.home") + "/languages/llvm/native/bin/clang";
        Process clangExec;
        try {
            clangExec = runtime.exec(new String[]{clangFile, llvmFile.getName(), "-o", llvmUUID, "-w"}, null, APIConstants.LLVM_FOLDER);
            clangExec.waitFor();
        } catch (IOException | InterruptedException e) {
            return errorResult(e, contextStream);
        }
        final StringJoiner joiner = new StringJoiner("\n");
        try (final InputStreamReader sr = new InputStreamReader(clangExec.getErrorStream());
                final BufferedReader br = new BufferedReader(sr)) {
            String str;
            while ((str = br.readLine()) != null) {
                joiner.add(str);
            }
        } catch (IOException e) {
            return errorResult(e, contextStream);
        }
        if (joiner.length() != 0) {
            return new ExecResult(null, "", null, joiner.toString());
        }
        Value value = null;
        String error = null;
        try {
            value = context.eval(Source.newBuilder(LanguageType.LLVM.getName(), new File(APIConstants.LLVM_FOLDER, llvmUUID)).build()).execute();
        } catch (Exception e) {
            error = e.getLocalizedMessage();
        }
        return new ExecResult(
                contextStream.toString(StandardCharsets.UTF_8),
                value == null ? null : value.toString(),
                error
        );
         */
        return null;
    }

}
