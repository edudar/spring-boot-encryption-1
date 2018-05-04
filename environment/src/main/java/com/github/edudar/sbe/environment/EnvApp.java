package com.github.edudar.sbe.environment;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class EnvApp implements CommandLineRunner {

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

    public static class EncryptionAwareStandardEnvironment extends StandardEnvironment {

        private final BasicTextEncryptor encryptor;

        public EncryptionAwareStandardEnvironment() {
            this.encryptor = new BasicTextEncryptor();
            this.encryptor.setPassword("test");
        }

        @Override
        public String getProperty(String key) {
            return decrypt(super.getProperty(key));
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            return decrypt(super.getProperty(key, defaultValue));
        }

        @Override
        public <T> T getProperty(String key, Class<T> targetType) {
            return super.getProperty(key, targetType);
        }

        @Override
        public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
            return super.getProperty(key, targetType, defaultValue);
        }

        @Override
        public String getRequiredProperty(String key) throws IllegalStateException {
            return decrypt(super.getRequiredProperty(key));
        }

        @Override
        public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
            return super.getRequiredProperty(key, targetType);
        }

        private String decrypt(String value) {
            return value != null && value.startsWith("ENC:") ? this.encryptor.decrypt(value.substring(4)) : value;
        }
    }

    @Autowired
    Service service;

    @Autowired
    Application application;

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(EnvApp.class)
                .environment(new EncryptionAwareStandardEnvironment())
                .web(WebApplicationType.NONE)
                .build()
                .run(args);
    }

    @Override
    public void run(String... args) {
        System.out.println(service.getName());
        System.out.println(application.getName());
    }
}
