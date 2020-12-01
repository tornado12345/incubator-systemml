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

import org.junit.Test;
import org.apache.sysds.hops.OptimizerUtils;
import org.apache.sysds.hops.recompile.Recompiler;
import org.apache.sysds.runtime.lineage.LineageItem;
import org.apache.sysds.runtime.lineage.LineageParser;
import org.apache.sysds.test.AutomatedTestBase;
import org.apache.sysds.test.TestUtils;
import org.apache.sysds.utils.Explain;

public class LineageReadTest extends AutomatedTestBase {
	
	protected static final String TEST_DIR = "functions/lineage/";
	protected static final String TEST_NAME = "LineageRead";
	protected String TEST_CLASS_DIR = TEST_DIR + LineageReadTest.class.getSimpleName() + "/";
	
	@Override
	public void setUp() {
		addTestConfiguration(TEST_CLASS_DIR, TEST_NAME);
	}
	
	@Test
	public void testLineageRead() {
		boolean oldRewrites = OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION;
		boolean oldRewrites2 = OptimizerUtils.ALLOW_SUM_PRODUCT_REWRITES;
		
		try {
			OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION = false;
			OptimizerUtils.ALLOW_SUM_PRODUCT_REWRITES = false;
			getAndLoadTestConfiguration(TEST_NAME);
			
			String lineage =
				"(0) (C) CP°createvar°pREADxxx°target/testTemp/functions/lineage/LineageTraceTest/in/X°false°MATRIX°text°10°5°-1°-1°copy\n" +
				"(2) (I) rblk (0)\n" +
				"(4) (L) 3·SCALAR·INT64·true\n" +
				"(5) (I) * (2) (4)\n" +
				"(7) (L) 5·SCALAR·INT64·true\n" +
				"(8) (I) + (5) (7)\n" +
				"(11) (L) target/testTemp/functions/lineage/LineageTraceTest/out/X.SCALAR.STRING.true\n" +
				"(12) (L) textcell·SCALAR·STRING·true\n" +
				"(13) (I) write (8) (11) (12)\n";
			LineageItem li = LineageParser.parseLineageTrace(lineage);
			TestUtils.compareScalars(lineage, Explain.explain(li));
		}
		finally {
			OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION = oldRewrites;
			OptimizerUtils.ALLOW_SUM_PRODUCT_REWRITES = oldRewrites2;
			Recompiler.reinitRecompiler();
		}
	}
}
