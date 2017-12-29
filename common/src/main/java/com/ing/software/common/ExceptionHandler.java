package com.ing.software.common;

import com.annimon.stream.function.Consumer;

/**
 * Handle exceptions and reuse the same catch logic for multiple blocks of code.
 * The finally pattern is supported through concatenation:
 * handler.tryRun(() -> {...}).tryRun(() -> {...}) ...
 * @author Riccardo Zaglia
 */
public class ExceptionHandler {

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }

    private Consumer<Exception> handler;
    private boolean exceptionOccurred;

    public ExceptionHandler(Consumer<Exception> handler) {
        this.handler = handler;
    }

    public ExceptionHandler tryRun(CheckedRunnable runnable) {
        ExceptionHandler excHdlr = new ExceptionHandler(handler);
        if (!exceptionOccurred) {
            try {
                runnable.run();
            } catch (Exception e) {
                excHdlr.exceptionOccurred = true;
                handler.accept(e);
            }
        }
        return excHdlr;
    }
}
