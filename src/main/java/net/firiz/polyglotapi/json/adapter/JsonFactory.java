package net.firiz.polyglotapi.json.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import net.firiz.polyglotapi.language.LanguageType;

public class JsonFactory implements TypeAdapterFactory {

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().registerTypeAdapterFactory(new JsonFactory()).create();

    private JsonFactory() {
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        final Class<T> rawType = cast(typeToken.getRawType());
        if (rawType == LanguageType.class) {
            return cast(StringTypeAdapter.createAdapter(
                    gson,
                    LanguageType::getName,
                    LanguageType::search
            ));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

}
