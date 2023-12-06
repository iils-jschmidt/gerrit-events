/*
 * The MIT License
 *
 * Copyright 2010 Sony Ericsson Mobile Communications. All rights reserved.
 * Copyright 2014 Sony Mobile Communications AB. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.sonymobile.tools.gerrit.gerritevents.workers;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEvent;
import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventType;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.PatchsetCreated;
import com.google.gson.JsonObject;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;

import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.BRANCH;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.CHANGE;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.EMAIL;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.ID;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.NAME;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.NUMBER;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.OWNER;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.PATCH_SET;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.PROJECT;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.REF;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.REVISION;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.SUBJECT;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link com.sonymobile.tools.gerrit.gerritevents.workers.StreamEventsStringWork}.
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
public class StreamEventsStringWorkTest {
    /**
     * Tests {@link StreamEventsStringWork#perform(Coordinator)}. with a standard scenario.
     * @throws Exception if so.
     */
    @Test
    public void testPerform() throws Exception {
        JsonObject patch = new JsonObject();
        patch.addProperty(NUMBER, "2");
        patch.addProperty(REVISION, "ad123456789");
        patch.addProperty(REF, "refs/changes/00/100/2");

        JsonObject jsonAccount = new JsonObject();
        jsonAccount.addProperty(EMAIL, "robert.sandell@sonyericsson.com");
        jsonAccount.addProperty(NAME, "Bobby");

        JsonObject change = new JsonObject();
        change.addProperty(PROJECT, "project");
        change.addProperty(BRANCH, "branch");
        change.addProperty(ID, "I2343434344");
        change.addProperty(NUMBER, "100");
        change.addProperty(SUBJECT, "subject");
        change.add(OWNER, jsonAccount);
        change.addProperty(URL, "http://localhost:8080");

        JsonObject jsonEvent = new JsonObject();
        jsonEvent.addProperty("type", GerritEventType.PATCHSET_CREATED.getTypeValue());
        jsonEvent.add(CHANGE, change);
        jsonEvent.add(PATCH_SET, patch);

        StreamEventsStringWork work = new StreamEventsStringWork(jsonEvent.toString());

        final GerritEvent[] notifiedEvent = {null};
        Coordinator coordinator = new Coordinator() {

            @Override
            public BlockingQueue<Work> getWorkQueue() {
                return mock(BlockingQueue.class);
            }
            @Override
            public void notifyListeners(GerritEvent event) {
                notifiedEvent[0] = event;
            }
        };
        work.perform(coordinator);

        assertNotNull(notifiedEvent[0]);
        PatchsetCreated event = (PatchsetCreated)notifiedEvent[0];
        assertEquals("project", event.getChange().getProject());
        assertEquals("100", event.getChange().getNumber());
        assertEquals("2", event.getPatchSet().getNumber());
    }
}
