/*
 *  The MIT License
 *
 *  Copyright 2010 Sony Ericsson Mobile Communications. All rights reserved.
 *  Copyright 2014 Sony Mobile Communications AB. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonymobile.tools.gerrit.gerritevents.workers;

import com.sonymobile.tools.gerrit.gerritevents.GerritJsonEventFactory;
import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEvent;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Provider;
import com.sonymobile.tools.gerrit.gerritevents.dto.events.GerritTriggeredEvent;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract work that converts a JSON object to a GerritEvent.
 * After the conversion the work is handed over to {@link AbstractGerritEventWork}
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
public abstract class AbstractJsonObjectWork extends AbstractGerritEventWork {

    private static final Logger logger = LoggerFactory.getLogger(AbstractJsonObjectWork.class);
    private final long createdOn;

    /**
     * Standard constructor, initialize the createOn time stamp.
     */
    protected AbstractJsonObjectWork() {
        this.createdOn = System.currentTimeMillis();
    }

    /**
     * Time stamp when the work was created.
     * @return the createdOn time stamp
     */
    public long createdOn() {
        return createdOn;
    }

    /**
     * Parses the JsonObject into a Java bean and sends the parsed {@link GerritEvent} down the inheritance chain.
     * @param json the JsonObject to work on.
     * @param coordinator the coordinator.
     * @param provider the Gerrit server info
     */
    protected void perform(JsonObject json, Coordinator coordinator, Provider provider) {
        logger.trace("Extracting event from JSON.");
        GerritEvent event = GerritJsonEventFactory.getEvent(json);
        if (event != null) {
            if (event instanceof GerritTriggeredEvent) {
                GerritTriggeredEvent gerritTriggeredEvent = (GerritTriggeredEvent)event;
                gerritTriggeredEvent.setProvider(provider);
                gerritTriggeredEvent.setReceivedOn(createdOn);
            }
            logger.debug("Event is: {}", event);
            perform(event, coordinator);
        } else {
            logger.debug("No event extracted!");
        }
    }
}
