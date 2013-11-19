/**
 * IBM Confidential
 * OCO Source Materials
 * (C) Copyright IBM Corp. 2010, 2014
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.ibm.bi.dml.packagesupport;


import com.ibm.bi.dml.packagesupport.Matrix;
import com.ibm.bi.dml.packagesupport.Matrix.ValueType;
import com.ibm.bi.dml.packagesupport.PackageFunction;
import com.ibm.bi.dml.packagesupport.PackageRuntimeException;

/**
 * Wrapper class to deNegInfinity matrices by replacing all Negative Infinities
 * with zeros, made by modifying <code>DeNaNWrapper.java</code>
 */
public class DeNegInfinityWrapper extends PackageFunction 
{
	@SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp. 2010, 2013\n" +
                                             "US Government Users Restricted Rights - Use, duplication  disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";
	
	private static final long serialVersionUID = 1L;
	private final String OUTPUT_FILE = "TMP";

	//return matrix
	private Matrix ret;

	@Override
	public int getNumFunctionOutputs() 
	{
		return 1;	
	}

	@Override
	public FIO getFunctionOutput(int pos) 
	{	
		if(pos == 0)
			return ret;
		
		throw new PackageRuntimeException("Invalid function output being requested");
	}

	@Override
	public void execute() 
	{ 
		try 
		{
			Matrix inM = (Matrix) getFunctionInput(0);
			double [][] inData = inM.getMatrixAsDoubleArray();
			for (int i = 0; i < inData.length; i++) {
				for (int j = 0; j < inData[i].length; j++) {
					if (Double.NEGATIVE_INFINITY == inData [i][j]) {
						inData [i][j] = 0.0;
			}   }   }
			//create and copy output matrix		
			String dir = createOutputFilePathAndName( OUTPUT_FILE );	
			ret = new Matrix( dir, inM.getNumRows(), inM.getNumCols(), ValueType.Double );
			ret.setMatrixDoubleArray(inData);
		} 
		catch (Exception e) 
		{
			throw new PackageRuntimeException("Error executing external order function", e);
		}
	}
}
