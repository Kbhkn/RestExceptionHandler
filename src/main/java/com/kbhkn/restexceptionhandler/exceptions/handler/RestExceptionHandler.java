package com.kbhkn.restexceptionhandler.exceptions.handler;

import com.kbhkn.restexceptionhandler.configurations.ExceptionHandlerPropertiesReader;
import com.kbhkn.restexceptionhandler.exceptions.model.ExceptionOutput;
import com.kbhkn.restexceptionhandler.exceptions.types.RemoteRestServiceException;
import com.kbhkn.restexceptionhandler.exceptions.types.RestServiceException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler for any project.
 * When the exception throws, we are sure that it is derived from RestServiceException or RemoteRestServiceException.
 *
 * @author Hakan KABASAKAL, 12-Aug-21
 */
@Lazy
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private final ExceptionHandlerPropertiesReader properties;
    private final ServiceExceptionUtil exceptionUtil;

    @Value("${spring.application.name}")
    private String appName;

    /**
     * Catches RestServiceException(Exp: UserException.class) than gets error description from the database by error code.
     *
     * @param ex      exception
     * @param request app request
     * @return prepared exception response.
     */
    @ExceptionHandler(value = {RestServiceException.class})
    protected ResponseEntity<Object> handleRestServiceException(RestServiceException ex, WebRequest request) {
        String locale = getApplicationLanguage(request);
        var requestId = getRequestId(request);

        ExceptionOutput exceptionOutput = exceptionUtil.prepareExceptionOutput(ex, locale);

        log.error("Exception Handled RequestId: {}\nType: RestServiceException, IP: {}, Api: {}, module: {}, Code: {}, Desc: {}",
            requestId, getRemoteIpAddressFromRequest(request), ex.getApi(), ex.getModule(), ex.getCode(), exceptionOutput.getDesc(), ex);

        return handleExceptionInternal(ex, exceptionOutput, new HttpHeaders(), HttpStatus.EXPECTATION_FAILED, request);
    }

    /**
     * Catches RemoteRestServiceException. RemoteRestServiceException is a Feign-Exceptions.
     * So, before throws from the feign calls, the exception is already prepared. Just passing data and throws again.
     *
     * @param ex      exception
     * @param request a request created by the user.
     * @return prepared exception response.
     */
    @ExceptionHandler(value = {RemoteRestServiceException.class})
    protected ResponseEntity<Object> handleRemoteServiceException(RemoteRestServiceException ex, WebRequest request) {
        var requestId = getRequestId(request);

        log.error("Exception Handled RequestId: {}\nType: RemoteServiceException, IP: {}, Api: {}, module: {}, Code: {}, Desc: {}",
            requestId, getRemoteIpAddressFromRequest(request), ex.getApi(), ex.getModule(), ex.getCode(), ex.getDesc(), ex);

        ExceptionOutput exceptionOutput = exceptionUtil.prepareRemoteExceptionOutput(ex);

        return handleExceptionInternal(ex, exceptionOutput, new HttpHeaders(), HttpStatus.EXPECTATION_FAILED, request);
    }

    /**
     * Generic exception.
     *
     * @param ex      any Exception excepts, RestServiceException and RemoteRestServiceException.
     * @param request a request created by the user.
     * @return prepared exception response.
     */
    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleGeneralExceptions(Exception ex, WebRequest request) {
        var requestId = getRequestId(request);

        log.error("Exception Handled RequestId: {}\nType: Exception.class, IP: {}, Api: {}, Default-Module: {}",
            requestId, getRemoteIpAddressFromRequest(request), appName, properties.getDefaultErrorModule(), ex);

        String locale = getApplicationLanguage(request);

        ExceptionOutput exceptionOutput = exceptionUtil.prepareDefaultExceptionOutput(locale);

        return handleExceptionInternal(ex, exceptionOutput, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR,
            request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {
        log.error("Exception Handled, Type: FieldValidation, IP: {}, Api: {}, Default-Module: {}",
            getRemoteIpAddressFromRequest(request), appName, properties.getDefaultErrorModule(), ex);

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> "'" + error.getField() + "' field is wrong. " + error.getDefaultMessage())
            .collect(Collectors.toList());

        String locale = getApplicationLanguage(request);

        ExceptionOutput exceptionOutput = exceptionUtil.prepareMethodArgumentNotValid(locale);

        exceptionOutput.getErrors().addAll(errors);

        return handleExceptionInternal(ex, exceptionOutput, headers, HttpStatus.BAD_REQUEST, request);
    }

    private String getRemoteIpAddressFromRequest(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRemoteAddr();
    }

    /**
     * Finds the users' application language plus, if it can't find, returns TR by default.
     *
     * @param request users' request.
     * @return lang.
     */
    private String getApplicationLanguage(WebRequest request) {
        String lang = request.getLocale().getLanguage();

        return lang.equals("")
            ? Locale.forLanguageTag("tr-TR").getLanguage().toUpperCase()
            : lang.toUpperCase();

    }

    /**
     * Finds requestId in the header and if there is no requestId, then generate it.
     *
     * @return requestId.
     */
    private String getRequestId(WebRequest request) {
        var orjRequestId = request.getHeader("RequestId");

        return Objects.nonNull(orjRequestId) && orjRequestId.length() > 7
            ? orjRequestId
            : Long.toHexString(Double.doubleToLongBits(Math.random()));
    }
}