package io.joranvanbelle.tiemeplan.util;

import java.util.concurrent.atomic.AtomicBoolean;

public final class CancellationToken {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void throwIfCancelled() {
        if (cancelled.get()) {
            throw new CancellationException();
        }
    }

    public static class CancellationException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
