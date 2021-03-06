package com.ilummc.wayback.schedules;

import com.ilummc.wayback.WaybackException;
import com.ilummc.wayback.tasks.Executable;

import java.util.Objects;

public abstract class ProgressedSchedule implements Runnable {

    int id = 0;

    private volatile boolean running, complete;

    private ProgressedSchedule next;

    @Override
    public void run() {
        try {
            complete = false;
            running = true;
            execute();
            complete = true;
            if (next() != null) {
                next().addToQueue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!(e instanceof WaybackException))
                throw new WaybackException(e);
        } finally {
            running = false;
        }
    }

    protected abstract void execute() throws Exception;

    public abstract String detail();

    public abstract String name();

    public abstract double progress();

    public abstract boolean terminate();

    public abstract void forceTerminate();

    public abstract long eta();

    public abstract ProgressedSchedule copyOfRetry();

    public String id() {
        return String.valueOf(id);
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isComplete() {
        return complete;
    }

    private ProgressedSchedule next() {
        return next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgressedSchedule that = (ProgressedSchedule) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void addToQueue() {
        this.id = WaybackSchedules.nextId();
        WaybackSchedules.instance().execute(this);
    }

    public static ProgressedSchedule of(Executable executable) {
        return new SimpleProgressedSchedule(executable);
    }

    private static class SimpleProgressedSchedule extends ProgressedSchedule {

        private Executable executable;

        private SimpleProgressedSchedule(Executable executable) {
            this.executable = executable;
        }

        @Override
        protected void execute() throws Exception {
            executable.execute();
        }

        @Override
        public String detail() {
            return executable.detail();
        }

        @Override
        public String name() {
            return executable.name();
        }

        @Override
        public double progress() {
            return executable.progress();
        }

        @Override
        public boolean terminate() {
            return executable.terminate();
        }

        @Override
        public void forceTerminate() {
            executable.forceTerminate();
        }

        @Override
        public long eta() {
            return executable.eta();
        }

        @Override
        public ProgressedSchedule copyOfRetry() {
            return executable.schedule();
        }
    }

}

