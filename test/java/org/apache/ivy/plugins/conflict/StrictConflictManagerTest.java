/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ivy.plugins.conflict;

import java.util.Date;

import junit.framework.TestCase;

import org.apache.ivy.Ivy;

public class StrictConflictManagerTest extends TestCase {

    public void testInitFromConf() throws Exception {
        Ivy ivy = new Ivy();
        ivy.configure(StrictConflictManagerTest.class.getResource("ivyconf-strict-test.xml"));
        ConflictManager cm = ivy.getSettings().getDefaultConflictManager();
        assertTrue(cm instanceof StrictConflictManager);
    }

    public void testNoConflictResolve() throws Exception {
        Ivy ivy = new Ivy();
        ivy.configure(StrictConflictManagerTest.class.getResource("ivyconf-strict-test.xml"));

        ivy.resolve(StrictConflictManagerTest.class.getResource("ivy-noconflict.xml"), null, new String[] { "*" }, null, new Date(), false);
    }

    public void testConflictResolve() throws Exception {
        Ivy ivy = new Ivy();
        ivy.configure(StrictConflictManagerTest.class.getResource("ivyconf-strict-test.xml"));

        try {
            ivy.resolve(StrictConflictManagerTest.class.getResource("ivy-conflict.xml"), null, new String[] { "*" }, null, new Date(), false);

            fail("Resolve should have failed with a conflict");
        } catch (StrictConflictException e) {
            // this is expected
        }
    }

}
