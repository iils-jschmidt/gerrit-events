/*
 * The MIT License
 *
 * Copyright 2010 Sony Mobile Communications Inc. All rights reserved.
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

package com.sonymobile.tools.gerrit.gerritevents.dto.attr;

import com.sonymobile.tools.gerrit.gerritevents.dto.GerritChangeStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.PROJECT;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.BRANCH;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.ID;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.NUMBER;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.SUBJECT;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.OWNER;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.URL;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.EMAIL;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.NAME;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.COMMENTS;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.MESSAGE;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.REVIEWER;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.CREATED_ON;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.LAST_UPDATED;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.STATUS;
import static com.sonymobile.tools.gerrit.gerritevents.dto.GerritEventKeys.HASHTAGS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for {@link com.sonymobile.tools.gerrit.gerritevents.dto.attr.Change}.
 * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
 */
public class ChangeTest {
    private Account account;
    private JsonObject jsonAccount;

    /**
     * Sets up a dummy Account object and a JSON version before each test.
     */
    @Before
    public void setUp() {
        account = new Account();
        account.setEmail("robert.sandell@sonyericsson.com");
        account.setName("Bobby");
        jsonAccount = new JsonObject();
        jsonAccount.addProperty(EMAIL, account.getEmail());
        jsonAccount.addProperty(NAME, account.getName());
    }

    /**
     * Tests {@link Change#fromJson(net.sf.json.JsonObject)}.
     * @throws Exception if so.
     */
    @Test
    public void testFromJson() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty(PROJECT, "project");
        json.addProperty(BRANCH, "branch");
        json.addProperty(ID, "I2343434344");
        json.addProperty(NUMBER, "100");
        json.addProperty(SUBJECT, "subject");
        json.add(OWNER, jsonAccount);
        json.addProperty(URL, "http://localhost:8080");
        Change change = new Change();
        change.fromJson(json);

        assertEquals(change.getProject(), "project");
        assertEquals(change.getBranch(), "branch");
        assertEquals(change.getId(), "I2343434344");
        assertEquals(change.getNumber(), "100");
        assertEquals(change.getSubject(), "subject");
        assertEquals(change.getOwner(), account);
        assertEquals(change.getUrl(), "http://localhost:8080");
        assertNull(change.getComments());
    }

    /**
     * Tests {@link Change#fromJson(net.sf.json.JsonObject)}.
     * Without any JSON URL data.
     * @throws Exception if so.
     */
    @Test
    public void testFromJsonNoUrl() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty(PROJECT, "project");
        json.addProperty(BRANCH, "branch");
        json.addProperty(ID, "I2343434344");
        json.addProperty(NUMBER, "100");
        json.addProperty(SUBJECT, "subject");
        json.add(OWNER, jsonAccount);
        Change change = new Change();
        change.fromJson(json);

        assertEquals(change.getProject(), "project");
        assertEquals(change.getBranch(), "branch");
        assertEquals(change.getId(), "I2343434344");
        assertEquals(change.getNumber(), "100");
        assertEquals(change.getSubject(), "subject");
        assertTrue(change.getOwner().equals(account));
        assertNull(change.getUrl());
        assertNull(change.getComments());
    }

    /**
     * Tests {@link Change#fromJson(net.sf.json.JsonObject)}.
     * With comments.
     * @throws Exception if so.
     */
    @Test
    public void testFromJsonWithEmptyComments() throws Exception {
        JsonArray jsonComments = new JsonArray();
        JsonObject json = new JsonObject();
        json.add(COMMENTS, jsonComments);
        Change change = new Change();
        change.fromJson(json);

        assertNotNull(change.getComments());
        assertTrue(change.getComments().isEmpty());
    }

    /**
     * Tests {@link Change#fromJson(net.sf.json.JsonObject)}.
     * With comments.
     * @throws Exception if so.
     */
    @Test
    public void testFromJsonWithNonEmptyComments() throws Exception {
        JsonObject jsonComment = new JsonObject();
        jsonComment.addProperty(MESSAGE, "Some review message");
        jsonComment.add(REVIEWER, jsonAccount);
        JsonArray jsonComments = new JsonArray();
        jsonComments.add(jsonComment);
        JsonObject json = new JsonObject();
        json.add(COMMENTS, jsonComments);
        Change change = new Change();
        change.fromJson(json);

        assertNotNull(change.getComments());
        assertEquals(1, change.getComments().size());
    }

    /**
     * Tests {@link Change#fromJson(net.sf.json.JsonObject)}.
     * With date values, createdOn and lastUpdated.
     * @throws Exception if so.
     */
    @Test
    // CS IGNORE MagicNumber FOR NEXT 3 LINES. REASON: TestData
    public void testFromJsonWithDateValues() throws Exception {
        long createdOn = 100000000L;
        long lastUpdated = 110000000L;
        JsonObject json = new JsonObject();
        //In gerrit, time is written in seconds, not milliseconds.
        long createdOnInMilliseconds = TimeUnit.SECONDS.toMillis(createdOn);
        Date createdOnAsDate = new Date(createdOnInMilliseconds);
        //In gerrit, time is written in seconds, not milliseconds.
        long lastUpdatedInMilliseconds = TimeUnit.SECONDS.toMillis(lastUpdated);
        Date lastUpdatedAsDate = new Date(lastUpdatedInMilliseconds);
        json.addProperty(CREATED_ON, createdOn);
        json.addProperty(LAST_UPDATED, lastUpdated);
        Change change = new Change();
        change.fromJson(json);

        assertEquals(createdOnAsDate, change.getCreatedOn());
        assertEquals(lastUpdatedAsDate, change.getLastUpdated());
    }

    /**
     * Tests {@link com.sonymobile.tools.gerrit.gerritevents.dto.attr.Change#Change(net.sf.json.JsonObject)}.
     * @throws Exception if so.
     */
    @Test
    public void testInitJson() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty(PROJECT, "project");
        json.addProperty(BRANCH, "branch");
        json.addProperty(ID, "I2343434344");
        json.addProperty(NUMBER, "100");
        json.addProperty(SUBJECT, "subject");
        json.add(OWNER, jsonAccount);
        json.addProperty(STATUS, "NEW");
        json.addProperty(URL, "http://localhost:8080");
        JsonArray hashtags = new JsonArray();
        hashtags.add("first");
        hashtags.add("second");
        json.add(HASHTAGS, hashtags);
        Change change = new Change(json);

        assertEquals(change.getProject(), "project");
        assertEquals(change.getBranch(), "branch");
        assertEquals(change.getId(), "I2343434344");
        assertEquals(change.getNumber(), "100");
        assertEquals(change.getSubject(), "subject");
        assertTrue(change.getOwner().equals(account));
        assertEquals(change.getUrl(), "http://localhost:8080");
        assertNull(change.getComments());
        assertEquals(change.getStatus(), GerritChangeStatus.NEW);
        assertEquals(change.getHashtags(), Arrays.asList("first", "second"));
    }

    /**
     * Tests {@link com.sonymobile.tools.gerrit.gerritevents.dto.attr.Change#equals(Object)}.
     * @throws Exception if so.
     */
    @Test
    public void testEquals() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty(PROJECT, "project");
        json.addProperty(BRANCH, "branch");
        json.addProperty(ID, "I2343434344");
        json.addProperty(NUMBER, "100");
        json.addProperty(SUBJECT, "subject");
        json.add(OWNER, jsonAccount);
        json.addProperty(URL, "http://localhost:8080");
        Change change = new Change(json);

        Change change2 = new Change();
        change2.setProject("project");
        change2.setBranch("branch");
        change2.setId("I2343434344");
        change2.setNumber("100");
        change2.setSubject("subject");
        change2.setOwner(account);
        change2.setUrl("http://localhost:8080");
        change2.setWip(false);
        change2.setPrivate(false);

        assertTrue(change.equals(change2));
    }
}
