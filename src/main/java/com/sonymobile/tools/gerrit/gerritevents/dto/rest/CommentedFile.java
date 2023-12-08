/*
 *  The MIT License
 *
 *  Copyright 2013 Jyrki Puttonen. All rights reserved.
 *  Copyright 2013 Sony Mobile Communications AB. All rights reserved.
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
package com.sonymobile.tools.gerrit.gerritevents.dto.rest;

import java.util.Collection;
import java.util.HashSet;

//CS IGNORE LineLength FOR NEXT 6 LINES. REASON: JavaDoc.
/**
 * A file with line comments.
 * Added to {@link ReviewInput#ReviewInput(String, Collection, Collection)}
 *
 * @see <a href="https://gerrit-documentation.storage.googleapis.com/Documentation/2.7/rest-api-changes.html#set-review">Gerrit Documentation</a>
 */
public class CommentedFile {
    private final String fileName;
    private final Collection<LineComment> comments;

    /**
     * Standard Constructor.
     *
     * @param fileName file name
     */
    public CommentedFile(String fileName) {
        this.fileName = fileName;
        this.comments = new HashSet<>();
    }

    /**
     * Standard Constructor.
     *
     * @param fileName file name
     * @param comments comments
     */
    public CommentedFile(String fileName, Collection<LineComment> comments) {
        this(fileName);
        this.comments.addAll(comments);
    }

    /**
     * File name.
     *
     * @return file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Add a LineComment to this file.
     *
     * @param comment the LineComment to add
     * @return true if the comment was added successfully
     */
    public boolean addLineComment(LineComment comment) {
        return comments.add(comment);
    }

    /**
     * all of them.
     *
     * @return line comments
     */
    public Collection<? extends LineComment> getLineComments() {
        return comments;
    }
}
