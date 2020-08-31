package net.firiz.polyglotapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.language.LanguageType;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

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
            }
            return languageType.getExec().exec(code, stdin);
        }).get();
        if (polyglotResult.isError()) {
            System.err.println(polyglotResult.getErrorMsg());
        } else {
            System.out.println(gson.toJson(polyglotResult));
        }
    }
}
