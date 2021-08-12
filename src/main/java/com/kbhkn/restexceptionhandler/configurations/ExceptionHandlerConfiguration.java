package com.kbhkn.restexceptionhandler.configurations;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Activated Custom Auto Conf. --> META-INF/spring.factories
 *
 * @author Hakan KABASAKAL, 12-Aug-21
 */
@Lazy
@Configuration
@ComponentScan(basePackages = "com.kbhkn.restexceptionhandler", lazyInit = true)
public class ExceptionHandlerConfiguration {
}
