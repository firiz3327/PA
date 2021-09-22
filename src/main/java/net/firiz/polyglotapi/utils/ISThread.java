package net.firiz.polyglotapi.utils;

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class ISThread extends Thread {

    private final List<String> list = new ArrayList<>();
    private final Queue<String> queue;

    private BufferedReader br;

    public ISThread(@Nullable Queue<String> queue) {
        this.queue = queue;
    }

    public void setInputStream(InputStream inputStream) {
        br = new BufferedReader(new InputStreamReader(inputStream));
    }

    @Override
    public synchronized void start() {
        if (br == null) {
            throw new NullPointerException("InputStream is not initialized.");
        }
        super.start();
    }

    @Override
    public void run() {
        try {
            for (; ; ) {
                String line = br.readLine();
                if (line == null) break;
                queue.add(line);
                list.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getStringList() {
        return Collections.unmodifiableList(list);
    }
}
