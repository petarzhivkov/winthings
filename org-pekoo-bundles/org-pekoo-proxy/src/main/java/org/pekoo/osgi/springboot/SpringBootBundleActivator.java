package org.pekoo.osgi.springboot;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pekoo.osgi.springboot.service.impl.ProxyServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * Witnigs Proxy Bundle Application spring boot entry point
 * @author Petar Zhivkov
 */
@Import(AppConfig.class)
@SpringBootConfiguration
@EnableAutoConfiguration
public class SpringBootBundleActivator implements BundleActivator {

    ConfigurableApplicationContext appContext;

    @Override
    public void start(BundleContext bundleContext) {
    	/* The class-loader setting is the trick to activate Spring Boot as a OSGI Bunlde */
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        appContext = SpringApplication.run(SpringBootBundleActivator.class);
        ProxyServiceImpl proxyService = (ProxyServiceImpl)appContext.getBean("proxy-service");
        proxyService.setProxyEnvironment(appContext.getEnvironment(), bundleContext);	
    }

    @Override
    public void stop(BundleContext bundleContext) {
        SpringApplication.exit(appContext, () -> 0);
    }
    
}