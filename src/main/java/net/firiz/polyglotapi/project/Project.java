package net.firiz.polyglotapi.project;

import net.firiz.polyglotapi.APIConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Base64;
import java.util.UUID;

public class Project {

    @NotNull
    private final UUID uuid;
    @NotNull
    private final File folder;
    @Nullable
    private final String fileName;
    @Nullable
    private final String file64;
    private final boolean isPPAPSwing;

    public Project(@NotNull UUID uuid, @Nullable String fileName, @Nullable String file64, boolean isPPAPSwing) {
        this.uuid = uuid;
        this.folder = new File(APIConstants.PROJECT_FOLDER, uuid.toString());
        this.fileName = fileName;
        this.file64 = file64;
        this.isPPAPSwing = isPPAPSwing;
    }

    public void init() throws IOException {
        if (folder.mkdir() && fileName != null && file64 != null && !fileName.isEmpty() && !file64.isEmpty()) {
            final File file = new File(folder, fileName);
            try (final FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(Base64.getDecoder().decode(file64));
                fos.flush();
            }
        }
    }

    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    @NotNull
    public File getFolder() {
        return folder;
    }

    public boolean isPPAPSwing() {
        return isPPAPSwing;
    }
}
