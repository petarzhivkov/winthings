package org.pekoo.osgi.springboot;


import org.pekoo.osgi.springboot.controller.ControllerConfig;
import org.pekoo.osgi.springboot.service.impl.ProxyServiceImpl;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;


/**
 * Witnigs Proxy Application configuration spring beans
 * @author Petar Zhivkov
 */
@Configuration
@Import(ControllerConfig.class)
public class AppConfig {
    
    @Bean(name="proxy-service")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ProxyServiceImpl proxyService(){
		return new ProxyServiceImpl();
    }
    
}