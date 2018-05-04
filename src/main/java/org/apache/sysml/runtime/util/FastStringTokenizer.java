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

package org.apache.sysml.runtime.util;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * This string tokenizer is essentially a simplified StringTokenizer. 
 * In addition to the default functionality it allows to reset the tokenizer and it makes
 * the simplifying assumptions of (1) no returns delimiter, and (2) single character delimiter.
 * 
 */
public class FastStringTokenizer implements Serializable
{
	private static final long serialVersionUID = 4051436015710778611L;
	private String _string = null;
    private char   _del    = 0;
    private int    _pos    = -1;

    /**
     * Constructs a new StringTokenizer for string using the specified
     * delimiter
     * 
     * @param delimiter
     *            the delimiter to use
     */
    public FastStringTokenizer(char delimiter) 
    {
        _del = delimiter;
        reset( null );
    }

    public void reset( String string )
    {
    	_string = string;
    	_pos = 0;
    }
    
    /**
     * Returns the next token in the string as a String.
     * 
     * @return next token in the string as a String
     */
    public String nextToken() 
    {
    	int len = _string.length();
    	int start = _pos;	
    	
    	//find start (skip over leading delimiters)
    	while(start < len && _del == _string.charAt(start) )
    		start++;
    	
    	//find end (next delimiter) and return
    	if(start < len) {
        	_pos = _string.indexOf(_del, start);
        	if( start < _pos && _pos < len )
        		return _string.substring(start, _pos);
        	else 
        		return _string.substring(start);
        }
  
    	//no next token
		throw new NoSuchElementException();
    }
    
    ////////////////////////////////////////
    // Custom parsing methods for textcell
    ////////////////////////////////////////
    
    public int nextInt()
    {
    	return Integer.parseInt( nextToken() );
    }
    
    public long nextLong()
    {
    	return Long.parseLong( nextToken() );
    }
    
    public double nextDouble()
    {
    	return Double.parseDouble( nextToken() );
    
    	//see nextDoubleForParallel, we use the same double parsing
    	//for sequential and parallel parsing because (1) it is faster (~10%)
    	//and (2) for consistency between sequential and parallel readers
    	
    	//return FloatingDecimal.parseDouble(nextToken());	
    }
    
    public double nextDoubleForParallel()
    {
    	//JDK 8 floating decimal, which removes a severe scalability bottleneck
    	//(synchronized static cache) in JDK7
    	//return FloatingDecimal.parseDouble(nextToken());
    	return Double.parseDouble( nextToken() );
    	
    	/*
    	//return Double.parseDouble( nextToken() );
    	
    	//NOTE: Depending on the platform string-2-double conversions were
    	//the main bottleneck in reading text data. Furthermore, we observed
    	//severe contention on multi-threaded parsing on Linux JDK.
    	// ---
    	//This is a known issue and has been fixed in JDK8.
    	//JDK-7032154 : Performance tuning of sun.misc.FloatingDecimal/FormattedFloatingDecimal
    	
    	// Simple workaround without JDK8 code, however, this does NOT guarantee exactly
    	// the same result due to potential for round off errors. 
    	
    	String val = nextToken();
    	double ret = 0;
    
    	if( UtilFunctions.isSimpleDoubleNumber(val) )
    	{ 
    		int ix = val.indexOf('.'); 
    		if( ix > 0 ) //DOUBLE parsing  
        	{
        		String s1 = val.substring(0, ix);
        		String s2 = val.substring(ix+1);
        		long tmp1 = Long.parseLong(s1);
        		long tmp2 = Long.parseLong(s2);
        		ret = (double)tmp2 / Math.pow(10, s2.length()) + tmp1;
        	}
        	else //LONG parsing and cast to double  
        		ret = (double)Long.parseLong(val);
    	}
    	else 
    	{
    		//fall-back to slow default impl if special characters
    		//e.g., ...E-0X, NAN, +-INFINITY, etc
    		ret = Double.parseDouble( val );
    	}
    	
    	return ret;
    	*/
    }
}
