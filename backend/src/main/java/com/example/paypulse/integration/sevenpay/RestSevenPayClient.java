package com.example.paypulse.integration.sevenpay;

import com.example.paypulse.config.SevenPayProperties;
import com.example.paypulse.integration.sevenpay.dto.SevenPayMerchant;
import com.example.paypulse.integration.sevenpay.dto.SevenPayPage;
import com.example.paypulse.integration.sevenpay.dto.SevenPaySalepoint;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTerminal;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTransaction;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTransactionDetails;
import com.example.paypulse.integration.sevenpay.dto.SevenPayTransactionQuery;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class RestSevenPayClient implements SevenPayClient {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final int MAX_ERROR_BODY = 500;
    private static final DateTimeFormatter REQUEST_TIME =
            DateTimeFormatter.ofPattern(SevenPayTransactionQuery.REQUEST_TIME_PATTERN);

    private final RestClient restClient;
    private final SevenPayProperties properties;
    private final SevenPayRetry retry;

    public RestSevenPayClient(SevenPayProperties properties) {
        this.properties = properties;
        this.retry = new SevenPayRetry(properties.maxRetries(), properties.retryDelay());
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory(properties))
                .defaultHeader(API_KEY_HEADER, properties.hasApiKey() ? properties.apiKey() : "")
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw new SevenPayException(response.getStatusCode().value(), "7Pay " + request.getURI().getPath()
                            + " returned " + response.getStatusCode().value() + ": " + errorBody(response));
                })
                .build();
    }

    @Override
    public SevenPayPage<SevenPayMerchant> getMerchants(int page) {
        return getPage(uri -> pageUri(uri.path("/merchants"), page, properties.pageSize()),
                new ParameterizedTypeReference<SevenPayPage<SevenPayMerchant>>() { });
    }

    @Override
    public SevenPayPage<SevenPaySalepoint> getSalepoints(int page) {
        return getPage(uri -> pageUri(uri.path("/salepoints"), page, properties.pageSize()),
                new ParameterizedTypeReference<SevenPayPage<SevenPaySalepoint>>() { });
    }

    @Override
    public SevenPayPage<SevenPayTerminal> getTerminals(int page) {
        return getPage(uri -> pageUri(uri.path("/terminals"), page, properties.pageSize()),
                new ParameterizedTypeReference<SevenPayPage<SevenPayTerminal>>() { });
    }

    @Override
    public SevenPayPage<SevenPayTransaction> searchTransactions(SevenPayTransactionQuery query, int page) {
        return getPage(uri -> {
            uri.path("/transactions/search").queryParam("merchantOib", query.merchantOib());
            addTime(uri, "timeFrom", query.timeFrom());
            addTime(uri, "timeUntil", query.timeUntil());
            return pageUri(uri, page, properties.transactionPageSize());
        }, new ParameterizedTypeReference<SevenPayPage<SevenPayTransaction>>() { });
    }

    @Override
    public SevenPayTransactionDetails getTransactionDetails(String transactionId) {
        return call(() -> restClient.get()
                .uri("/transactions/{id}/details", transactionId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(SevenPayTransactionDetails.class));
    }

    private <T> SevenPayPage<T> getPage(Function<UriBuilder, URI> uri,
                                        ParameterizedTypeReference<SevenPayPage<T>> type) {
        return call(() -> restClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(type));
    }

    private <T> T call(Supplier<T> request) {
        if (!properties.hasApiKey()) {
            throw new SevenPayException(401, "SEVENPAY_KEY is not set");
        }
        return retry.run(() -> {
            try {
                return request.get();
            } catch (ResourceAccessException exception) {
                throw new SevenPayException("7Pay is not reachable", exception);
            }
        });
    }

    private static URI pageUri(UriBuilder uri, int page, int size) {
        return uri.queryParam("page", page).queryParam("size", size).build();
    }

    private static void addTime(UriBuilder uri, String name, LocalDateTime time) {
        if (time != null) {
            uri.queryParam(name, REQUEST_TIME.format(time));
        }
    }

    private static String errorBody(ClientHttpResponse response) throws IOException {
        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        return body.length() > MAX_ERROR_BODY ? body.substring(0, MAX_ERROR_BODY) + "..." : body;
    }

    private static JdkClientHttpRequestFactory requestFactory(SevenPayProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.readTimeout());
        return factory;
    }
}
