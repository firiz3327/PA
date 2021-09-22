package net.firiz.polyglotapi.language;

import junit.framework.TestCase;
import net.firiz.polyglotapi.APIConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LanguageTypeTest extends TestCase {

    @Test
    void search() {
        System.out.println(APIConstants.INSTALLED_LANGUAGES);
        Assertions.assertEquals(LanguageType.UNKNOWN, LanguageType.search("abc"));
        Assertions.assertEquals(LanguageType.JAVA, LanguageType.search("java"));
        Assertions.assertEquals(LanguageType.LLVM, LanguageType.search("c++"));
    }
}