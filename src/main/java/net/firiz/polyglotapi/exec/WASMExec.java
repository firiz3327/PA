package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.exec.result.ExecResult;
import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.language.LanguageType;
import net.firiz.polyglotapi.project.Project;
import org.apache.commons.io.FileUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.ByteSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

// require Emscripten
// https://emscripten.org/docs/tools_reference/emcc.html
// [./emsdk install latest, ./emsdk activate latest]
// JAVA_HOMEが変更されてしまうので注意
public class WASMExec extends ContextExec implements ConsoleRunner {

    public WASMExec(@NotNull LanguageType languageType) {
        super(languageType);
    }

    @Override
    public @NotNull Object exec(@NotNull String code, @NotNull String[] bindData, @NotNull Context context, @NotNull ByteArrayOutputStream contextStream, @Nullable Project project) {
//        if (project == null) {
            return new PolyglotResult(LanguageType.WASM, code, "", null, "project is null.");
//        } else {
//            final String uuid = project.getUUID().toString();
//            final File file = new File(project.getFolder(), uuid + ".c");
//            final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
//            final PolyglotResult buildResult = buildNonNullProject(LanguageType.WASM, project, code, file, new String[]{
//                    "C:\\Users\\Firiz\\Desktop\\jdk\\emsdk-2.0.29\\upstream\\emscripten\\emcc.bat",
//                    "-o",
//                    uuid + ".wasm",
//                    file.getName()
//            });
//            if (buildResult == null) {
//                String result = null;
//                String error = null;
//                final Source source;
//                try {
//                    source = Source.newBuilder(languageType.getName(), ByteSequence.create(FileUtils.readFileToByteArray(file)), uuid).build();
//                } catch (IOException e) {
//                    return PolyglotResult.serverError(languageType, code, e);
//                }
//                context.eval(source);
//                final Value main = context.getBindings(languageType.getName()).getMember("main").getMember("_start");
////                main.execute();
//                return new ExecResult(
//                        contextStream.toString(StandardCharsets.UTF_8),
//                        result,
//                        error
//                );
//            }
//            return buildResult;
//        }
    }

}
