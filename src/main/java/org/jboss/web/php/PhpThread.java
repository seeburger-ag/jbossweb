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

package org.jboss.web.php;

import java.lang.Thread;
import java.lang.InterruptedException;

/* Thread that handle the php startup and shutdown */
public class PhpThread extends Thread {
    public void run() {
        boolean ok = true;
        Library.StartUp();
        while (ok) {
            try {
                sleep(60000);
            } catch (InterruptedException e) {
                ok = false;
            }
        }
        Library.shutdown();
    }
}
