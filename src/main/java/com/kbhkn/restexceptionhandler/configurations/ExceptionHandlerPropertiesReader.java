package com.kbhkn.restexceptionhandler.configurations;

import javax.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Each micro-service module has different API Name(WhoAmI).
 * Otherwise, default properties are fetched from config-server.
 *
 * @author Hakan KABASAKAL, 12-Aug-21
 */
@Getter
@Setter
@Validated
@Configuration
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ConfigurationProperties(prefix = "common.ex-handler")
public class ExceptionHandlerPropertiesReader {
    @NotEmpty(message = "Default Error Code isn't defined.")
    private String defaultErrorCode;

    @NotEmpty(message = "Default Error Module isn't defined.")
    private String defaultErrorModule;

    @NotEmpty(message = "Default Error Message isn't defined.")
    private String defaultErrorMessage;

    private Datasource datasource;

    @Getter
    @Setter
    public static class Datasource {
        @NotEmpty(message = "Datasource name isn't defined.")
        private String name;

        @NotEmpty(message = "Schema name isn't defined.")
        private String schemaName;

        /* If it is not empty, create a datasource and use it. */
        private String url;
        private String driver;
        private String username;
        private String password;
    }
}

