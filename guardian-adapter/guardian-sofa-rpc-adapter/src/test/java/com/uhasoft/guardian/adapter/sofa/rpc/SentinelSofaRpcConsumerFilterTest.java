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
package com.uhasoft.guardian.adapter.sofa.rpc;

import com.uhasoft.guardian.Constants;
import com.uhasoft.guardian.Entry;
import com.uhasoft.guardian.EntryType;
import com.uhasoft.guardian.context.Context;
import com.uhasoft.guardian.context.ContextUtil;
import com.uhasoft.guardian.node.ClusterNode;
import com.uhasoft.guardian.node.DefaultNode;
import com.uhasoft.guardian.node.Node;
import com.uhasoft.guardian.node.StatisticNode;
import com.uhasoft.guardian.slotchain.ResourceWrapper;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.filter.FilterInvoker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link SentinelSofaRpcConsumerFilter}.
 *
 * @author cdfive
 */
public class SentinelSofaRpcConsumerFilterTest extends BaseTest {

    @Before
    public void setUp() {
        cleanUpAll();
    }

    @After
    public void cleanUp() {
        cleanUpAll();
    }

    @Test
    public void testInvokeSentinelWorks() {
        SentinelSofaRpcConsumerFilter filter = new SentinelSofaRpcConsumerFilter();

        final String interfaceResourceName = "com.uhasoft.guardian.adapter.sofa.rpc.service.DemoService";
        final String methodResourceName = "com.uhasoft.guardian.adapter.sofa.rpc.service.DemoService#sayHello(java.lang.String,int)";

        SofaRequest request = mock(SofaRequest.class);
        when(request.getInvokeType()).thenReturn(RpcConstants.INVOKER_TYPE_SYNC);
        when(request.getInterfaceName()).thenReturn(interfaceResourceName);
        when(request.getMethodName()).thenReturn("sayHello");
        when(request.getMethodArgSigs()).thenReturn(new String[]{"java.lang.String", "int"});
        when(request.getMethodArgs()).thenReturn(new Object[]{"Sentinel", 2020});

        FilterInvoker filterInvoker = mock(FilterInvoker.class);
        when(filterInvoker.invoke(request)).thenAnswer(new Answer<SofaResponse>() {
            @Override
            public SofaResponse answer(InvocationOnMock invocationOnMock) throws Throwable {
                verifyInvocationStructure(interfaceResourceName, methodResourceName);
                SofaResponse response = new SofaResponse();
                response.setAppResponse("Hello Sentinel 2020");
                return response;
            }
        });

        // Before invoke
        assertNull(ContextUtil.getContext());

        // Do invoke
        SofaResponse response = filter.invoke(filterInvoker, request);
        assertEquals("Hello Sentinel 2020", response.getAppResponse());
        verify(filterInvoker).invoke(request);

        // After invoke, make sure exit context
        assertNull(ContextUtil.getContext());
    }

    /**
     * Verify Sentinel invocation structure in memory:
     * EntranceNode(defaultContextName)
     * --InterfaceNode(interfaceName)
     * ----MethodNode(resourceName)
     */
    private void verifyInvocationStructure(String interfaceResourceName, String methodResourceName) {
        Context context = ContextUtil.getContext();
        assertNotNull(context);

        // As not call ContextUtil.enter(methodResourceName, applicationName) in SentinelSofaRpcConsumerFilter, use default context
        // In actual project, a consumer is usually also a provider, the context will be created by SentinelSofaRpcProviderFilter
        // If consumer is on the top of SOFARPC invocation chain, use default context
        assertEquals(Constants.CONTEXT_DEFAULT_NAME, context.getName());
        assertEquals("", context.getOrigin());

        DefaultNode entranceNode = context.getEntranceNode();
        ResourceWrapper entranceResource = entranceNode.getId();
        assertEquals(Constants.CONTEXT_DEFAULT_NAME, entranceResource.getName());
        assertSame(EntryType.IN, entranceResource.getEntryType());

        // As SphU.entry(interfaceResourceName, EntryType.OUT);
        Set<Node> childList = entranceNode.getChildList();
        assertEquals(1, childList.size());
        DefaultNode interfaceNode = (DefaultNode) childList.iterator().next();
        ResourceWrapper interfaceResource = interfaceNode.getId();
        assertEquals(interfaceResourceName, interfaceResource.getName());
        assertSame(EntryType.OUT, interfaceResource.getEntryType());

        // As SphU.entry(methodResourceName, EntryType.OUT);
        childList = interfaceNode.getChildList();
        assertEquals(1, childList.size());
        DefaultNode methodNode = (DefaultNode) childList.iterator().next();
        ResourceWrapper methodResource = methodNode.getId();
        assertEquals(methodResourceName, methodResource.getName());
        assertSame(EntryType.OUT, methodResource.getEntryType());

        // Verify curEntry
        Entry curEntry = context.getCurEntry();
        assertSame(methodNode, curEntry.getCurNode());
        assertSame(interfaceNode, curEntry.getLastNode());
        // As context origin is not "", no originNode should be created in curEntry
        assertNull(curEntry.getOriginNode());

        // Verify clusterNode
        ClusterNode methodClusterNode = methodNode.getClusterNode();
        ClusterNode interfaceClusterNode = interfaceNode.getClusterNode();
        // Different resource->Different ProcessorSlot->Different ClusterNode
        assertNotSame(methodClusterNode, interfaceClusterNode);

        // As context origin is "", the StatisticNode should not be created in originCountMap of ClusterNode
        Map<String, StatisticNode> methodOriginCountMap = methodClusterNode.getOriginCountMap();
        assertEquals(0, methodOriginCountMap.size());

        Map<String, StatisticNode> interfaceOriginCountMap = interfaceClusterNode.getOriginCountMap();
        assertEquals(0, interfaceOriginCountMap.size());
    }
}
