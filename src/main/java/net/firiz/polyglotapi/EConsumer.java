package net.firiz.polyglotapi;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface EConsumer<T> {

    void accept(T t) throws Exception;

    default EConsumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
    }
}
