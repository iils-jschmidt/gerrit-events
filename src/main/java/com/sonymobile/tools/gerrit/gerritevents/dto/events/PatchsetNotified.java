/*
 *  The MIT License
 *
 *  Copyright 2015 rinrinne All rights reserved.
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
package com.sonymobile.tools.gerrit.gerritevents.dto.events;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventType;
import com.sonymobile.tools.gerrit.gerritevents.dto.attr.Account;
import com.google.gson.JsonObject;

import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.NOTIFIER;

/**
 * A DTO representation of the patchset-notified Gerrit Event.
 *
 * This is extended event defined by 'Gerrit Notify PatchSet Plugin'.
 * https://github.com/rinrinne/gerrit-notify-patchset
 *
 * @author rinrinne (rinrin.ne@gmail.com)
 */
public class PatchsetNotified extends ChangeBasedEvent {

    @Override
    public GerritEventType getEventType() {
        return GerritEventType.PATCHSET_NOTIFIED;
    }

    @Override
    public boolean isScorable() {
        return true;
    }

    @Override
    public void fromJson(JsonObject json) {
        super.fromJson(json);
        if (json.has(NOTIFIER)) {
            this.account = new Account(json.getAsJsonObject(NOTIFIER));
        }
    }

    @Override
    public String toString() {
        return "PatchsetNotified: " + change + " " + patchSet;
    }
}
