/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysds.test.applications;

import static org.apache.sysds.api.mlcontext.ScriptFactory.dmlFromFile;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.apache.sysds.api.mlcontext.Script;
import org.apache.sysds.test.functions.mlcontext.MLContextTestBase;

/**
 * Test the SystemDS deep learning library, `nn`.
 */
public class NNTest extends MLContextTestBase {

	private static final String TEST_SCRIPT = "src/test/scripts/applications/nn/run_tests.dml";
	private static final String ERROR_STRING = "ERROR:";

	@Test
	public void testNNLibrary() {
		Script script = dmlFromFile(TEST_SCRIPT);
		String stdOut = executeAndCaptureStdOut(ml, script).getRight();
		// This print should stay. Since the structure of these test,
		// is different compared to other tests.
		// The one script tests most of the gradient calculations and NN layers.
		System.out.println(stdOut);
		assertTrue(stdOut, !stdOut.contains(ERROR_STRING));
	}
}
