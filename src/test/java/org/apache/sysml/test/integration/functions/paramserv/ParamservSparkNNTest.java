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

package org.apache.sysml.test.integration.functions.paramserv;

import org.apache.sysml.api.DMLException;
import org.apache.sysml.api.DMLScript;
import org.apache.sysml.parser.Statement;
import org.apache.sysml.test.integration.AutomatedTestBase;
import org.apache.sysml.test.integration.TestConfiguration;
import org.junit.Test;

public class ParamservSparkNNTest extends AutomatedTestBase {

	private static final String TEST_NAME1 = "paramserv-test";
	private static final String TEST_NAME2 = "paramserv-spark-worker-failed";
	private static final String TEST_NAME3 = "paramserv-spark-agg-service-failed";

	private static final String TEST_DIR = "functions/paramserv/";
	private static final String TEST_CLASS_DIR = TEST_DIR + ParamservSparkNNTest.class.getSimpleName() + "/";

	@Override
	public void setUp() {
		addTestConfiguration(TEST_NAME1, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME1, new String[] {}));
		addTestConfiguration(TEST_NAME2, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME2, new String[] {}));
		addTestConfiguration(TEST_NAME3, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME3, new String[] {}));
	}

	@Test
	public void testParamservBSPBatchDisjointContiguous() {
		runDMLTest(2, 3, Statement.PSUpdateType.BSP, Statement.PSFrequency.BATCH, 16, Statement.PSScheme.DISJOINT_CONTIGUOUS);
	}

	@Test
	public void testParamservASPBatchDisjointContiguous() {
		runDMLTest(2, 3, Statement.PSUpdateType.ASP, Statement.PSFrequency.BATCH, 16, Statement.PSScheme.DISJOINT_CONTIGUOUS);
	}

	@Test
	public void testParamservBSPEpochDisjointContiguous() {
		runDMLTest(5, 3, Statement.PSUpdateType.BSP, Statement.PSFrequency.EPOCH, 16, Statement.PSScheme.DISJOINT_CONTIGUOUS);
	}

	@Test
	public void testParamservASPEpochDisjointContiguous() {
		runDMLTest(5, 3, Statement.PSUpdateType.ASP, Statement.PSFrequency.EPOCH, 16, Statement.PSScheme.DISJOINT_CONTIGUOUS);
	}

	@Test
	public void testParamservWorkerFailed() {
		runDMLTest(TEST_NAME2, true, DMLException.class, "Invalid indexing by name in unnamed list: worker_err.");
	}

	@Test
	public void testParamservAggServiceFailed() {
		runDMLTest(TEST_NAME3, true, DMLException.class, "Invalid indexing by name in unnamed list: agg_service_err.");
	}

	private void runDMLTest(String testname, boolean exceptionExpected, Class<?> expectedException, String errMessage) {
		programArgs = new String[] { "-explain" };
		internalRunDMLTest(testname, exceptionExpected, expectedException, errMessage);
	}

	private void internalRunDMLTest(String testname, boolean exceptionExpected, Class<?> expectedException,
			String errMessage) {
		DMLScript.RUNTIME_PLATFORM oldRtplatform = rtplatform;
		boolean oldUseLocalSparkConfig = DMLScript.USE_LOCAL_SPARK_CONFIG;
		rtplatform = DMLScript.RUNTIME_PLATFORM.HYBRID_SPARK;
		DMLScript.USE_LOCAL_SPARK_CONFIG = true;

		try {
			TestConfiguration config = getTestConfiguration(testname);
			loadTestConfiguration(config);
			String HOME = SCRIPT_DIR + TEST_DIR;
			fullDMLScriptName = HOME + testname + ".dml";
			runTest(true, exceptionExpected, expectedException, errMessage, -1);
		} finally {
			rtplatform = oldRtplatform;
			DMLScript.USE_LOCAL_SPARK_CONFIG = oldUseLocalSparkConfig;
		}
	}

	private void runDMLTest(int epochs, int workers, Statement.PSUpdateType utype, Statement.PSFrequency freq, int batchsize, Statement.PSScheme scheme) {
		programArgs = new String[] { "-explain", "-nvargs", "mode=REMOTE_SPARK", "epochs=" + epochs, "workers=" + workers, "utype=" + utype, "freq=" + freq, "batchsize=" + batchsize, "scheme=" + scheme};
		internalRunDMLTest(TEST_NAME1, false, null, null);
	}
}
