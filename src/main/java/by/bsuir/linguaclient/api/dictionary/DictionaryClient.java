package by.bsuir.linguaclient.api.dictionary;

import by.bsuir.linguaclient.dto.dictionary.DicResultDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Component
public class DictionaryClient {

    private final AsyncHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final String apiKey;

    private final String lookupApiUri;

    public DictionaryClient(@Value("${app.key.dictionary.api}") String apiKey,
                            @Value("${app.uri.dictionary.api.lookup}") String lookupApiUri) {
        this.httpClient = Dsl.asyncHttpClient();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.apiKey = apiKey;
        this.lookupApiUri = lookupApiUri;
    }

    @PreDestroy
    public void releaseResources() {
        try {
            httpClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<DicResultDto> lookup(String text, String fromLang, String toLang) {
        return httpClient.prepareGet(lookupApiUri)
                .addQueryParam("key", apiKey)
                .addQueryParam("text", text)
                .addQueryParam("lang", fromLang + "-" + toLang)
                .addQueryParam("ui", "en")
                .execute().toCompletableFuture().handleAsync(this::mapResponseToDicResultDto);
    }

    private DicResultDto mapResponseToDicResultDto(Response response, Throwable throwable) {
        try {
            String json = response.getResponseBody();
            return objectMapper.readValue(json, DicResultDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
