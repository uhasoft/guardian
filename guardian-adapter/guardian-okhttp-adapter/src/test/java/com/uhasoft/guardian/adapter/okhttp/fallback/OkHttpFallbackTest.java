/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.uhasoft.guardian.adapter.okhttp.fallback;

import com.uhasoft.guardian.slots.block.BlockException;
import com.uhasoft.guardian.slots.block.SentinelRpcException;
import com.uhasoft.guardian.slots.block.flow.FlowException;
import org.junit.Test;

/**
 * @author zhaoyuguang
 */
public class OkHttpFallbackTest {

    @Test(expected = SentinelRpcException.class)
    public void testDefaultOkHttpFallback() {
        BlockException e = new FlowException("xxx");
        OkHttpFallback fallback = new DefaultOkHttpFallback();
        fallback.handle(null, null, e);
    }
}
