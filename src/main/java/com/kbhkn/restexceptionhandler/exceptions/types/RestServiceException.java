package com.kbhkn.restexceptionhandler.exceptions.types;

import com.kbhkn.restexceptionhandler.exceptions.base.ServiceException;
import java.util.List;
import lombok.Getter;

/**
 * It is a must that each custom exception derives from this class.
 * For ex: "public enum AuthServiceException implements {@link ServiceException} of {@link RestServiceException}; {}".
 *
 * @author Hakan KABASAKAL, 12-Aug-21
 */
@Getter
public class RestServiceException extends RuntimeException {
    private final String code;
    private final String desc;
    private final String module;
    private final String api;
    private final List<String> parameters;

    /**
     * Creates final exception with given data.
     */
    public RestServiceException(ServiceException<?> serviceEx) {
        super(serviceEx.desc());
        this.code = serviceEx.code();
        this.desc = serviceEx.desc();
        this.module = serviceEx.module();
        this.api = serviceEx.api();
        this.parameters = serviceEx.parameters();
    }
}
