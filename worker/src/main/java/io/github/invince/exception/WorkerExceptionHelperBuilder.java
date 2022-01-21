package io.github.invince.exception;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Helper to check error.
 * You can do things like:
 *      WorkerError.verify("OK")
 *        .nonNull(person, "hello")
 *        .notEmpty(list)
 */
@Slf4j
class WorkerExceptionHelperBuilder {

    public final Function<String, WorkerException> exceptionToThrow;

    public WorkerExceptionHelperBuilder(Function<String, WorkerException> exceptionToThrow) {
        this.exceptionToThrow = exceptionToThrow;
    }

    public class WorkerExceptionHelper {
        private String message;

        WorkerExceptionHelper(String message) {
            this.message = message;
        }

        public WorkerExceptionHelper isTrue(boolean b) {
            if(!b) {
                throw exceptionToThrow.apply(message);
            }
            return this;
        }

        public WorkerExceptionHelper isFalse(boolean b) {
            if(b) {
                throw exceptionToThrow.apply(message);
            }
            return this;
        }

        public WorkerExceptionHelper notEmpty(String str) {
            if(str == null || "".equals(str)) {
                throw exceptionToThrow.apply(message);
            }
            return this;
        }

        public WorkerExceptionHelper notEmpty(Collection e) {
            if(e == null || e.isEmpty()) {
                throw exceptionToThrow.apply(message);
            }
            return this;
        }

        public WorkerExceptionHelper notEmpty(Map e) {
            if(e == null || e.isEmpty()) {
                throw exceptionToThrow.apply(message);
            }
            return this;
        }

        public WorkerExceptionHelper nonNull(Object... list) {
            if(list == null) {
                throw exceptionToThrow.apply(message);
            }
            for (Object o : list) {
                if(o == null) {
                    throw exceptionToThrow.apply(message);
                }
            }
            return this;
        }

        public WorkerExceptionHelper successfullyRun(Runnable runnable) {
            if(runnable == null) {
                throw exceptionToThrow.apply(message);
            }
            try {
                runnable.run();
            } catch (Exception e) {
                log.debug(e.getMessage(), e);
                throw exceptionToThrow.apply(message);
            }
            return this;
        }

        public <E> WorkerExceptionHelper successfullyConsume(Consumer<E> consumer, E param) {
            if(consumer == null) {
                throw exceptionToThrow.apply(message);
            }
            try {
                consumer.accept(param);
            } catch (Exception e) {
                log.debug(e.getMessage(), e);
                throw exceptionToThrow.apply(message);
            }
            return this;
        }

        public <A,B> WorkerExceptionHelper successfullyConsume(BiConsumer<A,B> consumer, A param1, B param2) {
            if(consumer == null) {
                throw exceptionToThrow.apply(message);
            }
            try {
                consumer.accept(param1, param2);
            } catch (Exception e) {
                log.debug(e.getMessage(), e);
                throw exceptionToThrow.apply(message);
            }
            return this;
        }
    }

    WorkerExceptionHelper build(String message) {
        return new WorkerExceptionHelper(message);
    }
}
