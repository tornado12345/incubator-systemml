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

package org.apache.sysml.udf.lib;

import org.apache.sysml.runtime.controlprogram.caching.MatrixObject;
import org.apache.sysml.runtime.matrix.data.MatrixBlock;
import org.apache.sysml.udf.FunctionParameter;
import org.apache.sysml.udf.Matrix;
import org.apache.sysml.udf.PackageFunction;
import org.apache.sysml.udf.Matrix.ValueType;
/**
 * Wrapper class for conversions of bit vectors to condensed position vectors.
 * The semantics are equivalent to the following dml snippet:
 *   # bitvector into position vector, e.g., 1011 -&gt; 1034
 *   Bv = seq(1,nrow(B)) * B; 
 *   # gather positions into condensed vector
 *   V = removeEmpty(target=Bv, margin="rows");
 * 
 * Note that the inverse operation would be a scatter that can be implemented 
 * via the following dml snippet:
 *   # position vector into bit vector, e.g., 1034 -&gt; 1011
 *   B = table( V, 1 );
 */
public class GatherWrapper extends PackageFunction 
{
	
	private static final long serialVersionUID = 1L;
	private static final String OUTPUT_FILE = "TMP";

	//return matrix
	private Matrix ret;

	@Override
	public int getNumFunctionOutputs() 
	{
		return 1;	
	}

	@Override
	public FunctionParameter getFunctionOutput(int pos) 
	{	
		if(pos == 0)
			return ret;
		
		throw new RuntimeException("Invalid function output being requested");
	}

	@Override
	public void execute() 
	{ 
		try 
		{
			//get input and meta information
			Matrix inM = (Matrix) getFunctionInput(0);
			MatrixObject mo = inM.getMatrixObject();
			MatrixBlock mb = mo.acquireRead();
			int len1 = mb.getNumRows();
			int len2 = (int) mb.getNonZeros();
			
			//create condensed position vector
			double[][] outM = new double[len2][1];
			int pos = 0;
			for( int i=0; i<len1; i++ ) {
				double val = mb.quickGetValue(i, 0);
				if( val != 0 )
					outM[pos++][0] = i+1;
			}
			mo.release();
			
			//create and copy output matrix		
			String dir = createOutputFilePathAndName( OUTPUT_FILE );	
			ret = new Matrix( dir, mb.getNonZeros(), 1, ValueType.Double );
			ret.setMatrixDoubleArray(outM);
		} 
		catch (Exception e) 
		{
			throw new RuntimeException("Error executing external order function", e);
		}
	}
}
