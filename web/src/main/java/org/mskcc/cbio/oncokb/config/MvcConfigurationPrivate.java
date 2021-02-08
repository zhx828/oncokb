package org.mskcc.cbio.oncokb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mskcc.cbio.oncokb.Constants.PRIVATE_API_VERSION;

@Configuration
@ComponentScan(basePackages = "org.mskcc.cbio.oncokb.api.pvt")
@EnableWebMvc
@EnableSwagger2
public class MvcConfigurationPrivate extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/api/private").setViewName("redirect:/api/private/swagger-ui.html");
        registry.addViewController("/api/private/").setViewName("redirect:/api/private/swagger-ui.html");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.regex("/.*")).build();
    }

    private List<SecurityReference> defaultAuth() {
        final AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        final AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{authorizationScope};
        return Collections.singletonList(new SecurityReference("apiKey", authorizationScopes));
    }

    private ApiKey apiKey() {
        return new ApiKey("apiKey", "Authorization", "header");
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("org.mskcc.cbio.oncokb.api.pvt"))
            .build()
            .securitySchemes(Arrays.asList(apiKey()))
            .securityContexts(Collections.singletonList(securityContext()))
            .apiInfo(new ApiInfo(
                "OncoKB APIs",
                "OncoKB, a comprehensive and curated precision oncology knowledge base, offers oncologists detailed, evidence-based information about individual somatic mutations and structural alterations present in patient tumors with the goal of supporting optimal treatment decisions.",
                PRIVATE_API_VERSION,
                "https://www.oncokb.org/terms",
                new Contact("OncoKB", "https://www.oncokb.org", "contact@oncokb.org"),
                "Terms of Use",
                "https://www.oncokb.org/terms"
            ));
    }
}
