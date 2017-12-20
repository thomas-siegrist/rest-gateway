package ch.sbb.api.restgateway.zuul;

import ch.sbb.api.restgateway.service.HostBasedServiceExtractor;
import ch.sbb.api.restgateway.service.TokenIntroscpectionService;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Component
public class OidcZuulFilter extends ZuulFilter {

    private static final String HEADER_AUTH = "Authorization";
    private static final String BEARER = "Bearer";

    @Autowired
    private TokenIntroscpectionService tokenIntroscpectionService;

    @Autowired
    private HostBasedServiceExtractor serviceExtractor;

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String token = readToken(ctx);

        TokenIntroscpectionService.AccessPolicy accessPolicy = tokenIntroscpectionService.introspectToken(token);
        String service = serviceExtractor.extractService(ctx);

        if (!accessPolicy.isValid || !accessPolicy.hasAccessTo(service)) {
            reportAccessDenied();
        }

        ctx.getZuulRequestHeaders().replace(HEADER_AUTH, accessPolicy.jwt); // FIXME: seems not to work properly

        return null;
    }

    private void reportAccessDenied() {
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setResponseStatusCode(403);
        if (ctx.getResponseBody() == null) {
            ctx.setResponseBody("Access denied.");
            ctx.setSendZuulResponse(false);
        }
    }

    private String readToken(RequestContext ctx) {
        String authHeader = ctx.getRequest().getHeader(HEADER_AUTH);
        return cutOffBearer(authHeader);
    }

    private String cutOffBearer(String authHeader) {
        if (authHeader == null)
            return null;
        if (authHeader.startsWith(BEARER))
            return authHeader.replace(BEARER + " ", "");
        return authHeader;
    }

}
