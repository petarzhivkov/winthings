package org.pekoo.osgi.springboot.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Witnigs Proxy Controller configuration spring bean
 * @author Petar Zhivkov
 */
@Configuration
public class ControllerConfig {

    @Bean
    WithingsProxyController withingsProxyController() {
        return new WithingsProxyController();
    }
}