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
package com.uhasoft.guardian.adapter.sofa.rpc.fallback;

import com.uhasoft.guardian.slots.block.BlockException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.filter.FilterInvoker;

/**
 * Sentinel fallback handler for SOFARPC services.
 *
 * @author cdfive
 */
public interface SofaRpcFallback {

    /**
     * Handle the block exception and provide fallback result.
     *
     * @param invoker FilterInvoker
     * @param request SofaRequest
     * @param ex block exception
     * @return fallback result
     */
    SofaResponse handle(FilterInvoker invoker, SofaRequest request, BlockException ex);
}
