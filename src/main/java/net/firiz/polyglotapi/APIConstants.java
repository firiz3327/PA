package net.firiz.polyglotapi;

import org.graalvm.polyglot.Context;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public final class APIConstants {

    private APIConstants() {
    }

    public static final Set<String> INSTALLED_LANGUAGES;
    public static final String BIND_MEMBER = "sbd";

    public static final int CONSOLE_COMPILE_TIME = 5;
    public static final int CONSOLE_RUN_TIME = 5;

    public static final File LLVM_FOLDER = new File("llvmFiles");
    public static final File JAVA_FOLDER = new File("javaFiles");

    static {
        try (final Context context = Context.newBuilder().allowAllAccess(true).build()) {
            INSTALLED_LANGUAGES = Collections.unmodifiableSet(context.getEngine().getLanguages().keySet());
        }
        LLVM_FOLDER.mkdir();
        JAVA_FOLDER.mkdir();
    }
}
