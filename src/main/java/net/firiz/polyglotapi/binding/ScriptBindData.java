package net.firiz.polyglotapi.binding;

import org.graalvm.polyglot.HostAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScriptBindData {

    private final String[] stdin;
    private int i = 0;

    public ScriptBindData(String[] stdin) {
        this.stdin = stdin;
    }

    @HostAccess.Export
    public List<String> getList() {
        return new ArrayList<>(Arrays.asList(stdin));
    }

    @HostAccess.Export
    public String getValue() {
        final String data = stdin[i];
        i++;
        return data;
    }

    @HostAccess.Export
    public int getIntValue() {
        return Integer.parseInt(getValue());
    }

    @HostAccess.Export
    public long getLongValue() {
        return Long.parseLong(getValue());
    }

    @HostAccess.Export
    public byte getByteValue() {
        return Byte.parseByte(getValue());
    }

    @HostAccess.Export
    public double getDoubleValue() {
        return Double.parseDouble(getValue());
    }

    @HostAccess.Export
    public float getFloatValue() {
        return Float.parseFloat(getValue());
    }

}
