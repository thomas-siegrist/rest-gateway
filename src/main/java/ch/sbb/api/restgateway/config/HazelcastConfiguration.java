package ch.sbb.api.restgateway.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by thomas on 19.12.17.
 */
@Configuration
public class HazelcastConfiguration {

    public static final String TOKEN_CACHE = "tokens";
    public static final String ROUTES_CACHE = "routes";

    @Bean
    public Config config() {
        Config config = new Config();
        config.getManagementCenterConfig().setEnabled(true);

        config.addMapConfig(
                new MapConfig()
                        .setName(TOKEN_CACHE)
                        .setEvictionPolicy(EvictionPolicy.LRU)
        );

        config.addMapConfig(
                new MapConfig()
                        .setName(ROUTES_CACHE)
                        .setEvictionPolicy(EvictionPolicy.LRU)
        );

        return config;
    }

}
