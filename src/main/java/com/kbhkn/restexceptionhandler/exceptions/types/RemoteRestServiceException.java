package com.kbhkn.restexceptionhandler.exceptions.types;

import java.time.LocalDateTime;
import lombok.Getter;

/**
 * Each service should throw the same responses!
 * Do not do something. Feign-Parser knows what to do!
 *
 * @author Hakan KABASAKAL, 12-Aug-21
 */
@Getter
public class RemoteRestServiceException extends RuntimeException {
    private final String code;
    private final String desc;
    private final String module;
    private final String api;
    private final LocalDateTime timeStamp;

    /**
     * Creates a final exception with given data.
     */
    public RemoteRestServiceException(String code, String desc, String api, String module, LocalDateTime timeStamp) {
        super(desc);
        this.code = code;
        this.desc = desc;
        this.api = api;
        this.module = module;
        this.timeStamp = timeStamp;
    }
}
