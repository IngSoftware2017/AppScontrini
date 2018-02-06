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
    public interface ThrowableRunnable {
        void run() throws Exception;
    }

    private Consumer<Exception> handler;
    private boolean exceptionOccurred;

    public ExceptionHandler(Consumer<Exception> handler) { this.handler = handler; }

    /**
     * Use as try or finally block.
     * @param runnable code to be handled
     * @return ExceptionHandler, to allow concatenation
     */
    public ExceptionHandler tryRun(ThrowableRunnable runnable) {
        //I create a new disposable instance of ExceptionHandler so the information that an exception happened
        // is limited to a single session of try-finally concatenation.
        // The field "exceptionOccurred" of the original instance is never modified.
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
