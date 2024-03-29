package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.APIConstants;
import net.firiz.polyglotapi.binding.ScriptBindData;
import net.firiz.polyglotapi.exec.result.ExecResult;
import net.firiz.polyglotapi.language.LanguageType;
import net.firiz.polyglotapi.project.Project;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class ScriptExec extends ContextExec {

    public ScriptExec(@NotNull LanguageType languageType) {
        super(languageType);
    }

    @Override
    public @NotNull ExecResult exec(@NotNull String code, @NotNull String[] bindData, @NotNull Context context, @NotNull ByteArrayOutputStream contextStream, @NotNull Project project) {
        String result = null;
        String error = null;
        final Value global = context.getBindings(languageType.getName());
        final ScriptBindData scriptBindData = new ScriptBindData(project, bindData);
        global.putMember(APIConstants.BIND_MEMBER, scriptBindData);
        try {
            final Value value = context.eval(languageType.getName(), code);
            if (global.hasMember("main") && global.getMember("main").canExecute()) {
                result = global.getMember("main").execute().as(Object.class).toString();
            } else {
                result = value.toString();
            }
        } catch (Exception e) {
            error = e.getLocalizedMessage();
        }
        return new ExecResult(
                contextStream.toString(StandardCharsets.UTF_8),
                result,
                error
        );
    }

}
