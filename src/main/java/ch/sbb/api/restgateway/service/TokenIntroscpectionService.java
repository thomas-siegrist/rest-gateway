package ch.sbb.api.restgateway.service;

import ch.sbb.api.restgateway.config.HazelcastConfiguration;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by thomas on 19.12.17.
 */
@Component
public class TokenIntroscpectionService {

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${cache.token.eviction.ttl}")
    private Integer tokenEvictionTtl;

    @Autowired
    private HazelcastInstance hz;

    @Autowired
    private RestTemplate rest;

    private static IMap<String, AccessPolicy> tokenCache;

    private IMap<String, AccessPolicy> tokenCache() {
        if (tokenCache == null)
            tokenCache = hz.getMap(HazelcastConfiguration.TOKEN_CACHE);
        return tokenCache;
    }

    /**
     * @param token
     * @return the introspected jwt representation to the given token.
     */
    public AccessPolicy introspectToken(String token) {
        AccessPolicy accessPolicy = tokenCache().get(token);
        if (accessPolicy != null) {
            return accessPolicy;
        }

        String jwt = rest
                .postForEntity(keycloakUrl, createIntrospectionRequest(token), String.class)
                .getBody();

        if (jwt.contains("\"active\":false")) {
            accessPolicy = new AccessPolicy(jwt, Arrays.asList(), false);
        } else {
            accessPolicy = new AccessPolicy(jwt, readServicePermissions(jwt), true);
        }

        tokenCache().put(token, accessPolicy, tokenEvictionTtl, TimeUnit.MINUTES);

        return accessPolicy;
    }

    private static Set<String> readServicePermissions(String jwt) {
        Map<String, Object> servicePermissions = JsonPath.read(jwt, "$.resource_access");
        return servicePermissions
                .keySet()
                .stream()
                .filter(key -> key != null)
                .map(key -> key.replace("-service", ""))
                .collect(Collectors.toSet());
    }

    private HttpEntity<MultiValueMap<String, String>> createIntrospectionRequest(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic cmVzdGdhdGV3YXk6MDliZGZhM2ItYjc5Mi00YzNhLWIzZWItZTkxM2IyYjdhYWFk"); // TODO: config

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("token_type_hint", "access_token");
        map.add("token", token);

        return new HttpEntity<>(map, headers);
    }

    public static class AccessPolicy implements Serializable {

        public final String jwt;
        public final Collection<String> servicePermissions;
        public final boolean isValid;

        public AccessPolicy(String jwt, Collection<String> servicePermissions, boolean isValid) {
            this.jwt = jwt;
            this.servicePermissions = servicePermissions;
            this.isValid = isValid;
        }

        public boolean hasAccessTo(String service) {
            return servicePermissions.contains(service);
        }

    }

}
