package com.kbhkn.restexceptionhandler.exceptions.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom exceptions use this interface.
 *
 * @author Hakan KABASAKAL, 12-Aug-21
 */

public interface ServiceException<T extends RuntimeException> {
    List<String> parameters = new ArrayList<>();

    /**
     * error code.
     *
     * @return Specific error code as a string for the exception.
     */
    String code();

    /**
     * error desc.
     *
     * @return description of the exception, human-readable desc for developers.
     */
    String desc();

    /**
     * module name inside the project.
     *
     * @return module name
     */
    String module();

    /**
     * Returns prefix name of the exception.
     *
     * It is intended to give the developer a hint, which service throw the exception.
     *
     * @return micro-service name
     */
    String api();

    /**
     * Needs for formatted exception message.
     *
     * @return message parameters.
     */
    default List<String> parameters() {
        return parameters;
    }

    /**
     * The Helper method throws the exception itself.
     */
    default void raise() {
        throw exception();
    }

    /**
     * The Helper method throws the exception itself with parameters.
     */
    default void raise(String... params) {
        throw exception(params);
    }

    /**
     * Init exception definition by an enum.
     *
     * @return the enum exception as Java exception itself.
     */
    T exception();

    /**
     * Init exception definition by an enum with parameters.
     *
     * @return the enum exception as Java exception itself.
     */
    default T exception(String... params) {
        parameters.clear();
        Collections.addAll(parameters, params);

        return exception();
    }
}

