/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.web;

import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;

/**
 * Logging IDs 8200-8500
 * @author Remy Maucherat
 */
@MessageBundle(projectCode = "JBWEB")
public interface FileUploadMessages {

    /**
     * The messages
     */
    FileUploadMessages MESSAGES = Messages.getBundle(FileUploadMessages.class);

    @Message(id = 8200, value = "Negative initial size: %s")
    IllegalArgumentException negativeBufferSize(int size);

    @Message(id = 8201, value = "Temporary file prefix is missing")
    IllegalArgumentException missingTemporaryFilePrefix();

    @Message(id = 8202, value = "Stream not closed")
    String streamNotClosed();

    @Message(id = 8203, value = "Null file")
    NullPointerException nullFile();

    @Message(id = 8204, value = "Null path")
    NullPointerException nullPath();

    @Message(id = 8205, value = "No new trackers can be added once finished")
    IllegalStateException cannotAddTrackersOnceFinished();

    @Message(id = 8206, value = "Deletion failed: %s")
    String failedToDelete(String file);

    @Message(id = 8207, value = "No FileItemFactory has been set")
    NullPointerException nullFactory();

    @Message(id = 8208, value = "Processing of %s request failed: %s")
    String multipartProcessingFailed(String multipart, String message);

    @Message(id = 8209, value = "Expected headers to be terminated by an empty line")
    IllegalStateException emptyLineAfterHeaders();

    @Message(id = 8210, value = "The field %s exceeds its maximum permitted size of %s bytes")
    String maxFieldSizeExceeded(String field, long maxFileSize);

    @Message(id = 8211, value = "The stream was already opened")
    IllegalStateException streamAlreadyOpened();

    @Message(id = 8212, value = "Null context")
    NullPointerException nullContext();

    @Message(id = 8213, value = "The request doesn't contain a %s or %s stream, content type header is %s")
    String invalidContentType(String multipartForm, String multipartMixed, String contentType);

    @Message(id = 8214, value = "The request was rejected because its size (%s) exceeds the configured maximum (%s)")
    String maxRequestSizeExceeded(long requestSize, long maxRequestSize);

    @Message(id = 8215, value = "The request was rejected because no multipart boundary was found")
    String missingMultipartBoundary();

    @Message(id = 8216, value = "Unable to delete directory %s")
    String failedToDeleteDirectory(String directory);

    @Message(id = 8217, value = "Directory %s does not exist")
    IllegalArgumentException missingDirectory(String directory);

    @Message(id = 8218, value = "Path %s is not a directory")
    IllegalArgumentException notDirectory(String directory);

    @Message(id = 8219, value = "Failed listing contents of directory %s")
    String failedListingDirectory(String directory);

    @Message(id = 8220, value = "File %s does not exist")
    String missingFile(String file);

    @Message(id = 8221, value = "Unable to delete file %s")
    String failedToDeleteFile(String file);

    @Message(id = 8222, value = "No more data is available")
    String noDataAvailable();

    @Message(id = 8223, value = "Unexpected characters follow a boundary")
    String unexpectedCharactersAfterBoundary();

    @Message(id = 8224, value = "Stream ended unexpectedly")
    String unexpectedEndOfStream();

    @Message(id = 8225, value = "The length of a boundary token can not be changed")
    String invalidBoundaryToken();

    @Message(id = 8226, value = "Header section has more than %s bytes (maybe it is not properly terminated)")
    String invalidHeader(int maxHeaderSize);

    @Message(id = 8227, value = "Cannot write uploaded file to disk")
    String errorWritingUpload();

    @Message(id = 8228, value = "Invalid file name: %s")
    String invalidFileName(String fileName);

    @Message(id = 8229, value = "The boundary specified in the %s header is too long")
    String invalidBoundary(String header);

    @Message(id = 8230, value = "The buffer size specified for the MultipartStream is too small")
    IllegalArgumentException multipartStreamBufferSizeTooSmall();

}
