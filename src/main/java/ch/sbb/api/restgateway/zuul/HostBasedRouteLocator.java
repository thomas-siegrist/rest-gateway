package ch.sbb.api.restgateway.zuul;

import ch.sbb.api.restgateway.service.HostBasedServiceExtractor;
import com.netflix.zuul.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;

public class HostBasedRouteLocator extends SimpleRouteLocator {

    private HostBasedServiceExtractor serviceExtractor = new HostBasedServiceExtractor();

    public HostBasedRouteLocator(String servletPath, ZuulProperties properties) {
        super(servletPath, properties);
    }

    @Override
    public Route getMatchingRoute(String path) {
        RequestContext ctx = RequestContext.getCurrentContext();
        String service = serviceExtractor.extractService(ctx);

        String backendPath = "/" + service + path;
        String backendLocation = "https://echo-api.3scale.net";
        String id = backendLocation + backendPath;

        return new Route(
                id,
                backendPath,
                backendLocation,
                null,
                true,
                new HashSet<>()
        );
    }

}
