/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.uhasoft.guardian.adapter.gateway.zuul.filters;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.uhasoft.guardian.AsyncEntry;
import com.uhasoft.guardian.EntryType;
import com.uhasoft.guardian.ResourceTypeConstants;
import com.uhasoft.guardian.SphU;
import com.uhasoft.guardian.adapter.gateway.common.param.GatewayParamParser;
import com.uhasoft.guardian.adapter.gateway.common.rule.GatewayFlowRule;
import com.uhasoft.guardian.adapter.gateway.zuul.RequestContextItemParser;
import com.uhasoft.guardian.adapter.gateway.zuul.api.ZuulGatewayApiMatcherManager;
import com.uhasoft.guardian.adapter.gateway.zuul.api.matcher.RequestContextApiMatcher;
import com.uhasoft.guardian.adapter.gateway.zuul.callback.ZuulGatewayCallbackManager;
import com.uhasoft.guardian.adapter.gateway.zuul.constants.ZuulConstant;
import com.uhasoft.guardian.adapter.gateway.zuul.fallback.BlockResponse;
import com.uhasoft.guardian.adapter.gateway.zuul.fallback.ZuulBlockFallbackManager;
import com.uhasoft.guardian.adapter.gateway.zuul.fallback.ZuulBlockFallbackProvider;
import com.uhasoft.guardian.context.ContextUtil;
import com.uhasoft.guardian.slots.block.BlockException;
import com.uhasoft.guardian.util.StringUtil;
import com.uhasoft.guardian.util.function.Predicate;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import javax.servlet.http.HttpServletRequest;

import static com.uhasoft.guardian.adapter.gateway.common.SentinelGatewayConstants.*;

/**
 * This pre-filter will regard all {@code proxyId} and all customized API as resources.
 * When a BlockException caught, the filter will try to find a fallback to execute.
 *
 * @author tiger
 * @author Eric Zhao
 */
public class SentinelZuulPreFilter extends ZuulFilter {

    private final int order;

    private final GatewayParamParser<RequestContext> paramParser = new GatewayParamParser<>(
        new RequestContextItemParser());

    public SentinelZuulPreFilter() {
        this(10000);
    }

    public SentinelZuulPreFilter(int order) {
        this.order = order;
    }

    @Override
    public String filterType() {
        return ZuulConstant.PRE_TYPE;
    }

    /**
     * This run before route filter so we can get more accurate RT time.
     */
    @Override
    public int filterOrder() {
        return order;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    private void doSentinelEntry(String resourceName, final int resType, RequestContext requestContext,
                                 Deque<EntryHolder> holders) throws BlockException {
        Object[] params = paramParser.parseParameterFor(resourceName, requestContext,
            new Predicate<GatewayFlowRule>() {
                @Override
                public boolean test(GatewayFlowRule r) {
                    return r.getResourceMode() == resType;
                }
            });
        AsyncEntry entry = SphU.asyncEntry(resourceName, ResourceTypeConstants.COMMON_API_GATEWAY,
                EntryType.IN, params);
        EntryHolder holder = new EntryHolder(entry, params);
        holders.push(holder);
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        String origin = parseOrigin(ctx.getRequest());
        String routeId = (String)ctx.get(ZuulConstant.PROXY_ID_KEY);

        Deque<EntryHolder> holders = new ArrayDeque<>();
        String fallBackRoute = routeId;
        try {
            if (StringUtil.isNotBlank(routeId)) {
                ContextUtil.enter(GATEWAY_CONTEXT_ROUTE_PREFIX + routeId, origin);
                doSentinelEntry(routeId, RESOURCE_MODE_ROUTE_ID, ctx, holders);
            }

            Set<String> matchingApis = pickMatchingApiDefinitions(ctx);
            if (!matchingApis.isEmpty() && ContextUtil.getContext() == null) {
                ContextUtil.enter(ZuulConstant.ZUUL_DEFAULT_CONTEXT, origin);
            }
            for (String apiName : matchingApis) {
                fallBackRoute = apiName;
                doSentinelEntry(apiName, RESOURCE_MODE_CUSTOM_API_NAME, ctx, holders);
            }
        } catch (BlockException ex) {
            ZuulBlockFallbackProvider zuulBlockFallbackProvider = ZuulBlockFallbackManager.getFallbackProvider(
                fallBackRoute);
            BlockResponse blockResponse = zuulBlockFallbackProvider.fallbackResponse(fallBackRoute, ex);
            // Prevent routing from running
            ctx.setRouteHost(null);
            ctx.set(ZuulConstant.SERVICE_ID_KEY, null);

            // Set fallback response.
            ctx.setResponseBody(blockResponse.toString());
            ctx.setResponseStatusCode(blockResponse.getCode());
            // Set Response ContentType
            ctx.getResponse().setContentType("application/json; charset=utf-8");
        } finally {
            // We don't exit the entry here. We need to exit the entries in post filter to record Rt correctly.
            // So here the entries will be carried in the request context.
            if (!holders.isEmpty()) {
                ctx.put(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY, holders);
            }
        }
        return null;
    }

    private String parseOrigin(HttpServletRequest request) {
        return ZuulGatewayCallbackManager.getOriginParser().parseOrigin(request);
    }

    private Set<String> pickMatchingApiDefinitions(RequestContext requestContext) {
        Set<String> apis = new HashSet<>();
        for (RequestContextApiMatcher matcher : ZuulGatewayApiMatcherManager.getApiMatcherMap().values()) {
            if (matcher.test(requestContext)) {
                apis.add(matcher.getApiName());
            }
        }
        return apis;
    }
}
