package zzangmin.db_automation.config;


import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;


@Configuration
public class RestTemplateConfig {

    private final int REST_TEMPLATE_CONNECT_TIMEOUT_MS = 5000;

    @Bean
    public RestTemplate restTemplate() throws Exception {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(getHttpClient());
        requestFactory.setConnectTimeout(REST_TEMPLATE_CONNECT_TIMEOUT_MS);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }

    private CloseableHttpClient getHttpClient()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        return HttpClients.custom()
                .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create()
                                .setSSLSocketFactory(
                                        SSLConnectionSocketFactoryBuilder.create()
                                                .setSslContext(
                                                        SSLContexts.custom()
                                                                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                                                                .build())
                                                .setHostnameVerifier((s, sslSession) -> true)
                                                .build())
                                .build())
                .build();
    }

}