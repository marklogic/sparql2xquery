/**
 * Copyright (c)2009-2010 Mark Logic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * The use of the Apache License does not indicate that this project is
 * affiliated with the Apache Software Foundation.
 */
package com.marklogic.sparql2xquery.example;

import java.io.File;
import java.io.InputStream;

import sw4j.util.Sw4jException;
import sw4j.util.ToolIO;

public class ToolDataLoader {


	public static String loadDataFromFile(String filename, String filepath) throws Sw4jException{
		File f = new File(filepath + filename);
		String data;
		data = ToolIO.pipeFileToString(f);
		return data;
	}
	
	public static InputStream prepareInputStreamFromFile(String filename, String filepath) throws Sw4jException{
		File f = new File(filepath + filename);
		return ToolIO.prepareFileInputStream(f);
	}
	
}
