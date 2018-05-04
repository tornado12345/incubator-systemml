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


package org.apache.sysml.runtime.matrix.data;

import java.io.IOException;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileOutputCommitter;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobContext;
import org.apache.hadoop.mapred.TaskAttemptContext;
import org.apache.hadoop.mapred.TaskAttemptID;
import org.apache.sysml.runtime.matrix.mapred.MRJobConfiguration;


public class MultipleOutputCommitter extends FileOutputCommitter 
{	
	// maintain the map of matrix index to its destination output dir
	// private HashMap<Byte, String> outputmap=new HashMap<Byte, String>();
	private String[] outputs;

	@Override
	public void setupJob(JobContext context) 
		throws IOException 
	{
		super.setupJob(context);
		// get output file directories and create directories
		JobConf conf = context.getJobConf();
		String[] loutputs = MRJobConfiguration.getOutputs(conf);
		for (String dir : loutputs) {
			Path path = new Path(dir);
			FileSystem fs = path.getFileSystem(conf);
			if( !fs.mkdirs(path) )
				LOG.error("Mkdirs failed to create " + path.toString());
		}
	}

	@Override
	public void cleanupJob(JobContext context) 
		throws IOException 
	{
		JobConf conf = context.getJobConf();
		// do the clean up of temporary directory
		Path outputPath = FileOutputFormat.getOutputPath(conf);
		if (outputPath != null) {
			FileSystem fs = outputPath.getFileSystem(conf);
			context.getProgressible().progress();
			if( fs.exists(outputPath) ) 
				fs.delete(outputPath, true);
		}
	}

	@Override
	public void commitTask(TaskAttemptContext context) 
		throws IOException 
	{
		JobConf conf = context.getJobConf();
		TaskAttemptID attemptId = context.getTaskAttemptID();
		
		// get the mapping between index to output filename
		outputs = MRJobConfiguration.getOutputs(conf);
		
		// get temp task output path (compatible with hadoop1 and hadoop2)
		Path taskOutPath = FileOutputFormat.getWorkOutputPath(conf);
		FileSystem fs = taskOutPath.getFileSystem(conf);
		if( !fs.exists(taskOutPath) )
			throw new IOException("Task output path "+ taskOutPath.toString() + "does not exist.");
		
		// move the task outputs to their final places
		context.getProgressible().progress();
		moveFinalTaskOutputs(context, fs, taskOutPath);
		
		// delete the temporary task-specific output directory
		if( !fs.delete(taskOutPath, true) ) 
			LOG.debug("Failed to delete the temporary output directory of task: " + attemptId + " - " + taskOutPath);
	}

	private void moveFinalTaskOutputs(TaskAttemptContext context, FileSystem fs, Path taskOutput)
		throws IOException 
	{
		context.getProgressible().progress();
		
		if( fs.getFileStatus(taskOutput).isDirectory() ) {
			FileStatus[] files = fs.listStatus(taskOutput);
			if (files != null)
				for (FileStatus file : files) //for all files
					if( !file.isDirectory() ) //skip directories
						moveFileToDestination(context, fs, file.getPath());
		}
	}

	private void moveFileToDestination(TaskAttemptContext context, FileSystem fs, Path file) 
		throws IOException 
	{
		TaskAttemptID attemptId = context.getTaskAttemptID();
		
		// get output index and final destination 
		String name =  file.getName(); //e.g., 0-r-00000 
		int index = Integer.parseInt(name.substring(0, name.indexOf("-")));
		Path dest = new Path(outputs[index], name); //e.g., outX/0-r-00000
		
		// move file from 'file' to 'finalPath'
		if( !fs.rename(file, dest) ) {
			if (!fs.delete(dest, true))
				throw new IOException("Failed to delete earlier output " + dest + " for rename of " + file + " in task " + attemptId);
			if (!fs.rename(file, dest)) 
				throw new IOException("Failed to save output " + dest + " for rename of " + file + " in task: " + attemptId);
		}
	}
}
