package by.bsuir.linguaclient.api.lingua;

import by.bsuir.linguaclient.dto.lingua.*;
import by.bsuir.linguaclient.exception.UnauthorizedException;
import by.bsuir.linguaclient.subtitle.Subtitle;
import by.bsuir.linguaclient.subtitle.SubtitleParser;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
    private final String usernamePreferencesKey;
    private final String rolesPreferencesKey;
    private final String tokenPreferencesKey;
    private final ReadWriteLock tokenReadWriteLock;
    private String username;
    private List<Role> roles;
    private String token;

    private final String registerApiUri;
    private final String loginApiUri;
    private final String catalogSearchApiUri;
    private final String videoContentApiUri;
    private final String videoContentEditApiUri;
    private final String createDuoWatchRequestApiUri;
    private final String duoWatchRequestCatalogSearchApiUri;
    private final String duoWatchRequestPersonalSearchApiUri;
    private final String acceptDuoWatchRequestApiUri;
    private final String allLanguagesApiUri;
    private final String subtitleApiUri;
    private final String dictionariesApiUri;
    private final String addDictionaryApiUri;
    private final String trainingsApiUri;

    private final String pictureUri;
    private final String videoContentUri;
    private final String subtitleUri;

    public LinguaClient(Preferences preferences,
                        SubtitleParser subtitleParser,
                        @Value("${app.preference.key.username}") String usernamePreferencesKey,
                        @Value("${app.preference.key.roles}") String rolesPreferencesKey,
                        @Value("${app.preference.key.token}") String tokenPreferencesKey,
                        @Value("${app.uri.lingua.api.register}") String registerApiUri,
                        @Value("${app.uri.lingua.api.login}") String loginApiUri,
                        @Value("${app.uri.lingua.api.catalog.search}") String catalogSearchApiUri,
                        @Value("${app.uri.lingua.api.videocontent.details}") String videoContentApiUri,
                        @Value("${app.uri.lingua.api.videocontent.edit}") String videoContentEditApiUri,
                        @Value("${app.uri.lingua.api.duo-watch-request.create}") String createDuoWatchRequestApiUri,
                        @Value("${app.uri.lingua.api.duo-watch-request.catalog.search}") String duoWatchRequestCatalogSearchApiUri,
                        @Value("${app.uri.lingua.api.duo-watch-request.personal.search}") String duoWatchRequestPersonalSearchApiUri,
                        @Value("${app.uri.lingua.api.duo-watch-request.accept}") String acceptDuoWatchRequestApiUri,
                        @Value("${app.uri.lingua.api.language.all}") String allLanguagesApiUri,
                        @Value("${app.uri.lingua.api.subtitle}") String subtitleApiUri,
                        @Value("${app.uri.lingua.api.dictionaries}") String dictionariesApiUri,
                        @Value("${app.uri.lingua.api.dictionaries.add}") String addDictionaryApiUri,
                        @Value("${app.uri.lingua.api.trainings}") String trainingsApiUri,
                        @Value("${app.uri.lingua.picture}") String pictureUri,
                        @Value("${app.uri.lingua.videocontent}") String videoContentUri,
                        @Value("${app.uri.lingua.subtitle}") String subtitleUri) {
        this.preferences = preferences;
        this.httpClient = buildHttpClient();
        this.objectMapper = new ObjectMapper();
        this.subtitleParser = subtitleParser;
        this.usernamePreferencesKey = usernamePreferencesKey;
        this.rolesPreferencesKey = rolesPreferencesKey;
        this.tokenPreferencesKey = tokenPreferencesKey;
        this.tokenReadWriteLock = new ReentrantReadWriteLock(true);
        loadState();
        this.registerApiUri = registerApiUri;
        this.loginApiUri = loginApiUri;
        this.catalogSearchApiUri = catalogSearchApiUri;
        this.videoContentApiUri = videoContentApiUri;
        this.videoContentEditApiUri = videoContentEditApiUri;
        this.createDuoWatchRequestApiUri = createDuoWatchRequestApiUri;
        this.duoWatchRequestCatalogSearchApiUri = duoWatchRequestCatalogSearchApiUri;
        this.duoWatchRequestPersonalSearchApiUri = duoWatchRequestPersonalSearchApiUri;
        this.acceptDuoWatchRequestApiUri = acceptDuoWatchRequestApiUri;
        this.allLanguagesApiUri = allLanguagesApiUri;
        this.subtitleApiUri = subtitleApiUri;
        this.dictionariesApiUri = dictionariesApiUri;
        this.addDictionaryApiUri = addDictionaryApiUri;
        this.trainingsApiUri = trainingsApiUri;
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
            username = null;
            roles = Collections.emptyList();
            token = null;
            persistState();
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

    public String getUsername() {
        return username;
    }

    public List<Role> getRoles() {
        return roles;
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
            String json = response.getResponseBody();
            try {
                var loginResultDto = objectMapper.readValue(json, LoginResultDto.class);
                username = loginResultDto.getUsername();
                roles = loginResultDto.getRoles();
                token = loginResultDto.getToken();
                persistState();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } finally {
                tokenReadWriteLock.writeLock().unlock();
            }
            return true;
        } else {
            return false;
        }
    }

    public void logOut() {
        tokenReadWriteLock.writeLock().lock();
        username = null;
        roles = Collections.emptyList();
        token = null;
        persistState();
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

    public CompletableFuture<VideoContentDetailsDto> getVideoContentDetails(UUID videoContentId) {
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

    public CompletableFuture<PersonalDuoWatchRequestPageDto> duoWatchRequestPersonalSearch(String name,
                                                                                           Boolean owner,
                                                                                           DuoWatchRequestStatus status,
                                                                                           Integer page,
                                                                                           Integer pageSize) {
        BoundRequestBuilder requestBuilder = httpClient.prepareGet(duoWatchRequestPersonalSearchApiUri);
        if (name != null) {
            requestBuilder.addQueryParam("q", name);
        }
        requestBuilder.addQueryParam("owner", owner.toString());
        if (status != null) {
            requestBuilder.addQueryParam("status", status.toString());
        }
        if (page != null) {
            requestBuilder.addQueryParam("p", page.toString());
        }
        if (pageSize != null) {
            requestBuilder.addQueryParam("s", pageSize.toString());
        }
        return requestBuilder.execute().toCompletableFuture().handleAsync(this::mapResponseToDPersonalDuoWatchRequestPageDto);
    }

    private PersonalDuoWatchRequestPageDto mapResponseToDPersonalDuoWatchRequestPageDto(Response response, Throwable throwable) {
        try {
            String json = response.getResponseBody();
            return objectMapper.readValue(json, PersonalDuoWatchRequestPageDto.class);
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

    public CompletableFuture<List<DictionaryDto>> getDictionariesDtos() {
        return httpClient.prepareGet(dictionariesApiUri)
                .execute().toCompletableFuture().handleAsync(this::mapResponseToDictionariesDto);
    }

    private List<DictionaryDto> mapResponseToDictionariesDto(Response response, Throwable throwable) {
        try {
            String json = response.getResponseBody();
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<List<DictionaryWordDto>> getDictionaryWordDtos(Long dictionaryId) {
        return httpClient.prepareGet(dictionariesApiUri + "/" + dictionaryId)
                .execute().toCompletableFuture().handleAsync(this::mapResponseToDictionaryWordDtos);
    }

    private List<DictionaryWordDto> mapResponseToDictionaryWordDtos(Response response, Throwable throwable) {
        try {
            String json = response.getResponseBody();
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<SubtitleDto> getSubtitleDto(UUID videoContentLocId, Long languageId) {
        return httpClient.prepareGet(subtitleApiUri)
                .addQueryParam("videoContentLocId", videoContentLocId.toString())
                .addQueryParam("languageId", languageId.toString())
                .execute().toCompletableFuture().handleAsync(this::mapResponseToSubtitleDto);
    }

    private SubtitleDto mapResponseToSubtitleDto(Response response, Throwable throwable) {
        try {
            String json = response.getResponseBody();
            return objectMapper.readValue(json, SubtitleDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<List<DictionaryWordDto>> getTraining(Long dictionaryId,
                                                                  Integer size) {
        return httpClient.prepareGet(trainingsApiUri + "/" + dictionaryId)
                .addQueryParam("size", size.toString())
                .execute().toCompletableFuture().handleAsync(this::mapResponseToDictionaryWordDtos);
    }

    public CompletableFuture<Boolean> saveTrainingAnswers(Long dictionaryId,
                                                          List<TrainingAnswerDto> trainingAnswerDtos) {
        try {
            String body = objectMapper.writeValueAsString(trainingAnswerDtos);
            return httpClient.preparePost(trainingsApiUri + "/" + dictionaryId + "/answers")
                    .setBody(body)
                    .execute().toCompletableFuture().handleAsync((response, throwable) -> response.getStatusCode() == HttpConstants.ResponseStatusCodes.OK_200);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<VideoContentEditFormDto> getVideoContentEditFormDto(UUID videoContentId) {
        String uri = videoContentEditApiUri.replaceFirst("\\{}", videoContentId.toString());
        return httpClient.prepareGet(uri)
                .execute().toCompletableFuture().handleAsync(this::mapResponseToVideoContentEditFormDto);
    }

    private VideoContentEditFormDto mapResponseToVideoContentEditFormDto(Response response, Throwable throwable) {
        try {
            String json = response.getResponseBody();
            return objectMapper.readValue(json, VideoContentEditFormDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Image getImage(UUID imageId, double width, double height) {
        return new Image(pictureUri + imageId, width, height, true, true, true);
    }

    public void playVideoContent(EmbeddedMediaPlayer embeddedMediaPlayer, UUID videoContentId) {
        tokenReadWriteLock.readLock().lock();
        String uri = videoContentUri + videoContentId + "?token=" + token;
        tokenReadWriteLock.readLock().unlock();
        embeddedMediaPlayer.media().play(uri);
    }

    public StompSession connectWebSocket() {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());
        WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
        webSocketHttpHeaders.add("Authorization", "Bearer " + token);
        try {
            return client.connectAsync("ws://lingua.com:8080/ws", webSocketHttpHeaders, new StompSessionHandlerAdapter() {
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Subtitle> getSubtitle(UUID subtitleId) {
        BoundRequestBuilder requestBuilder = httpClient.prepareGet(subtitleUri + subtitleId);
        tokenReadWriteLock.readLock().lock();
        requestBuilder.addQueryParam("token", token);
        tokenReadWriteLock.readLock().unlock();
        return requestBuilder.execute().toCompletableFuture().handleAsync(this::mapResponseToSubtitle);
    }

    private Subtitle mapResponseToSubtitle(Response response, Throwable throwable) {
        return subtitleParser.parse(response.getResponseBodyAsStream());
    }

    private String rolesToString(List<Role> roles) {
        StringBuilder builder = new StringBuilder();
        for (Role role : roles) {
            builder.append(role.toString()).append(';');
        }
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    private List<Role> stringToRoles(String s) {
        return Arrays.stream(s.split(";"))
                .map(Role::valueOf)
                .toList();
    }

    private void persistState() {
        if (username != null) {
            preferences.put(usernamePreferencesKey, username);
        } else {
            preferences.remove(usernamePreferencesKey);
        }
        if (!roles.isEmpty()) {
            preferences.put(rolesPreferencesKey, rolesToString(roles));
        } else {
            preferences.remove(rolesPreferencesKey);
        }
        if (token != null) {
            preferences.put(tokenPreferencesKey, token);
        } else {
            preferences.remove(tokenPreferencesKey);
        }
    }

    private void loadState() {
        username = preferences.get(usernamePreferencesKey, null);
        roles = Optional.ofNullable(preferences.get(rolesPreferencesKey, null))
                .map(this::stringToRoles)
                .orElse(Collections.emptyList());
        token = preferences.get(tokenPreferencesKey, null);
    }
}
