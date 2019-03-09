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

package org.apache.sysml.test.integration.functions.reorg;

import java.util.HashMap;

import org.junit.Test;

import org.apache.sysml.api.DMLScript;
import org.apache.sysml.api.DMLScript.RUNTIME_PLATFORM;
import org.apache.sysml.hops.OptimizerUtils;
import org.apache.sysml.hops.ReorgOp;
import org.apache.sysml.lops.LopProperties.ExecType;
import org.apache.sysml.runtime.matrix.data.MatrixValue.CellIndex;
import org.apache.sysml.test.integration.AutomatedTestBase;
import org.apache.sysml.test.integration.TestConfiguration;
import org.apache.sysml.test.utils.TestUtils;

/**
 * TODO add tests for dynamically computed ordering parameters; in hybrid execution mode
 * this works via dag splits and dynamic recompilation; however in this test we cannot force
 * MR and use dynamic recompilation recompilation at the same time. 
 * 
 */
public class FullOrderTest extends AutomatedTestBase 
{
	private final static String TEST_NAME1 = "Order";
	private final static String TEST_NAME2 = "OrderDyn";
	
	private final static String TEST_DIR = "functions/reorg/";
	private static final String TEST_CLASS_DIR = TEST_DIR + FullOrderTest.class.getSimpleName() + "/";
	private final static double eps = 1e-10;
	
	private final static int rows1 = 1017;
	private final static int rows2 = 42057;
	private final static int cols1 = 7;	
	private final static int by = 3;
	private final static double sparsity1 = 0.7;
	private final static double sparsity2 = 0.1;
	
	public enum InputType {
		DENSE,
		SPARSE,
		EMPTY,
	}
	
	/**
	 * Main method for running one test at a time.
	 */
	public static void main(String[] args) {
		long startMsec = System.currentTimeMillis();
		
		FullOrderTest t = new FullOrderTest();
		t.setUpBase();
		t.setUp();
		t.testOrderVectorIndexAscEmptyNoRewriteMR();
		t.tearDown();

		long elapsedMsec = System.currentTimeMillis() - startMsec;
		System.err.printf("Finished in %1.3f sec\n", elapsedMsec / 1000.0);
	}
	
	@Override
	public void setUp() 
	{
		TestUtils.clearAssertionInformation();
		addTestConfiguration(TEST_NAME1,
			new TestConfiguration(TEST_CLASS_DIR, TEST_NAME1,new String[]{"B"}));
		addTestConfiguration(TEST_NAME2,
			new TestConfiguration(TEST_CLASS_DIR, TEST_NAME2,new String[]{"B"}));
	}

	
	@Test
	public void testOrderMatrixDataAscDenseCP() 
	{
		runOrderTest(true, InputType.DENSE, false, false, true, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixIndexAscDenseCP() 
	{
		runOrderTest(true, InputType.DENSE, false, true, true, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixDataAscSparseCP() 
	{
		runOrderTest(true, InputType.SPARSE, false, false, true, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixIndexAscSparseCP() 
	{
		runOrderTest(true, InputType.SPARSE, false, true, true, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixDataAscEmptyCP() 
	{
		runOrderTest(true, InputType.EMPTY, false, false, true, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixIndexAscEmptyCP() 
	{
		runOrderTest(true, InputType.EMPTY, false, true, true, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixDataDescDenseCP() 
	{
		runOrderTest(true, InputType.DENSE, true, false, true, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixIndexDescDenseCP() 
	{
		runOrderTest(true, InputType.DENSE, true, true, true, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixDataDescSparseCP() 
	{
		runOrderTest(true, InputType.SPARSE, true, false, true, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixIndexDescSparseCP() 
	{
		runOrderTest(true, InputType.SPARSE, true, true, true, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixDataDescEmptyCP() 
	{
		runOrderTest(true, InputType.EMPTY, true, false, true, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixIndexDescEmptyCP() 
	{
		runOrderTest(true, InputType.EMPTY, true, true, true, ExecType.CP);
	}

	@Test
	public void testOrderVectorDataAscDenseCP() 
	{
		runOrderTest(false, InputType.DENSE, false, false, true, ExecType.CP);
	}
	
	@Test
	public void testOrderVectorIndexAscDenseCP() 
	{
		runOrderTest(false, InputType.DENSE, false, true, true, ExecType.CP);
	}
	
	@Test
	public void testOrderVectorDataAscSparseCP() 
	{
		runOrderTest(false, InputType.SPARSE, false, false, true, ExecType.CP);
	}
	
	@Test
	public void testOrderVectorIndexAscSparseCP() 
	{
		runOrderTest(false, InputType.SPARSE, false, true, true, ExecType.CP);
	}
	
	@Test
	public void testOrderVectorDataAscEmptyCP() 
	{
		runOrderTest(false, InputType.EMPTY, false, false, true, ExecType.CP);
	}
	
	@Test
	public void testOrderVectorIndexAscEmptyCP() 
	{
		runOrderTest(false, InputType.EMPTY, false, true, true, ExecType.CP);
	}
	
	@Test
	public void testOrderVectorDataDescDenseCP() 
	{
		runOrderTest(false, InputType.DENSE, true, false, true, ExecType.CP);
	}
	
	@Test
	public void testOrderVectorIndexDescDenseCP() 
	{
		runOrderTest(false, InputType.DENSE, true, true, true, ExecType.CP);
	}
	
	@Test
	public void testOrderVectorDataDescSparseCP() 
	{
		runOrderTest(false, InputType.SPARSE, true, false, true, ExecType.CP);
	}
	
	@Test
	public void testOrderVectorIndexDescSparseCP() 
	{
		runOrderTest(false, InputType.SPARSE, true, true, true, ExecType.CP);
	}
	
	@Test
	public void testOrderVectorDataDescEmptyCP() 
	{
		runOrderTest(false, InputType.EMPTY, true, false, true, ExecType.CP);
	}
	
	@Test
	public void testOrderVectorIndexDescEmptyCP() 
	{
		runOrderTest(false, InputType.EMPTY, true, true, true, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixDataAscEmptyNoRewriteCP() 
	{
		runOrderTest(true, InputType.EMPTY, false, false, false, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixIndexAscEmptyNoRewriteCP() 
	{
		runOrderTest(true, InputType.EMPTY, false, true, false, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixDataDescEmptyNoRewriteCP() 
	{
		runOrderTest(true, InputType.EMPTY, true, false, false, ExecType.CP);
	}
	
	@Test
	public void testOrderMatrixIndexDescEmptyNoRewriteCP() 
	{
		runOrderTest(true, InputType.EMPTY, true, true, false, ExecType.CP);
	}

	@Test
	public void testOrderVectorDataAscEmptyNoRewriteCP() 
	{
		runOrderTest(false, InputType.EMPTY, false, false, false, ExecType.CP);
	}
	
	@Test
	public void testOrderVectorIndexAscEmptyNoRewriteCP() 
	{
		runOrderTest(false, InputType.EMPTY, false, true, false, ExecType.CP);
	}
	
	@Test
	public void testOrderVectorDataDescEmptyNoRewriteCP() 
	{
		runOrderTest(false, InputType.EMPTY, true, false, false, ExecType.CP);
	}
	
	@Test
	public void testOrderVectorIndexDescEmptyNoRewriteCP() 
	{
		runOrderTest(false, InputType.EMPTY, true, true, false, ExecType.CP);
	}
	
	// ----------------------------
	
	@Test
	public void testOrderMatrixDataAscDenseSP() 
	{
		runOrderTest(true, InputType.DENSE, false, false, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderMatrixIndexAscDenseSP() 
	{
		runOrderTest(true, InputType.DENSE, false, true, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderMatrixDataAscSparseSP() 
	{
		runOrderTest(true, InputType.SPARSE, false, false, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderMatrixIndexAscSparseSP() 
	{
		runOrderTest(true, InputType.SPARSE, false, true, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderMatrixDataAscEmptySP() 
	{
		runOrderTest(true, InputType.EMPTY, false, false, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderMatrixIndexAscEmptySP() 
	{
		runOrderTest(true, InputType.EMPTY, false, true, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderMatrixDataDescDenseSP() 
	{
		runOrderTest(true, InputType.DENSE, true, false, true, ExecType.SPARK, true);
	}
	
	@Test
	public void testOrderMatrixIndexDescDenseSP() 
	{
		runOrderTest(true, InputType.DENSE, true, true, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderMatrixDataDescSparseSP() 
	{
		runOrderTest(true, InputType.SPARSE, true, false, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderMatrixIndexDescSparseSP() 
	{
		runOrderTest(true, InputType.SPARSE, true, true, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderMatrixDataDescEmptySP() 
	{
		runOrderTest(true, InputType.EMPTY, true, false, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderMatrixIndexDescEmptySP() 
	{
		runOrderTest(true, InputType.EMPTY, true, true, true, ExecType.SPARK);
	}

	@Test
	public void testOrderVectorDataAscDenseSP() 
	{
		runOrderTest(false, InputType.DENSE, false, false, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderVectorIndexAscDenseSP() 
	{
		runOrderTest(false, InputType.DENSE, false, true, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderVectorDataAscSparseSP() 
	{
		runOrderTest(false, InputType.SPARSE, false, false, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderVectorIndexAscSparseSP() 
	{
		runOrderTest(false, InputType.SPARSE, false, true, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderVectorDataAscEmptySP() 
	{
		runOrderTest(false, InputType.EMPTY, false, false, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderVectorIndexAscEmptySP() 
	{
		runOrderTest(false, InputType.EMPTY, false, true, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderVectorDataDescDenseSP() 
	{
		runOrderTest(false, InputType.DENSE, true, false, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderVectorIndexDescDenseSP() 
	{
		runOrderTest(false, InputType.DENSE, true, true, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderVectorDataDescSparseSP() 
	{
		runOrderTest(false, InputType.SPARSE, true, false, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderVectorIndexDescSparseSP() 
	{
		runOrderTest(false, InputType.SPARSE, true, true, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderVectorDataDescEmptySP() 
	{
		runOrderTest(false, InputType.EMPTY, true, false, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderVectorIndexDescEmptySP() 
	{
		runOrderTest(false, InputType.EMPTY, true, true, true, ExecType.SPARK);
	}
	
	@Test
	public void testOrderMatrixDataAscEmptyNoRewriteSP() 
	{
		runOrderTest(true, InputType.EMPTY, false, false, false, ExecType.SPARK);
	}
	
	@Test
	public void testOrderMatrixIndexAscEmptyNoRewriteSP() 
	{
		runOrderTest(true, InputType.EMPTY, false, true, false, ExecType.SPARK);
	}
	
	@Test
	public void testOrderMatrixDataDescEmptyNoRewriteSP() 
	{
		runOrderTest(true, InputType.EMPTY, true, false, false, ExecType.SPARK);
	}
	
	@Test
	public void testOrderMatrixIndexDescEmptyNoRewriteSP() 
	{
		runOrderTest(true, InputType.EMPTY, true, true, false, ExecType.SPARK);
	}

	@Test
	public void testOrderVectorDataAscEmptyNoRewriteSP() 
	{
		runOrderTest(false, InputType.EMPTY, false, false, false, ExecType.SPARK);
	}
	
	@Test
	public void testOrderVectorIndexAscEmptyNoRewriteSP() 
	{
		runOrderTest(false, InputType.EMPTY, false, true, false, ExecType.SPARK);
	}
	
	@Test
	public void testOrderVectorDataDescEmptyNoRewriteSP() 
	{
		runOrderTest(false, InputType.EMPTY, true, false, false, ExecType.SPARK);
	}
	
	@Test
	public void testOrderVectorIndexDescEmptyNoRewriteSP() 
	{
		runOrderTest(false, InputType.EMPTY, true, true, false, ExecType.SPARK);
	}
	
	// ----------------------------
	
	@Test
	public void testOrderMatrixDataAscDenseMR() 
	{
		runOrderTest(true, InputType.DENSE, false, false, true, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixIndexAscDenseMR() 
	{
		runOrderTest(true, InputType.DENSE, false, true, true, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixDataAscSparseMR() 
	{
		runOrderTest(true, InputType.SPARSE, false, false, true, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixIndexAscSparseMR() 
	{
		runOrderTest(true, InputType.SPARSE, false, true, true, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixDataAscEmptyMR() 
	{
		runOrderTest(true, InputType.EMPTY, false, false, true, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixIndexAscEmptyMR() 
	{
		runOrderTest(true, InputType.EMPTY, false, true, true, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixDataDescDenseMR() 
	{
		runOrderTest(true, InputType.DENSE, true, false, true, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixIndexDescDenseMR() 
	{
		runOrderTest(true, InputType.DENSE, true, true, true, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixDataDescSparseMR() 
	{
		runOrderTest(true, InputType.SPARSE, true, false, true, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixIndexDescSparseMR() 
	{
		runOrderTest(true, InputType.SPARSE, true, true, true, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixDataDescEmptyMR() 
	{
		runOrderTest(true, InputType.EMPTY, true, false, true, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixIndexDescEmptyMR() 
	{
		runOrderTest(true, InputType.EMPTY, true, true, true, ExecType.MR);
	}

	@Test
	public void testOrderVectorDataAscDenseMR() 
	{
		runOrderTest(false, InputType.DENSE, false, false, true, ExecType.MR);
	}
	
	@Test
	public void testOrderVectorIndexAscDenseMR() 
	{
		runOrderTest(false, InputType.DENSE, false, true, true, ExecType.MR);
	}
	
	@Test
	public void testOrderVectorDataAscSparseMR() 
	{
		runOrderTest(false, InputType.SPARSE, false, false, true, ExecType.MR);
	}
	
	@Test
	public void testOrderVectorIndexAscSparseMR() 
	{
		runOrderTest(false, InputType.SPARSE, false, true, true, ExecType.MR);
	}
	
	@Test
	public void testOrderVectorDataAscEmptyMR() 
	{
		runOrderTest(false, InputType.EMPTY, false, false, true, ExecType.MR);
	}
	
	@Test
	public void testOrderVectorIndexAscEmptyMR() 
	{
		runOrderTest(false, InputType.EMPTY, false, true, true, ExecType.MR);
	}
	
	@Test
	public void testOrderVectorDataDescDenseMR() 
	{
		runOrderTest(false, InputType.DENSE, true, false, true, ExecType.MR);
	}
	
	@Test
	public void testOrderVectorIndexDescDenseMR() 
	{
		runOrderTest(false, InputType.DENSE, true, true, true, ExecType.MR);
	}
	
	@Test
	public void testOrderVectorDataDescSparseMR() 
	{
		runOrderTest(false, InputType.SPARSE, true, false, true, ExecType.MR);
	}
	
	@Test
	public void testOrderVectorIndexDescSparseMR() 
	{
		runOrderTest(false, InputType.SPARSE, true, true, true, ExecType.MR);
	}
	
	@Test
	public void testOrderVectorDataDescEmptyMR() 
	{
		runOrderTest(false, InputType.EMPTY, true, false, true, ExecType.MR);
	}
	
	@Test
	public void testOrderVectorIndexDescEmptyMR() 
	{
		runOrderTest(false, InputType.EMPTY, true, true, true, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixDataAscEmptyNoRewriteMR() 
	{
		runOrderTest(true, InputType.EMPTY, false, false, false, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixIndexAscEmptyNoRewriteMR() 
	{
		runOrderTest(true, InputType.EMPTY, false, true, false, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixDataDescEmptyNoRewriteMR() 
	{
		runOrderTest(true, InputType.EMPTY, true, false, false, ExecType.MR);
	}
	
	@Test
	public void testOrderMatrixIndexDescEmptyNoRewriteMR() 
	{
		runOrderTest(true, InputType.EMPTY, true, true, false, ExecType.MR);
	}

	@Test
	public void testOrderVectorDataAscEmptyNoRewriteMR() 
	{
		runOrderTest(false, InputType.EMPTY, false, false, false, ExecType.MR);
	}
	
	@Test
	public void testOrderVectorIndexAscEmptyNoRewriteMR() 
	{
		runOrderTest(false, InputType.EMPTY, false, true, false, ExecType.MR);
	}
	
	@Test
	public void testOrderVectorDataDescEmptyNoRewriteMR() 
	{
		runOrderTest(false, InputType.EMPTY, true, false, false, ExecType.MR);
	}
	
	@Test
	public void testOrderVectorIndexDescEmptyNoRewriteMR() 
	{
		runOrderTest(false, InputType.EMPTY, true, true, false, ExecType.MR);
	}

	//TODO enable all index tests as soon as sort piggybacking issue is fixed.
	
	@Test
	public void testOrderMatrixDataAscDenseMR2() 
	{
		runOrderTest(true, InputType.DENSE, false, false, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixIndexAscDenseMR2() 
	{
		runOrderTest(true, InputType.DENSE, false, true, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixDataAscSparseMR2() 
	{
		runOrderTest(true, InputType.SPARSE, false, false, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixIndexAscSparseMR2() 
	{
		runOrderTest(true, InputType.SPARSE, false, true, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixDataAscEmptyMR2() 
	{
		runOrderTest(true, InputType.EMPTY, false, false, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixIndexAscEmptyMR2() 
	{
		runOrderTest(true, InputType.EMPTY, false, true, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixDataDescDenseMR2() 
	{
		runOrderTest(true, InputType.DENSE, true, false, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixIndexDescDenseMR2() 
	{
		runOrderTest(true, InputType.DENSE, true, true, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixDataDescSparseMR2() 
	{
		runOrderTest(true, InputType.SPARSE, true, false, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixIndexDescSparseMR2() 
	{
		runOrderTest(true, InputType.SPARSE, true, true, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixDataDescEmptyMR2() 
	{
		runOrderTest(true, InputType.EMPTY, true, false, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixIndexDescEmptyMR2() 
	{
		runOrderTest(true, InputType.EMPTY, true, true, true, ExecType.MR, true);
	}

	@Test
	public void testOrderVectorDataAscDenseMR2() 
	{
		runOrderTest(false, InputType.DENSE, false, false, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderVectorIndexAscDenseMR2() 
	{
		runOrderTest(false, InputType.DENSE, false, true, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderVectorDataAscSparseMR2() 
	{
		runOrderTest(false, InputType.SPARSE, false, false, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderVectorIndexAscSparseMR2() 
	{
		runOrderTest(false, InputType.SPARSE, false, true, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderVectorDataAscEmptyMR2() 
	{
		runOrderTest(false, InputType.EMPTY, false, false, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderVectorIndexAscEmptyMR2() 
	{
		runOrderTest(false, InputType.EMPTY, false, true, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderVectorDataDescDenseMR2() 
	{
		runOrderTest(false, InputType.DENSE, true, false, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderVectorIndexDescDenseMR2() 
	{
		runOrderTest(false, InputType.DENSE, true, true, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderVectorDataDescSparseMR2() 
	{
		runOrderTest(false, InputType.SPARSE, true, false, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderVectorIndexDescSparseMR2() 
	{
		runOrderTest(false, InputType.SPARSE, true, true, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderVectorDataDescEmptyMR2() 
	{
		runOrderTest(false, InputType.EMPTY, true, false, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderVectorIndexDescEmptyMR2() 
	{
		runOrderTest(false, InputType.EMPTY, true, true, true, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixDataAscEmptyNoRewriteMR2() 
	{
		runOrderTest(true, InputType.EMPTY, false, false, false, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixIndexAscEmptyNoRewriteMR2() 
	{
		runOrderTest(true, InputType.EMPTY, false, true, false, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixDataDescEmptyNoRewriteMR2() 
	{
		runOrderTest(true, InputType.EMPTY, true, false, false, ExecType.MR, true);
	}
	
	@Test
	public void testOrderMatrixIndexDescEmptyNoRewriteMR2() 
	{
		runOrderTest(true, InputType.EMPTY, true, true, false, ExecType.MR, true);
	}

	@Test
	public void testOrderVectorDataAscEmptyNoRewriteMR2() 
	{
		runOrderTest(false, InputType.EMPTY, false, false, false, ExecType.MR, true);
	}
	
	@Test
	public void testOrderVectorIndexAscEmptyNoRewriteMR2() 
	{
		runOrderTest(false, InputType.EMPTY, false, true, false, ExecType.MR, true);
	}
	
	@Test
	public void testOrderVectorDataDescEmptyNoRewriteMR2() 
	{
		runOrderTest(false, InputType.EMPTY, true, false, false, ExecType.MR, true);
	}
	
	@Test
	public void testOrderVectorIndexDescEmptyNoRewriteMR2() 
	{
		runOrderTest(false, InputType.EMPTY, true, true, false, ExecType.MR, true);
	}
	
	//SPECIFIC REWRITE TESTS 
	
	@Test
	public void testOrderMatrixDynDynDenseCP() 
	{
		runOrderTest(TEST_NAME2, true, InputType.DENSE, false, false, true, ExecType.CP);
	}
	
	//-----------------------------
	@Test
	public void testOrderMatrixIndexAscDenseSP_ForceDist() 
	{
		runOrderTest(true, InputType.DENSE, false, true, true, ExecType.SPARK, true);
	}

	@Test
	public void testOrderMatrixDataDescSparseSP_ForceDist() 
	{
		runOrderTest(true, InputType.SPARSE, true, false, true, ExecType.SPARK, true);
	}

	@Test
	public void testOrderVectorDataDescDenseSP_ForceDist() 
	{
		runOrderTest(false, InputType.DENSE, true, false, true, ExecType.SPARK, true);
	}
	

	/**
	 * 
	 * @param matrix
	 * @param dtype
	 * @param desc
	 * @param ixreturn
	 * @param rewrite
	 * @param instType
	 */
	private void runOrderTest( boolean matrix, InputType dtype, boolean desc, boolean ixreturn, boolean rewrite, ExecType instType)
	{
		runOrderTest(TEST_NAME1, matrix, dtype, desc, ixreturn, rewrite, instType, false);
	}
	
	private void runOrderTest( String testname, boolean matrix, InputType dtype, boolean desc, boolean ixreturn, boolean rewrite, ExecType instType)
	{
		runOrderTest(TEST_NAME1, matrix, dtype, desc, ixreturn, rewrite, instType, false);
	}
	
	private void runOrderTest( boolean matrix, InputType dtype, boolean desc, boolean ixreturn, boolean rewrite, ExecType instType, boolean forceDistSort)
	{
		runOrderTest(TEST_NAME1, matrix, dtype, desc, ixreturn, rewrite, instType, forceDistSort);
	}
	
	/**
	 * 
	 * @param sparseM1
	 * @param sparseM2
	 * @param instType
	 */
	private void runOrderTest( String testname, boolean matrix, InputType dtype, boolean desc, boolean ixreturn, boolean rewrite, ExecType instType, boolean forceDistSort)
	{
		boolean sparkConfigOld = DMLScript.USE_LOCAL_SPARK_CONFIG;
		RUNTIME_PLATFORM platformOld = setRuntimePlatform(instType);
		if(shouldSkipTest())
			return;
		
		boolean rewriteOld = OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION;
		boolean forceOpOld = ReorgOp.FORCE_DIST_SORT_INDEXES;
		
		try
		{
			String TEST_NAME = testname;
			
			OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION = rewrite;
			ReorgOp.FORCE_DIST_SORT_INDEXES = forceDistSort;
			
			int rows = matrix ? rows1 : rows2;
			int cols = matrix ? cols1 : 1;
			int bycol = matrix ? by : 1;
			double sparsity = dtype==InputType.DENSE ? sparsity1 : sparsity2;
			
			getAndLoadTestConfiguration(TEST_NAME);
			
			/* This is for running the junit test the new way, i.e., construct the arguments directly */
			String HOME = SCRIPT_DIR + TEST_DIR;
			fullDMLScriptName = HOME + TEST_NAME + ".dml";
			programArgs = new String[]{"-explain","-args", input("A"), Integer.toString(bycol),
				Boolean.toString(desc).toUpperCase(), Boolean.toString(ixreturn).toUpperCase(), output("B") };
			
			fullRScriptName = HOME + TEST_NAME + ".R";
			rCmd = "Rscript" + " " + fullRScriptName + " " + inputDir() + " " + 
				bycol + " " + Boolean.toString(desc).toUpperCase() + " " + 
				Boolean.toString(ixreturn).toUpperCase() + " " + expectedDir();
	
			//generate actual dataset 
			double min = (dtype==InputType.EMPTY)? 0 : -1;
			double max = (dtype==InputType.EMPTY)? 0 : 1;
			int nnz = (dtype==InputType.EMPTY)? 0 : -1;
			
			double[][] A = getRandomMatrix(rows, cols, min, max, sparsity, 7); 
			writeInputMatrixWithMTD("A", A, nnz, true);
	
			boolean exceptionExpected = false;
			runTest(true, exceptionExpected, null, -1); 
			
			runRScript(true); 
		
			//compare matrices 
			HashMap<CellIndex, Double> dmlfile = readDMLMatrixFromHDFS("B");
			HashMap<CellIndex, Double> rfile  = readRMatrixFromFS("B");
			TestUtils.compareMatrices(dmlfile, rfile, eps, "Stat-DML", "Stat-R");
		}
		finally
		{
			//reset flags
			rtplatform = platformOld;
			OptimizerUtils.ALLOW_ALGEBRAIC_SIMPLIFICATION = rewriteOld;
			ReorgOp.FORCE_DIST_SORT_INDEXES = forceOpOld;
			DMLScript.USE_LOCAL_SPARK_CONFIG = sparkConfigOld;
		}
	}
	
		
}
