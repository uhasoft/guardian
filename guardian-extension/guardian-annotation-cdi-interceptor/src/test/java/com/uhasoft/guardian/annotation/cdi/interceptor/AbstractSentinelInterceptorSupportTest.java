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
package com.uhasoft.guardian.annotation.cdi.interceptor;

import com.uhasoft.guardian.annotation.cdi.interceptor.integration.service.FooService;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author sea
 */
public class AbstractSentinelInterceptorSupportTest extends AbstractSentinelInterceptorSupport {

    @Test
    public void testGetResourceName() throws Exception {
        Method method = FooService.class.getMethod("random");
        String resourceName = "someRandom";
        String expectedResolvedName = FooService.class.getName() + ":random()";
        assertThat(getResourceName(resourceName, method)).isEqualTo(resourceName);
        assertThat(getResourceName(null, method)).isEqualTo(expectedResolvedName);
        assertThat(getResourceName("", method)).isEqualTo(expectedResolvedName);
    }
}
