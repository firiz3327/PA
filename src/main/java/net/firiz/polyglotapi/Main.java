package net.firiz.polyglotapi;

import net.firiz.polyglotapi.exec.result.ExecResult;
import net.firiz.polyglotapi.language.LanguageType;
import org.graalvm.polyglot.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Supplier;

public final class Main {

    public static void main(String[] args) {
        final LanguageType languageType = LanguageType.search(args[0]);
        final String code = new String(Base64.getDecoder().decode(args[1].getBytes()), StandardCharsets.UTF_8);
        final PolyglotResult polyglotResult = ((Supplier<PolyglotResult>) () -> {
            try (final ByteArrayOutputStream contextStream = new ByteArrayOutputStream()) {
                final String returnValue;
                final String error;
                if (!languageType.isSupported()) {
                    returnValue = null;
                    error = "'" + languageType.getName() + "' language is not supported.";
                } else {
                    // llvmを実行したときのコンソールログがcontextStreamに出力されていない？
                    try (final Context context = Context.newBuilder().out(contextStream).err(contextStream).allowAllAccess(true).build()) {
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
        if(polyglotResult.getError() != null) {
            System.out.println("error: " + polyglotResult.getError());
        }
        System.out.println(polyglotResult.getReturnValue());
        if (polyglotResult.getLog() != null && !polyglotResult.getLog().isEmpty()) {
            System.out.println(polyglotResult.getLog());
        }
    }

    private static PolyglotResult serverError(LanguageType languageType, String code, Exception e) {
        return serverError(languageType, code, new ExecResult(e, null, null, null));
    }

    private static PolyglotResult serverError(LanguageType languageType, String code, ExecResult result) {
//        logger.log(Level.SEVERE, result.getException().toString(), result.getException());
        return new PolyglotResult(languageType, code, result.getLog(), result.getReturnValue(), "server error.");
    }

//    public static Logger getLogger() {
//        return logger;
//    }

}
