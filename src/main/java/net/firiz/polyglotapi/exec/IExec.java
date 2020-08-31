package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.json.PolyglotResult;
import org.jetbrains.annotations.NotNull;

public interface IExec {

    PolyglotResult exec(@NotNull final String code, @NotNull String[] bindData);

}
