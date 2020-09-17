package net.firiz.polyglotapi;

import net.firiz.polyglotapi.json.JsonArgs;
import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.json.adapter.JsonFactory;
import net.firiz.polyglotapi.language.LanguageType;
import net.firiz.polyglotapi.project.Project;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

public final class Main {

    public static void main(String[] args) {
        new Main().start(args);
    }

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
        final JsonArgs json = JsonFactory.fromJson(args[0], JsonArgs.class);
        json.init();
//        final LanguageType languageType = LanguageType.search(args[0]);
//        final String code = new String(Base64.getDecoder().decode(args[1].getBytes()), StandardCharsets.UTF_8);
//        final String[] stdin = args.length >= 3 ? Arrays.copyOfRange(args, 2, args.length) : new String[0];
        final LanguageType languageType = json.getLanguageType();
        final Project project;
        if (json.getLanguageType().isAutoProject() || json.isAutoProject()) {
            final UUID uuid = UUID.randomUUID();
            project = new Project(uuid, json.getFileName(), json.getFile64());
            try {
                project.init();
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
                return;
            }
        } else {
            project = null;
        }
        final PolyglotResult polyglotResult = ((Supplier<PolyglotResult>) () -> {
            if (!languageType.isSupported()) {
                return new PolyglotResult(languageType, json.getCode(), "", null, "'" + languageType.getName() + "' language is not supported.");
            }
            return languageType.getExec().exec(json.getCode(), json.getStdin(), project);
        }).get();
        if (polyglotResult.isError()) {
            System.err.println(polyglotResult.getErrorMsg());
        } else {
            System.out.println(JsonFactory.toJson(polyglotResult));
        }
    }
}
