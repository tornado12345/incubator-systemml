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

import static org.apache.sysds.api.mlcontext.ScriptFactory.dml;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaRDD;
import org.apache.sysds.api.mlcontext.MatrixFormat;
import org.apache.sysds.api.mlcontext.MatrixMetadata;
import org.apache.sysds.api.mlcontext.Script;
import org.apache.sysds.runtime.lineage.LineageCacheConfig.ReuseCacheType;
import org.apache.sysds.test.functions.mlcontext.MLContextTestBase;
import org.junit.Test;

public class LineageMLContextTest extends MLContextTestBase {

	protected static final Log LOG = LogFactory.getLog(LineageMLContextTest.class.getName());
	@Test
	public void testPrintLineage() {
		LOG.debug("LineageMLContextTest - JavaRDD<String> IJV sum DML");

		List<String> list = new ArrayList<>();
		list.add("1 1 5");
		list.add("2 2 5");
		list.add("3 3 5");
		JavaRDD<String> javaRDD = sc.parallelize(list);
		MatrixMetadata mm = new MatrixMetadata(MatrixFormat.IJV, 3, 3);

		Script script = dml(
			 "print('sum: '+sum(M+M));"
			+ "print(lineage(M+M));"
			).in("M", javaRDD, mm);
		
		ml.setLineage(ReuseCacheType.NONE);
		String out = MLContextTestBase.executeAndCaptureStdOut(ml,script).getRight();
		assertTrue(out.contains("sum: 30.0"));
		ml.execute(script);
	}
	
	@Test
	public void testReuseSameRDD() {
		LOG.debug("LineageMLContextTest - JavaRDD<String> IJV sum DML");

		List<String> list = new ArrayList<>();
		list.add("1 1 5");
		list.add("2 2 5");
		list.add("3 3 5");
		JavaRDD<String> javaRDD = sc.parallelize(list);
		MatrixMetadata mm = new MatrixMetadata(MatrixFormat.IJV, 3, 3);
		
		Script script = dml(
			 "print('sum: '+sum(M+M));"
			+ "s = lineage(M+M);"
			+"if( sum(M) < 0 )  print(s);"
			).in("M", javaRDD, mm);
		
		ml.setLineage(ReuseCacheType.REUSE_FULL);
		String out = MLContextTestBase.executeAndCaptureStdOut(ml,script).getRight();
		assertTrue(out.contains("sum: 30.0"));
		out = MLContextTestBase.executeAndCaptureStdOut(ml,script).getRight();
		assertTrue(out.contains("sum: 30.0"));
	}
	
	@Test
	public void testNoReuseDifferentRDD() {
		LOG.debug("LineageMLContextTest - JavaRDD<String> IJV sum DML");

		List<String> list = new ArrayList<>();
		list.add("1 1 5");
		list.add("2 2 5");
		list.add("3 3 5");
		JavaRDD<String> javaRDD = sc.parallelize(list);
		MatrixMetadata mm = new MatrixMetadata(MatrixFormat.IJV, 3, 3);
		
		Script script = dml(
			 "print('sum: '+sum(M+M));"
			+ "s = lineage(M+M);"
			+"if( sum(M) < 0 )  print(s);"
			).in("M", javaRDD, mm);
		
		ml.setLineage(ReuseCacheType.REUSE_FULL);
		
		String out = MLContextTestBase.executeAndCaptureStdOut(ml,script).getRight();
		assertTrue(out.contains("sum: 30.0"));
		
		list.add("4 4 5");
		JavaRDD<String> javaRDD2 = sc.parallelize(list);
		MatrixMetadata mm2 = new MatrixMetadata(MatrixFormat.IJV, 4, 4);
		script.in("M", javaRDD2, mm2);
		
		out = MLContextTestBase.executeAndCaptureStdOut(ml,script).getRight();
		assertTrue(out.contains("sum: 40.0"));
	}
}
