package com.kbhkn.restexceptionhandler.exceptions.handler;

import com.kbhkn.restexceptionhandler.configurations.ExceptionHandlerPropertiesReader;
import com.kbhkn.restexceptionhandler.exceptions.model.ExceptionOutput;
import com.kbhkn.restexceptionhandler.exceptions.types.RemoteRestServiceException;
import com.kbhkn.restexceptionhandler.exceptions.types.RestServiceException;
import com.kbhkn.restexceptionhandler.repository.ApiErrorFinderRepositoryService;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Prepares of exception responses!.
 *
 * @author Hakan KABASAKAL, 12-Aug-21
 */
@Lazy
@Slf4j
@Component
@RequiredArgsConstructor
class ServiceExceptionUtil {
    private static final String METHOD_NOT_VALID_CODE = "999";
    private final ApiErrorFinderRepositoryService apiRepository;
    private final ExceptionHandlerPropertiesReader properties;

    @Value("${spring.application.name}")
    private String appName;

    ExceptionOutput prepareDefaultExceptionOutput(String lang) {
        return ExceptionOutput.builder()
            .api(appName)
            .code(properties.getDefaultErrorCode())
            .module(properties.getDefaultErrorModule())
            .desc(useDefaultErrorDefinition(appName, lang))
            .timeStamp(LocalDateTime.now())
            .build();
    }

    ExceptionOutput prepareRemoteExceptionOutput(RemoteRestServiceException ex) {
        return ExceptionOutput.builder()
            .api(ex.getApi())
            .code(ex.getCode())
            .module(ex.getModule())
            .desc(ex.getDesc())
            .timeStamp(Objects.nonNull(ex.getTimeStamp()) ? ex.getTimeStamp() : LocalDateTime.now())
            .build();
    }

    ExceptionOutput prepareMethodArgumentNotValid(String lang) {
        return prepareExceptionOutput(appName, "Self", METHOD_NOT_VALID_CODE, null, lang);
    }

    public ExceptionOutput prepareExceptionOutput(RestServiceException ex, String lang) {
        return prepareExceptionOutput(ex.getApi(), ex.getModule(), ex.getCode(), ex.getParameters(), lang);
    }

    /**
     * Fetches i18n error descriptions by error-code from the database.
     *
     * @param api    which api.
     * @param code   error code
     * @param module which micro-service threw the exception.
     * @param lang   application language.
     * @return prepared exception
     */
    private ExceptionOutput prepareExceptionOutput(String api, String module, String code, List<String> parameters, String lang) {
        String errCode = Optional.ofNullable(code).orElse(properties.getDefaultErrorCode());

        String errDesc;
        String prefix = "";

        Optional<String> optExceptionMessage =
            apiRepository.findErrorDescription(api, module, code, lang);

        if (optExceptionMessage.isPresent()) {
            errDesc = formatExceptionMessageBy(optExceptionMessage.get(), parameters);
        } else {
            log.error("Could not find exception definition for module: {} code: {}", module, errCode);
            if (!properties.getDefaultErrorCode().equalsIgnoreCase(errCode)) {
                //The exception already exists, but it is not defined(ND) in the api_translation_table.
                prefix = "ND_";
            }

            errDesc = useDefaultErrorDefinition(api, lang);
        }

        return ExceptionOutput.builder()
            .api(api)
            .desc(errDesc)
            .module(module)
            .code(prefix + errCode)
            .timeStamp(LocalDateTime.now())
            .build();

    }

    private String formatExceptionMessageBy(String exceptionMessage, List<String> params) {
        if (Objects.isNull(params) || params.isEmpty()) {
            return exceptionMessage;
        }

        String formattedExceptionMessage = exceptionMessage.contains("'")
            ? exceptionMessage.replaceAll("'", "''")
            : exceptionMessage;

        return MessageFormat.format(formattedExceptionMessage, params.toArray());
    }

    /**
     * Default strategy to apply when error code doesn't found in database or DB has a connection problem.
     *
     * @param lang application language.
     * @return default error description to use
     */
    private String useDefaultErrorDefinition(String application, String lang) {
        String defaultErrorModule = properties.getDefaultErrorModule();
        String defaultErrorCode = properties.getDefaultErrorCode();

        Optional<String> optExceptionMessage =
            apiRepository.findErrorDescription(application, defaultErrorModule, defaultErrorCode, lang);

        if (optExceptionMessage.isPresent()) {
            return optExceptionMessage.get();
        } else {
            log.error("Could not find default exception definition for api: {} module: {}, code: {}",
                application, defaultErrorModule, defaultErrorCode);

            return properties.getDefaultErrorMessage();
        }
    }
}
