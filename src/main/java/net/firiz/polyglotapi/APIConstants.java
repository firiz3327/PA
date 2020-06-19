package net.firiz.polyglotapi;

import org.graalvm.polyglot.Context;

import java.util.Set;

public final class APIConstants {

    private APIConstants() {
    }

    public static final Set<String> INSTALLED_LANGUAGES;

    static {
        try (final Context context = Context.newBuilder().allowAllAccess(true).build()) {
            INSTALLED_LANGUAGES = context.getEngine().getLanguages().keySet();
        }
    }
}
