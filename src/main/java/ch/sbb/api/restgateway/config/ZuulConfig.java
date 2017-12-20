package ch.sbb.api.restgateway.config;

import ch.sbb.api.restgateway.zuul.HostBasedRouteLocator;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.filters.CompositeRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.post.LocationRewriteFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableZuulProxy
public class ZuulConfig {

    @Bean
    public LocationRewriteFilter locationRewriteFilter() {
        // Rewrite Location Headers for Web Content
        return new LocationRewriteFilter();
    }

    @Bean
    public CompositeRouteLocator compositeRouteLocator(HostBasedRouteLocator hostBasedRouteLocator) {
        return new CompositeRouteLocator(Arrays.asList(hostBasedRouteLocator));
    }

    @Bean
    public HostBasedRouteLocator routeLocator(ServerProperties server, ZuulProperties properties) {
        return new HostBasedRouteLocator(server.getServletPrefix(), properties);
    }

}
