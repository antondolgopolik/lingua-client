package by.bsuir.linguaclient.api.lingua;

import by.bsuir.linguaclient.dto.lingua.*;
import by.bsuir.linguaclient.exception.UnauthorizedException;
import by.bsuir.linguaclient.subtitle.Subtitle;
import by.bsuir.linguaclient.subtitle.SubtitleParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import jakarta.annotation.PreDestroy;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;
import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.util.HttpConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.prefs.Preferences;

@Component
@Slf4j
public class LinguaClient {

    private final Preferences preferences;
    private final AsyncHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SubtitleParser subtitleParser;
    private final String tokenPreferencesKey;
    private final ReadWriteLock tokenReadWriteLock;
    private String token;

    private final String registerApiUri;
    private final String loginApiUri;
    private final String catalogSearchApiUri;
    private final String videoContentApiUri;
    private final String createDuoWatchRequestApiUri;
    private final String duoWatchRequestCatalogSearchApiUri;
    private final String acceptDuoWatchRequestApiUri;
    private final String allLanguagesApiUri;

    private final String pictureUri;
    private final String videoContentUri;
    private final String subtitleUri;

    public LinguaClient(Preferences preferences,
                        SubtitleParser subtitleParser,
                        @Value("${app.preference.key.token}") String tokenPreferencesKey,
                        @Value("${app.uri.lingua.api.register}") String registerApiUri,
                        @Value("${app.uri.lingua.api.login}") String loginApiUri,
                        @Value("${app.uri.lingua.api.catalog.search}") String catalogSearchApiUri,
                        @Value("${app.uri.lingua.api.videocontent.details}") String videoContentApiUri,
                        @Value("${app.uri.lingua.api.duo-watch-request.create}") String createDuoWatchRequestApiUri,
                        @Value("${app.uri.lingua.api.duo-watch-request.catalog.search}") String duoWatchRequestCatalogSearchApiUri,
                        @Value("${app.uri.lingua.api.duo-watch-request.accept}") String acceptDuoWatchRequestApiUri,
                        @Value("${app.uri.lingua.api.language.all}") String allLanguagesApiUri,
                        @Value("${app.uri.lingua.picture}") String pictureUri,
                        @Value("${app.uri.lingua.videocontent}") String videoContentUri,
                        @Value("${app.uri.lingua.subtitle}") String subtitleUri) {
        this.preferences = preferences;
        this.httpClient = buildHttpClient();
        this.objectMapper = new ObjectMapper();
        this.subtitleParser = subtitleParser;
        this.tokenPreferencesKey = tokenPreferencesKey;
        this.tokenReadWriteLock = new ReentrantReadWriteLock(true);
        this.token = preferences.get(tokenPreferencesKey, null);
        this.registerApiUri = registerApiUri;
        this.loginApiUri = loginApiUri;
        this.catalogSearchApiUri = catalogSearchApiUri;
        this.videoContentApiUri = videoContentApiUri;
        this.createDuoWatchRequestApiUri = createDuoWatchRequestApiUri;
        this.duoWatchRequestCatalogSearchApiUri = duoWatchRequestCatalogSearchApiUri;
        this.acceptDuoWatchRequestApiUri = acceptDuoWatchRequestApiUri;
        this.allLanguagesApiUri = allLanguagesApiUri;
        this.pictureUri = pictureUri;
        this.videoContentUri = videoContentUri;
        this.subtitleUri = subtitleUri;
    }

    private AsyncHttpClient buildHttpClient() {
        DefaultAsyncHttpClientConfig clientConfig = Dsl.config()
                .addRequestFilter(this::addAuthorizationHeader)
                .addRequestFilter(this::addContentTypeHeader)
                .addResponseFilter(this::checkAuth)
                .build();
        return Dsl.asyncHttpClient(clientConfig);
    }

    private <T> FilterContext<T> addAuthorizationHeader(FilterContext<T> filterContext) {
        tokenReadWriteLock.readLock().lock();
        if (token != null) {
            HttpHeaders headers = filterContext.getRequest().getHeaders();
            headers.add(HttpHeaderNames.AUTHORIZATION, "Bearer " + token);
        }
        tokenReadWriteLock.readLock().unlock();
        return filterContext;
    }

    private <T> FilterContext<T> addContentTypeHeader(FilterContext<T> filterContext) {
        HttpHeaders headers = filterContext.getRequest().getHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE, "application/json");
        return filterContext;
    }

    private <T> FilterContext<T> checkAuth(FilterContext<T> filterContext) {
        if (filterContext.getResponseStatus().getStatusCode() == HttpConstants.ResponseStatusCodes.UNAUTHORIZED_401) {
            tokenReadWriteLock.writeLock().lock();
            token = null;
            preferences.remove(tokenPreferencesKey);
            tokenReadWriteLock.writeLock().unlock();
            throw new UnauthorizedException();
        }
        return filterContext;
    }

    @PreDestroy
    public void releaseResources() {
        try {
            httpClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Boolean> register(RegistrationFormDto registrationFormDto) {
        try {
            String body = objectMapper.writeValueAsString(registrationFormDto);
            return httpClient.preparePost(registerApiUri)
                    .setBody(body)
                    .execute().toCompletableFuture().handleAsync(this::saveToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Boolean> logIn(LoginFormDto loginFormDto) {
        try {
            String body = objectMapper.writeValueAsString(loginFormDto);
            return httpClient.preparePost(loginApiUri)
                    .setBody(body)
                    .execute().toCompletableFuture().handleAsync(this::saveToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean saveToken(Response response, Throwable throwable) {
        if (response.getStatusCode() == HttpConstants.ResponseStatusCodes.OK_200) {
            tokenReadWriteLock.writeLock().lock();
            token = response.getResponseBody();
            preferences.put(tokenPreferencesKey, token);
            tokenReadWriteLock.writeLock().unlock();
            return true;
        } else {
            return false;
        }
    }

    public void logOut() {
        tokenReadWriteLock.writeLock().lock();
        token = null;
        preferences.remove(tokenPreferencesKey);
        tokenReadWriteLock.writeLock().unlock();
    }

    public boolean isLoggedIn() {
        tokenReadWriteLock.readLock().lock();
        boolean loggedIn = token != null;
        tokenReadWriteLock.readLock().unlock();
        return loggedIn;
    }

    public CompletableFuture<CatalogItemPageDto> catalogSearch(String name,
                                                               Integer page,
                                                               Integer pageSize) {
        BoundRequestBuilder requestBuilder = httpClient.prepareGet(catalogSearchApiUri);
        if (name != null) {
            requestBuilder.addQueryParam("q", name);
        }
        if (page != null) {
            requestBuilder.addQueryParam("p", page.toString());
        }
        if (pageSize != null) {
            requestBuilder.addQueryParam("s", pageSize.toString());
        }
        return requestBuilder.execute().toCompletableFuture().handleAsync(this::mapResponseToCatalogItemPageDto);
    }

    private CatalogItemPageDto mapResponseToCatalogItemPageDto(Response response, Throwable throwable) {
        try {
            String json = response.getResponseBody();
            return objectMapper.readValue(json, CatalogItemPageDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<VideoContentDetailsDto> getVideoContentDetails(String videoContentId) {
        return httpClient.prepareGet(videoContentApiUri + videoContentId)
                .execute().toCompletableFuture().handleAsync(this::mapResponseToVideoContentDetailsDto);
    }

    private VideoContentDetailsDto mapResponseToVideoContentDetailsDto(Response response, Throwable throwable) {
        try {
            String json = response.getResponseBody();
            return objectMapper.readValue(json, VideoContentDetailsDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Boolean> createDuoWatchRequest(CreateDuoWatchRequestFormDto createDuoWatchRequestFormDto) {
        try {
            String body = objectMapper.writeValueAsString(createDuoWatchRequestFormDto);
            return httpClient.preparePost(createDuoWatchRequestApiUri)
                    .setBody(body)
                    .execute().toCompletableFuture().handleAsync(this::mapResponseToCreateDuoWatchRequestSuccess);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Boolean mapResponseToCreateDuoWatchRequestSuccess(Response response, Throwable throwable) {
        return response.getStatusCode() == HttpConstants.ResponseStatusCodes.OK_200;
    }

    public CompletableFuture<DuoWatchRequestCatalogItemPageDto> duoWatchRequestCatalogSearch(String name,
                                                                                             Long videoContentLangId,
                                                                                             Long secondLangId,
                                                                                             Integer page,
                                                                                             Integer pageSize) {
        BoundRequestBuilder requestBuilder = httpClient.prepareGet(duoWatchRequestCatalogSearchApiUri);
        if (name != null) {
            requestBuilder.addQueryParam("q", name);
        }
        if (videoContentLangId != null) {
            requestBuilder.addQueryParam("videoContentLang", videoContentLangId.toString());
        }
        if (secondLangId != null) {
            requestBuilder.addQueryParam("secondLang", secondLangId.toString());
        }
        if (page != null) {
            requestBuilder.addQueryParam("p", page.toString());
        }
        if (pageSize != null) {
            requestBuilder.addQueryParam("s", pageSize.toString());
        }
        return requestBuilder.execute().toCompletableFuture().handleAsync(this::mapResponseToDuoWatchRequestCatalogItemPageDto);
    }

    private DuoWatchRequestCatalogItemPageDto mapResponseToDuoWatchRequestCatalogItemPageDto(Response response, Throwable throwable) {
        try {
            String json = response.getResponseBody();
            return objectMapper.readValue(json, DuoWatchRequestCatalogItemPageDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<List<LanguageDto>> getAllLanguages() {
        return httpClient.prepareGet(allLanguagesApiUri)
                .execute().toCompletableFuture().handleAsync(this::mapResponseToLanguages);
    }

    private List<LanguageDto> mapResponseToLanguages(Response response, Throwable throwable) {
        try {
            String json = response.getResponseBody();
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Boolean> acceptDuoWatchRequest(Long duoWatchRequestId) {
        String uri = acceptDuoWatchRequestApiUri.replaceFirst("\\{}", duoWatchRequestId.toString());
        return httpClient.preparePost(uri)
                .execute().toCompletableFuture().handleAsync(this::mapResponseToAcceptDuoWatchRequestSuccess);
    }

    private Boolean mapResponseToAcceptDuoWatchRequestSuccess(Response response, Throwable throwable) {
        return response.getStatusCode() == HttpConstants.ResponseStatusCodes.OK_200;
    }

    public CompletableFuture<Image> getImage(String imageId) {
        return httpClient.prepareGet(pictureUri + imageId)
                .execute().toCompletableFuture().handleAsync(this::mapResponseToImage);
    }

    public Image mapResponseToImage(Response response, Throwable throwable) {
        return new Image(response.getResponseBodyAsStream());
    }

    public void playVideoContent(EmbeddedMediaPlayer embeddedMediaPlayer, String videoContentId) {
        tokenReadWriteLock.readLock().lock();
        String uri = videoContentUri + videoContentId + "?token=" + token;
        tokenReadWriteLock.readLock().unlock();
        embeddedMediaPlayer.media().play(uri);
    }

    public CompletableFuture<Subtitle> getSubtitle(String subtitleId) {
        BoundRequestBuilder requestBuilder = httpClient.prepareGet(subtitleUri + subtitleId);
        tokenReadWriteLock.readLock().lock();
        requestBuilder.addQueryParam("token", token);
        tokenReadWriteLock.readLock().unlock();
        return requestBuilder.execute().toCompletableFuture().handleAsync(this::mapResponseToSubtitle);
    }

    private Subtitle mapResponseToSubtitle(Response response, Throwable throwable) {
        return subtitleParser.parse(response.getResponseBodyAsStream());
    }
}