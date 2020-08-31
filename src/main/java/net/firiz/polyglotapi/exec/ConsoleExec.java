package net.firiz.polyglotapi.exec;

import net.firiz.polyglotapi.EConsumer;
import net.firiz.polyglotapi.ISThread;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public abstract class ConsoleExec extends Exec {

    protected ConsoleResult process(File directory, int waitSecond, String... command) {
        return process(directory, waitSecond, process -> {}, command);
    }

    protected ConsoleResult process(File directory, int waitSecond, EConsumer<Process> processConsumer, String... command) {
        final Process process;
        try {
            process = new ProcessBuilder(command).directory(directory).start();
        } catch (IOException e) {
            return new ConsoleResult(e, true);
        }
        final ISThread ist = new ISThread();
        final ISThread est = new ISThread();
        final int exitValue;
        try {
            ist.setInputStream(process.getInputStream());
            est.setInputStream(process.getErrorStream());
            ist.start();
            est.start();
            processConsumer.accept(process);
            process.waitFor(waitSecond, TimeUnit.SECONDS);
            aliveDestroy(process);
            ist.join();
            est.join();
            exitValue = process.exitValue();
        } catch (Exception e) {
            return new ConsoleResult(e, false);
        } finally {
            aliveDestroy(process);
        }
        return new ConsoleResult(ist, est, exitValue);
    }

    protected void aliveDestroy(Process process) {
        if (process.isAlive()) {
            process.destroy();
        }
    }

    protected static class ConsoleResult {
        private final ISThread ist;
        private final ISThread est;
        private final int exitValue;
        private final Exception e;
        private final boolean createProcessError;

        private ConsoleResult(ISThread ist, ISThread est, int exitValue) {
            this.ist = ist;
            this.est = est;
            this.exitValue = exitValue;
            this.e = null;
            this.createProcessError = false;
        }

        private ConsoleResult(Exception e, boolean createProcessError) {
            this.ist = null;
            this.est = null;
            this.exitValue = 1;
            this.e = e;
            this.createProcessError = createProcessError;
        }

        public boolean hasException() {
            return e != null;
        }

        public Exception getException() {
            return e;
        }

        public boolean isCreateProcessError() {
            return createProcessError;
        }

        public ISThread getIst() {
            return ist;
        }

        public ISThread getEst() {
            return est;
        }

        public int getExitValue() {
            return exitValue;
        }
    }
}
