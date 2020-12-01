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

package org.apache.sysds.test.functions.lineage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.sysds.common.Types.ExecMode;
import org.apache.sysds.hops.OptimizerUtils;
import org.apache.sysds.hops.recompile.Recompiler;
import org.apache.sysds.lops.LopProperties.ExecType;
import org.apache.sysds.runtime.lineage.Lineage;
import org.apache.sysds.runtime.lineage.LineageCacheConfig.ReuseCacheType;
import org.apache.sysds.runtime.matrix.data.MatrixValue;
import org.apache.sysds.test.TestConfiguration;
import org.apache.sysds.test.TestUtils;
import org.junit.Test;

public class SBFullReuseTest extends LineageBase {
	
	protected static final String TEST_DIR = "functions/lineage/";
	protected static final String TEST_NAME1 = "SBFullReuse1";
	protected static final String TEST_NAME2 = "SBFullReuse2";
	protected static final String TEST_NAME3 = "SBFullReuse3";
	protected String TEST_CLASS_DIR = TEST_DIR + SBFullReuseTest.class.getSimpleName() + "/";
	
	@Override
	public void setUp() {
		TestUtils.clearAssertionInformation();
		addTestConfiguration(TEST_NAME1, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME1));
		addTestConfiguration(TEST_NAME2, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME2));
		addTestConfiguration(TEST_NAME3, new TestConfiguration(TEST_CLASS_DIR, TEST_NAME3));
	}
	
	@Test
	public void testCacheHit() {
		testLineageTrace(TEST_NAME1);
	}
	
	@Test
	public void testCacheMiss() {
		testLineageTrace(TEST_NAME2);
	}

	@Test
	public void testCacheFallThrough() {
		testLineageTrace(TEST_NAME3);
	}
	
	public void testLineageTrace(String testname) {
		boolean old_simplification = OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION;
		boolean old_sum_product = OptimizerUtils.ALLOW_SUM_PRODUCT_REWRITES;
		ExecMode platformOld = setExecMode(ExecType.CP);
		
		try {
			LOG.debug("------------ BEGIN " + testname + "------------");
			
			OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION = false;
			OptimizerUtils.ALLOW_SUM_PRODUCT_REWRITES = false;
			
			getAndLoadTestConfiguration(testname);
			fullDMLScriptName = getScript();
			
			// Without lineage-based reuse enabled
			List<String> proArgs = new ArrayList<>();
			proArgs.add("-stats");
			proArgs.add("-lineage");
			proArgs.add("-args");
			proArgs.add(output("X"));
			programArgs = proArgs.toArray(new String[proArgs.size()]);
			
			Lineage.resetInternalState();
			runTest(true, EXCEPTION_NOT_EXPECTED, null, -1);
			HashMap<MatrixValue.CellIndex, Double> X_orig = readDMLMatrixFromOutputDir("X");
			
			// With lineage-based reuse enabled
			proArgs.clear();
			proArgs.add("-stats");
			proArgs.add("-lineage");
			proArgs.add(ReuseCacheType.REUSE_MULTILEVEL.name().toLowerCase());
			proArgs.add("-args");
			proArgs.add(output("X"));
			programArgs = proArgs.toArray(new String[proArgs.size()]);
			
			Lineage.resetInternalState();
			Lineage.setLinReuseFull();
			runTest(true, EXCEPTION_NOT_EXPECTED, null, -1);
			HashMap<MatrixValue.CellIndex, Double> X_reused = readDMLMatrixFromOutputDir("X");
			Lineage.setLinReuseNone();
			
			TestUtils.compareMatrices(X_orig, X_reused, 1e-6, "Origin", "Reused");
		}
		finally {
			OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION = old_simplification;
			OptimizerUtils.ALLOW_SUM_PRODUCT_REWRITES = old_sum_product;
			rtplatform = platformOld;
			Recompiler.reinitRecompiler();
		}
	}
}
