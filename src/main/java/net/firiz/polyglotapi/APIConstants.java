package net.firiz.polyglotapi;

import org.graalvm.polyglot.Context;

import java.util.Collections;
import java.util.Set;

public final class APIConstants {

    private APIConstants() {
    }

    public static final Set<String> INSTALLED_LANGUAGES;
    public static final String BIND_MEMBER = "sbd";

    public static final int LLVM_COMPILE_TIME = 5;
    public static final int LLVM_RUN_TIME = 5;

    static {
        try (final Context context = Context.newBuilder().allowAllAccess(true).build()) {
            INSTALLED_LANGUAGES = Collections.unmodifiableSet(context.getEngine().getLanguages().keySet());
        }
    }
}
