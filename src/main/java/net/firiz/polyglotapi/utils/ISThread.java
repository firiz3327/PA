package net.firiz.polyglotapi.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ISThread extends Thread {

    private final List<String> list = new ArrayList<>();

    private BufferedReader br;

    public ISThread() {
    }

    public ISThread(InputStream inputStream) {
        setInputStream(inputStream);
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
