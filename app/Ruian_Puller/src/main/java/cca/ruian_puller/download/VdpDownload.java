package cca.ruian_puller.download;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;


import cca.ruian_puller.utils.LoggerUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Samostatná komponenta pro stahování dat z vpd.cuzk.cz. Pro testy nahrazena mockem.
 */
@Component
public class VdpDownload {

    private CloseableHttpClient client;

    @Value("${http.proxyHost:}")
    private String proxyHost;

    @Value("${http.proxyPort:}")
    private String proxyPort;

    @PostConstruct
    private void init() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        final SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustAllStrategy());
        final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                builder.build());
        final RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(30000)
                .setSocketTimeout(30000)
                .build();
        final HttpClientBuilder clientBuilder = HttpClientBuilder.create()
                .setSSLSocketFactory(sslsf)
                .setDefaultRequestConfig(config);
        if (StringUtils.isNoneBlank(proxyHost, proxyPort)) {
            clientBuilder.setProxy(new HttpHost(proxyHost, Integer.valueOf(proxyPort), "http"));
        }
        client = clientBuilder.build();
    }

    @PreDestroy
    private void close() throws IOException {
        client.close();
    }

    public void tryGet(final String url, final Consumer<InputStream> consumer) throws IOException {
        final HttpGet request = new HttpGet(url);
        try (final CloseableHttpResponse response = client.execute(request)) {
            final HttpEntity entity = response.getEntity();
            consumer.accept(entity.getContent());
        }
    }

    public void trySaveFilter(final String url) throws IOException {
        final CloseableHttpResponse response = client.execute(new HttpGet(url));
        ensureOK(response);
        response.close();
    }

    private void ensureOK(final CloseableHttpResponse response) {
        if (!HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
//            throw new VdpClientException("Chyba pri volani VDP:" + response.getStatusLine());
            LoggerUtil.LOGGER.error("Chyba pri volani VDP:{}", response.getStatusLine());
        }
    }

}
