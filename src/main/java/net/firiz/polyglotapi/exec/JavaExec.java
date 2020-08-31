package net.firiz.polyglotapi.exec;

import javassist.*;
import javassist.bytecode.ClassFile;
import net.firiz.polyglotapi.APIConstants;
import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.language.LanguageType;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.UUID;

public class JavaExec extends ConsoleExec {

    @Override
    public PolyglotResult exec(@NotNull String code, @NotNull String[] bindData) {
        final String uuid = UUID.randomUUID().toString();
        final File uuidFolder = new File(APIConstants.JAVA_FOLDER, uuid);
        if (!uuidFolder.mkdir()) {
            return new PolyglotResult(LanguageType.JAVA, code, "", null, "duplicated uuid.");
        }
        final File javaFile = new File(uuidFolder, "Main.java");
        try (final FileOutputStream fos = new FileOutputStream(javaFile);
             final PrintWriter printWriter = new PrintWriter(fos, true, StandardCharsets.UTF_8)
        ) {
            printWriter.print(code);
        } catch (IOException e) {
            return new PolyglotResult(LanguageType.JAVA, code, "", null, e.getLocalizedMessage());
        }

        final ConsoleResult buildResult = process(
                uuidFolder,
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

        final ClassPool classPool = ClassPool.getDefault();
        try {
            final ClassFile classFile = new ClassFile(new DataInputStream(new FileInputStream(new File(uuidFolder, "Main.class"))));
            final CtClass ctClass = classPool.makeClass(classFile);
            final Class<?> aClass = ctClass.toClass();
            final Method main = aClass.getDeclaredMethod("main", String[].class);
            main.setAccessible(true);

            final String log;
            final String errLog;
            final InputStream defaultIN = System.in;
            final PrintStream defaultERR = System.err;
            final PrintStream defaultOUT = System.out;
            try (final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 final PrintStream out = new PrintStream(byteOut, true);
                 final ByteArrayOutputStream byteErr = new ByteArrayOutputStream();
                 final PrintStream err = new PrintStream(byteErr, true);
                 final InputStream stdinStream = new ByteArrayInputStream(String.join(System.lineSeparator(), Arrays.asList(bindData)).getBytes(StandardCharsets.UTF_8))
            ) {
                System.setIn(stdinStream);
                System.setOut(out);
                System.setErr(err);
                main.invoke(null, (Object) bindData);
                log = byteOut.toString(StandardCharsets.UTF_8);
                errLog = byteErr.toString(StandardCharsets.UTF_8);
            }
            System.setIn(defaultIN);
            System.setOut(defaultOUT);
            System.setErr(defaultERR);
            final boolean isError = !errLog.isEmpty();
            return new PolyglotResult(
                    LanguageType.JAVA,
                    code,
                    isError ? "" : log,
                    "0",
                    isError ? errLog : null
            );
        } catch (IOException | CannotCompileException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return new PolyglotResult(LanguageType.JAVA, code, "", null, e.getLocalizedMessage());
        }
    }

}
