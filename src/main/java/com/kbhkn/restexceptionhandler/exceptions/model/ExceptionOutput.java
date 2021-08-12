package com.kbhkn.restexceptionhandler.exceptions.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Exception messages' structure.
 *
 * @author Hakan KABASAKAL, 12-Aug-21
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionOutput {
    private final List<String> errors = new ArrayList<>();
    private String code;
    private String desc;
    private String module;
    private String api;
    private LocalDateTime timeStamp;
}
