package de.tum.in.www1.artemis.config;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import de.tum.in.www1.artemis.config.auth.JiraAuthorizationInterceptor;
import de.tum.in.www1.artemis.service.connectors.gitlab.GitLabHeaderAuthorizationInterceptor;
import de.tum.in.www1.artemis.service.connectors.jenkins.JenkinsAuthorizationInterceptor;

/**
 * For now only provides a basic {@link org.springframework.web.client.RestTemplate RestTemplate} bean. Can be extended
 * to further customize how requests to other REST APIs are handled
 */
@Configuration
public class RestTemplateConfiguration {

    @Bean
    @Profile("gitlab")
    @Autowired
    public RestTemplate gitlabRestTemplate(GitLabHeaderAuthorizationInterceptor gitlabInterceptor) {
        return initializeRestTemplateWithInterceptors(gitlabInterceptor);
    }

    @Bean
    @Profile("jenkins")
    @Autowired
    public RestTemplate jenkinsRestTemplate(JenkinsAuthorizationInterceptor jenkinsInterceptor) {
        return initializeRestTemplateWithInterceptors(jenkinsInterceptor);
    }

    @Bean
    @Profile("jira")
    @Autowired
    public RestTemplate jiraRestTemplate(JiraAuthorizationInterceptor jiraAuthorizationInterceptor) {
        return initializeRestTemplateWithInterceptors(jiraAuthorizationInterceptor);
    }

    @Bean
    @Profile("bitbucket")
    public RestTemplate bitbucketRestTemplate() {
        // TODO: authenticate here
        return new RestTemplate();
    }

    @Bean
    @Profile("bamboo")
    public RestTemplate bambooRestTemplate() {
        // TODO: authenticate here
        return new RestTemplate();
    }

    @NotNull
    private RestTemplate initializeRestTemplateWithInterceptors(ClientHttpRequestInterceptor interceptor) {
        final var restTemplate = new RestTemplate();
        var interceptors = restTemplate.getInterceptors();
        if (interceptors.isEmpty()) {
            interceptors = new ArrayList<>();
        }
        interceptors.add(interceptor);
        restTemplate.setInterceptors(interceptors);

        // we do not want to use MappingJackson2XmlHttpMessageConverter here because it would lead to problems with the tests
        HttpMessageConverter<?> messageConverterToRemove = null;
        for (HttpMessageConverter<?> messageConverter : restTemplate.getMessageConverters()) {
            if (messageConverter instanceof MappingJackson2XmlHttpMessageConverter) {
                messageConverterToRemove = messageConverter;
            }
        }
        if (messageConverterToRemove != null) {
            restTemplate.getMessageConverters().remove(messageConverterToRemove);
        }
        return restTemplate;
    }

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
