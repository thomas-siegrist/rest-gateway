package ch.sbb.api.restgateway.service;

/**
 * Created by thomas on 19.12.17.
 */

import com.netflix.zuul.context.RequestContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HostBasedServiceExtractor {

    private static final String HEADER_HOST = "Host";

    @Value("${routing.locator.default.domain}")
    private String domain = ".api-sbb.ch"; // TODO: hacky - do it in spring style ! :)

    public String extractService(RequestContext ctx) {
        String host = ctx.getRequest().getHeaders(HEADER_HOST).nextElement();
        host = cutOffPort(host);
        host = cutOffDomain(host);
        return host;
    }

    private String cutOffDomain(String host) {
        if (host.contains(domain))
            return host.substring(0, host.length() - domain.length());
        return host;
    }

    private String cutOffPort(String host) {
        if (host.contains(":"))
            return host.substring(0, host.lastIndexOf(":"));
        return host;
    }

}
