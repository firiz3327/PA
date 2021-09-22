package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.json.PolyglotResult;
import net.firiz.polyglotapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface IExec {

    @NotNull
    PolyglotResult exec(@NotNull final String code, @NotNull String[] bindData, @NotNull Project project);

}
