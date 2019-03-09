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
package org.apache.sysml.runtime.instructions.gpu.context;

import static jcuda.runtime.JCuda.cudaFree;
import static jcuda.runtime.JCuda.cudaMallocManaged;
import static jcuda.runtime.JCuda.cudaMemGetInfo;
import static jcuda.runtime.cudaError.cudaSuccess;
import static jcuda.runtime.JCuda.cudaMemAttachGlobal;
import org.apache.sysml.conf.ConfigurationManager;
import org.apache.sysml.conf.DMLConfig;

import jcuda.CudaException;
import jcuda.Pointer;
import jcuda.runtime.cudaError;

public class UnifiedMemoryAllocator  implements GPUMemoryAllocator {

	private final double GPU_MEMORY_UTILIZATION_FACTOR = ConfigurationManager.getDMLConfig().getDoubleValue(DMLConfig.GPU_MEMORY_UTILIZATION_FACTOR);
	
	/**
	 * Allocate memory on the device. 
	 * 
	 * @param devPtr Pointer to allocated device memory
	 * @param size size in bytes
	 * @throws jcuda.CudaException if unable to allocate
	 */
	public void allocate(Pointer devPtr, long size) throws CudaException {
		int status = cudaMallocManaged(devPtr, size, cudaMemAttachGlobal);
		if(status != cudaSuccess) {
			throw new jcuda.CudaException("cudaMallocManaged failed:" + cudaError.stringFor(status));
		}
		
	}

	/**
	 * Frees memory on the device
	 * 
	 * @param devPtr Device pointer to memory to free
	 * @throws jcuda.CudaException if error occurs
	 */
	public void free(Pointer devPtr) throws CudaException {
		int status = cudaFree(devPtr);
		if(status != cudaSuccess) {
			throw new jcuda.CudaException("cudaFree failed:" + cudaError.stringFor(status));
		}
	}
	
	private static long maxAvailableMemory = -1;
	private static double gpuUtilizationFactor = -1;
	
	/**
	 * Check if there is enough memory to allocate a pointer of given size 
	 * 
	 * @param size size in bytes
	 * @return true if there is enough available memory to allocate a pointer of the given size 
	 */
	public boolean canAllocate(long size) {
		return true; // Unified memory can allocate any amount of memory. Note: all allocations are guarded by SystemML's optimizer which uses getAvailableMemory
	}
	
	/**
	 * Gets the available memory on GPU that SystemML can use.
	 *
	 * @return the available memory in bytes
	 */
	public long getAvailableMemory() {
		if(maxAvailableMemory < 0 || gpuUtilizationFactor != GPU_MEMORY_UTILIZATION_FACTOR) {
			long free[] = { 0 };
			long total[] = { 0 };
			cudaMemGetInfo(free, total);
			maxAvailableMemory = (long) (total[0] * GPU_MEMORY_UTILIZATION_FACTOR);
			gpuUtilizationFactor = GPU_MEMORY_UTILIZATION_FACTOR;
		}
		return maxAvailableMemory;
	}

}
