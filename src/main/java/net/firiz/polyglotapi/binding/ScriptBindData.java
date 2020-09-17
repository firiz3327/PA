package net.firiz.polyglotapi.binding;

import net.firiz.polyglotapi.project.Project;
import org.graalvm.polyglot.HostAccess;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScriptBindData {

    @Nullable
    private final Project project;
    private final String[] stdin;
    private int i = 0;

    public ScriptBindData(@Nullable Project project, String[] stdin) {
        this.project = project;
        this.stdin = stdin;
    }

    @HostAccess.Export
    public File getFile(String name) {
        if (project != null) {
            return new File(project.getFolder(), name);
        }
        return new File(name);
    }

    @HostAccess.Export
    public String getPath(String name) {
        return getFile(name).getAbsolutePath();
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
