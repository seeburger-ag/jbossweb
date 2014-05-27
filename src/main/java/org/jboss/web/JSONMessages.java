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

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;

/**
 * Logging IDs 8000-8200
 * @author Remy Maucherat
 */
@MessageBundle(projectCode = "JBWEB")
public interface JSONMessages {

    /**
     * The messages
     */
    JSONMessages MESSAGES = Messages.getBundle(JSONMessages.class);

    @Message(id = 8000, value = "A JSONArray text must start with '[' at ")
    String arrayMustStartWithBracket();

    @Message(id = 8001, value = "Expected a '%s' at ")
    String arrayCharExpected(char ch);

    @Message(id = 8002, value = "Expected a ',' or ']' at ")
    String arrayEndExpected();

    @Message(id = 8003, value = "Array initial value should be a string or collection or array")
    String arrayInvalidValue();

    @Message(id = 8004, value = "Index %s not found in array")
    String arrayInvalidIndex(int index);

    @Message(id = 8005, value = "Array value at %s does not correspond to type %s")
    String arrayInvalidType(int index, String type);

    @Message(id = 8006, value = "A JSONObject text must begin with '{' at ")
    String objectMustStartWithBracket();

    @Message(id = 8007, value = "A JSONObject text must end with '}' at ")
    String objectMustEndWithBracket();

    @Message(id = 8008, value = "Expected a ':' after a key at ")
    String objectExpectedKey();

    @Message(id = 8009, value = "Expected a ',' or '}' at ")
    String objectExpectedEnd();

    @Message(id = 8010, value = "JSONObject[%s] does not correspond to type %s")
    String objectInvalidType(String key, String type);

    @Message(id = 8011, value = "JSONObject[%s] not found")
    String objectNotFound(String key);

    @Message(id = 8012, value = "Duplicate key %s")
    String objectDuplicateKey(String key);

    @Message(id = 8013, value = "JSON does not allow non-finite numbers")
    String objectInfiniteNumber();

    @Message(id = 8014, value = "Bad value from toJSONString: %s")
    String objectBadString(Object o);

    @Message(id = 8015, value = "Stepping back two steps is not supported")
    String tokenerStepBack();

    @Message(id = 8016, value = "Expected '%s' and instead saw '%s' at ")
    String tokenerBadChar(char expected, char found);

    @Message(id = 8017, value = "Substring bounds error at ")
    String tokenerSubstring();

    @Message(id = 8018, value = "Unterminated string at ")
    String tokenerUnterminatedString();

    @Message(id = 8019, value = "Missing value at ")
    String tokenerMissingValue();

    @Message(id = 8020, value = "Null string")
    String writerNull();

    @Message(id = 8021, value = "Value out of sequence")
    String writerValueOutOfSequence();

    @Message(id = 8023, value = "Misplaced array")
    String writerMisplacedArray();

    @Message(id = 8024, value = "Misplaced array end")
    String writerMisplacedArrayEnd();

    @Message(id = 8025, value = "Misplaced object end")
    String writerMisplacedObjectEnd();

    @Message(id = 8026, value = "Null key")
    String writerNullKey();

    @Message(id = 8027, value = "Misplaced key")
    String writerMisplacedKey();

    @Message(id = 8028, value = "Misplaced object")
    String writerMisplacedObject();

    @Message(id = 8029, value = "Nesting error")
    String writerNestingError();

    @Message(id = 8030, value = "Nesting too deep")
    String writerNestingTooDeep();

}
