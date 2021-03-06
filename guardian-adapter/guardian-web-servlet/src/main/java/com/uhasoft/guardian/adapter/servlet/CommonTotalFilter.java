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
package com.uhasoft.guardian.adapter.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uhasoft.guardian.Entry;
import com.uhasoft.guardian.ResourceTypeConstants;
import com.uhasoft.guardian.SphU;
import com.uhasoft.guardian.Tracer;
import com.uhasoft.guardian.adapter.servlet.callback.WebCallbackManager;
import com.uhasoft.guardian.adapter.servlet.config.WebServletConfig;
import com.uhasoft.guardian.context.ContextUtil;
import com.uhasoft.guardian.slots.block.BlockException;

/***
 * Servlet filter for all requests.
 *
 * @author youji.zj
 */
public class CommonTotalFilter implements Filter {

    public static final String TOTAL_URL_REQUEST = "total-url-request";

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest sRequest = (HttpServletRequest)request;

        Entry entry = null;
        try {
            ContextUtil.enter(WebServletConfig.WEB_SERVLET_CONTEXT_NAME);
            entry = SphU.entry(TOTAL_URL_REQUEST, ResourceTypeConstants.COMMON_WEB);
            chain.doFilter(request, response);
        } catch (BlockException e) {
            HttpServletResponse sResponse = (HttpServletResponse)response;
            WebCallbackManager.getUrlBlockHandler().blocked(sRequest, sResponse, e);
        } catch (IOException | ServletException | RuntimeException e2) {
            Tracer.trace(e2);
            throw e2;
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
    }

    @Override
    public void destroy() {

    }

}
