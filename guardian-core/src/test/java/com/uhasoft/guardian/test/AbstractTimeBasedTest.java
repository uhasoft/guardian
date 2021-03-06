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
package com.uhasoft.guardian.test;

import java.util.concurrent.ThreadLocalRandom;

import com.uhasoft.guardian.Entry;
import com.uhasoft.guardian.SphU;
import com.uhasoft.guardian.slots.block.BlockException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.uhasoft.guardian.Tracer;
import com.uhasoft.guardian.util.TimeUtil;

/**
 * Mock support for {@link TimeUtil}.
 * 
 * @author jason
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ TimeUtil.class })
public abstract class AbstractTimeBasedTest {

    private long currentMillis = 0;

    {
        PowerMockito.mockStatic(TimeUtil.class);
        PowerMockito.when(TimeUtil.currentTimeMillis()).thenReturn(currentMillis);
    }

    protected final void useActualTime() {
        PowerMockito.when(TimeUtil.currentTimeMillis()).thenCallRealMethod();
    }

    protected final void setCurrentMillis(long cur) {
        currentMillis = cur;
        PowerMockito.when(TimeUtil.currentTimeMillis()).thenReturn(currentMillis);
    }

    protected final void sleep(int t) {
        currentMillis += t;
        PowerMockito.when(TimeUtil.currentTimeMillis()).thenReturn(currentMillis);
    }

    protected final void sleepSecond(int timeSec) {
        sleep(timeSec * 1000);
    }
    
    protected final boolean entryAndSleepFor(String res, int sleepMs) {
        Entry entry = null;
        try {
            entry = SphU.entry(res);
            sleep(sleepMs);
        } catch (BlockException ex) {
            return false;
        } catch (Exception ex) {
            Tracer.traceEntry(ex, entry);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
        return true;
    }

    protected final boolean entryWithErrorIfPresent(String res, Exception ex) {
        Entry entry = null;
        try {
            entry = SphU.entry(res);
            if (ex != null) {
                Tracer.traceEntry(ex, entry);
            }
            sleep(ThreadLocalRandom.current().nextInt(5, 10));
        } catch (BlockException b) {
            return false;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
        return true;
    }
}
