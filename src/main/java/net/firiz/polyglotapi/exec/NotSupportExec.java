package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NotSupportExec implements IExec {

    @Override
    public @NotNull PolyglotResult exec(@NotNull String code, @NotNull String[] bindData, @Nullable Project project) {
        throw new UnsupportedOperationException("not support language type.");
    }

}
