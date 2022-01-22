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

    /**
     * Error message to exception function
     */
    public final Function<String, WorkerException> exceptionToThrow;

    public WorkerExceptionHelperBuilder(Function<String, WorkerException> exceptionToThrow) {
        this.exceptionToThrow = exceptionToThrow;
    }

    /**
     * Helper to check error.
     */
    public class WorkerExceptionHelper {
        /**
         * Error message
         */
        private final String message;

        WorkerExceptionHelper(String message) {
            this.message = message;
        }

        /**
         * if it's not true, we throw exception
         * @param b boolean to check
         * @return WorkerExceptionHelper
         */
        public WorkerExceptionHelper isTrue(boolean b) {
            if(!b) {
                throw exceptionToThrow.apply(message);
            }
            return this;
        }


        /**
         * if it's not false, we throw exception
         * @param b boolean to check
         * @return WorkerExceptionHelper
         */
        public WorkerExceptionHelper isFalse(boolean b) {
            if(b) {
                throw exceptionToThrow.apply(message);
            }
            return this;
        }

        /**
         * if string is empty (null or ""), we throw exception
         * @param str String to check
         * @return WorkerExceptionHelper
         */
        public WorkerExceptionHelper notEmpty(String str) {
            if(str == null || "".equals(str)) {
                throw exceptionToThrow.apply(message);
            }
            return this;
        }

        /**
         * if collection is empty (null or empty), we throw exception
         * @param e collection to check
         * @return WorkerExceptionHelper
         */
        public WorkerExceptionHelper notEmpty(Collection e) {
            if(e == null || e.isEmpty()) {
                throw exceptionToThrow.apply(message);
            }
            return this;
        }

        /**
         * if map is empty (null or empty), we throw exception
         * @param e map to check
         * @return WorkerExceptionHelper
         */
        public WorkerExceptionHelper notEmpty(Map e) {
            if(e == null || e.isEmpty()) {
                throw exceptionToThrow.apply(message);
            }
            return this;
        }

        /**
         * if any of the object is null, we throw exception
         * @param list list of object to check
         * @return WorkerExceptionHelper
         */
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

        /**
         * if the runnable cannot successfully run, we throw exception
         * @param runnable runnable to check
         * @return WorkerExceptionHelper
         */
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

        /**
         * if the consumer cannot successfully consume the param, we throw exception
         * @param consumer consumer to check
         * @param param param for the consumer
         * @return WorkerExceptionHelper
         */
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

        /**
         * if the consumer(BiConsumer) cannot successfully consume the param1 and param2, we throw exception
         * @param consumer consumer to check
         * @param param1 param1 for the consumer
         * @param param2 param2 for the consumer
         * @return WorkerExceptionHelper
         */
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
