package com.github.edudar.sbe.configurer;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class ConfApp implements CommandLineRunner {

    @Component
    public static class Service {

        @Value("${application.name}")
        private String name;

        public String getName() {
            return name;
        }
    }

    @Component
    @ConfigurationProperties(prefix = "application")
    public static class Application {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Component
    public static class EncryptionAwarePropertySourcesPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

        private final BasicTextEncryptor encryptor;

        public EncryptionAwarePropertySourcesPlaceholderConfigurer() {
            this.encryptor = new BasicTextEncryptor();
            this.encryptor.setPassword("test");
        }

        @Override
        protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, ConfigurablePropertyResolver propertyResolver) throws BeansException {
            super.processProperties(beanFactoryToProcess, propertyResolver);
            doProcessProperties(beanFactoryToProcess,
                    value -> value.startsWith("ENC:") ? this.encryptor.decrypt(value.substring(4)) : value);
        }
    }

    @Autowired
    Service service;

    @Autowired
    Application application;

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(ConfApp.class)
                .web(WebApplicationType.NONE)
                .build()
                .run(args);
    }

    @Override
    public void run(String... args) {
        System.out.println("@Value(\"${application.name}\"): " + service.getName());
        System.out.println("@ConfigurationProperties(prefix = \"application\"): " + application.getName());
    }
}
