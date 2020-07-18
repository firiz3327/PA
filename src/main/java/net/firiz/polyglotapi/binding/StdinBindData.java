package net.firiz.polyglotapi.binding;

import org.graalvm.polyglot.HostAccess;

public class StdinBindData {

    private final String[] stdin;
    private int i = 0;

    public StdinBindData(String[] stdin) {
        this.stdin = stdin;
    }

    @HostAccess.Export
    public String getValue() {
        final String data = stdin[i];
        i++;
        return data;
    }

}
