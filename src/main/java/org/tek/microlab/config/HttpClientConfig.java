package org.tek.microlab.config;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.NTLMSchemeFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class HttpClientConfig {

    @Value("${dms.userid}")
    private String dmsUserId;

    @Value("${dms.password}")
    private String dmsPassword;

    @Bean
    public CloseableHttpClient ntlmHttpClient() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials credentials = new NTCredentials(dmsUserId, dmsPassword.toCharArray(), null, null);
        ((BasicCredentialsProvider) credsProvider).setCredentials(new AuthScope(null, -1), credentials);

        Lookup<AuthSchemeFactory> authSchemeRegistry = RegistryBuilder.<AuthSchemeFactory>create()
                .register("NTLM", new NTLMSchemeFactory())
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setTargetPreferredAuthSchemes(List.of("NTLM"))
                .build();

        return HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultAuthSchemeRegistry(authSchemeRegistry)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
}