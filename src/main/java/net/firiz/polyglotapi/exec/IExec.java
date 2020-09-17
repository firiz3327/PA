package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IExec {

    PolyglotResult exec(@NotNull final String code, @NotNull String[] bindData, @Nullable Project project);

}
