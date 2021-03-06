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

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.marklogic.sparql2xquery.translator.S2XTranslator;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;

import sw4j.rdf.load.RDFSYNTAX;
import sw4j.rdf.pellet.ToolPellet;
import sw4j.rdf.util.AgentSparql;
import sw4j.util.Sw4jException;

public class ExampleBakesale {
	/*********************************************
	 * config
	 */
  // This allows you to specify a different connection uri by running java -DMLURI="..."
  String connectionUri = System.getProperty("MLURI", "xcc://admin:admin@localhost:8006/bakesale-test");
		
    public static void main(String [] args){
    	new ExampleBakesale().run();
    }

    public void run(){
    	//load bakesale RDF data
    	loadData();
    	
    	//generate inferred triples 
    	generateInferredTriples(this.m_map_url_data.keySet());
    	
    	//put bakesale RDF data into marklogic triple store
    	putDataIntoTripleStore();
    	
    	
    	//load SPARQL queries
    	loadSparql();
    	
    	//translate SPARQL to Xquery 
    	translateSparql2Xquery();
    	
    	//execute sparql
    	executeSparqlQuery();
    	
    	//execute translated Xquery against triple store and display results
    	executeXquery();
    	
    	printDebubInfo();
    }
    
    public void printDebubInfo(){
    	//System.out.println(ToolString.printMapToString(this.m_map_query_sparql));
    	//System.out.println(ToolString.printMapToString(this.m_map_query_xquery));
    	//System.out.println(ToolString.printMapToString(this.m_map_url_insert));
    	//System.out.println(ToolString.printMapToString(this.m_map_url_data));
    	//System.out.println(ToolString.printMapToString(this.m_map_query_sparql_result));

    	for (String szQuery: m_map_query_sparql.keySet()){
    		String szQuerySparql = m_map_query_sparql.get(szQuery);
    		String szQueryXquery = m_map_query_xquery.get(szQuery);
    		//System.out.println(String.format("----sparql----\n%s", szQuerySparql));
    		//System.out.println(String.format("----xquery----\n%s", szQueryXquery ));

    		//ToolJena.printModel(m);
    		if (null!=m_map_query_sparql_result){
    			String szResults_sparql = m_map_query_sparql_result.get(szQuery).toString();    			
        		System.out.println(String.format("[sparql results: %d]", countResults(szResults_sparql)));
        	        		
        		
    			String szResults_xquery = this.m_map_query_xquery_result.get(szQuery);
    			if (null!=szResults_xquery){
    				System.out.println(String.format("[xquery results: %d]", countResults(szResults_xquery)));
/*
            		System.out.println("-----------------");
            		System.out.println(szQuerySparql);
            		System.out.println("-----------------");
            		System.out.println(szResults_sparql.replaceAll("</binding>\\s+<binding", "").replaceAll("http://lod-apps.googlecode.com/svn/trunk/data/bakesale/", ""));
            		System.out.println("-----------------");
            		System.out.println(szQueryXquery);
            		System.out.println("-----------------");
            		System.out.println(szResults_xquery.replaceAll("</binding>\\s+<binding", "").replaceAll("http://lod-apps.googlecode.com/svn/trunk/data/bakesale/", ""));
*/
    			}
    		}

    		System.out.println();
    	}
    	
		System.out.println(String.format("----graph statistics ----\n"));
    	for (String szUrl: m_map_url_data.keySet()){
    		Model m = m_map_url_data.get(szUrl);
    		System.out.println(String.format("[graph: %s]\t[triples: %d]", szUrl, m.size() ));
    	}
    }

    
	final static String PATH_FILE_DATA= "data/bakesale/";
	final static String PATH_URL_DATA= "http://lod-apps.googlecode.com/svn/trunk/data/bakesale/";
	final static String DATA_RDF_FORMAT= "N3";

	private String getDataUrl(String szFileName){
		return PATH_URL_DATA + szFileName;
	}
	
    Map<String,Model> m_map_url_data = null;
    private Map<String,Model> loadData(){
    	if (null==m_map_url_data){
    		m_map_url_data = new TreeMap<String,Model> ();
    		
    		String [] filenames = new String[]{
    			"bs-event.ttl",	
    			"bs-location.ttl",	
    			"bs-ontology.ttl",	
    			"bs-student.ttl",	
    			"bs-work.ttl",	
    		};
    		
    		for (String szFileName: filenames){
    			String szUrl = getDataUrl( szFileName);
    			
    			InputStream in;
				try {
					in = ToolDataLoader.prepareInputStreamFromFile( szFileName, PATH_FILE_DATA);
	    			Model m = ModelFactory.createDefaultModel();
	    			m.read(in, szUrl, DATA_RDF_FORMAT);
					m_map_url_data.put(szUrl,m);
					
				} catch (Sw4jException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}    
    	return m_map_url_data;
    }

    //public static final String FILENAME_DIFF_INF_FULL= "bakesale-diff-inf-full";
    //public static final String FILENAME_DIFF_INF_ONTO= "bakesale-diff-inf-onto";
    
    private Map<String,Model> generateInferredTriples(Collection<String> filenames){
    	if (null==m_map_url_data){
    		return null;
    	}

    	Model m_src= ModelFactory.createDefaultModel();
    	for(String szUrl : filenames){
    		Model m = m_map_url_data.get(szUrl);
    		m_src.add(m);
    	}

    	try {
    		//generate deductive closure
    		Model m_inf=ToolPellet.model_createDeductiveClosure(m_src);
    		
    		//only keep the diff
       		m_inf.remove(m_src);
    		
       		//keep the inferred triples in default graph (with on name, i.e. no source)
			m_map_url_data.put ("",m_inf);
		} catch (Sw4jException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return m_map_url_data;
    }

    
    Map<String,String> m_map_url_insert= null;
    private Map<String,String> putDataIntoTripleStore(){
    	if (null==m_map_url_data){
    		return null;
    	}
    	if (null==m_map_url_insert){
    		m_map_url_insert = new TreeMap<String,String> ();
    	
	    	for (String szNamedGraph : m_map_url_data.keySet()){
	    		Model m = m_map_url_data.get(szNamedGraph);
	    		
	    		String szTextInsert = S2XTranslator.translateRdfDataToMarkLogicInsert(m, szNamedGraph);
	    		m_map_url_insert.put(szNamedGraph, szTextInsert);
	    		
	    		//insert
		    	ToolMarkLogicQueryRunner cq;
				try {
					cq = new ToolMarkLogicQueryRunner(new URI(connectionUri));
			    	cq.executeToSingleString(szTextInsert,"\n");
										
				} catch (XccConfigException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RequestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
	    	}
    	}
    	return m_map_url_insert;
    }

    
	final static String PATH_FILE_QUERY = "data/bakesale/sparql/";
	//final static String PATH_URL_QUERY = "http://lod-apps.googlecode.com/svn/trunk/data/bakesale/sparql/";

    Map<String,String> m_map_query_sparql = null;
    private Map<String,String> loadSparql(){
    	if (null==m_map_query_sparql){
    		m_map_query_sparql = new TreeMap<String,String> ();
    		
    		String [] querynames = new String[]{
    			"bakesale-query-31.sparql",	
    			"bakesale-query-32.sparql",	
    			"bakesale-query-33.sparql",	
    			"bakesale-query-34.sparql",	
    			"bakesale-query-36.sparql",	
    		};
    		
    		for (String szQueryName: querynames){
				try {
	    			String szQuery;
					szQuery = ToolDataLoader.loadDataFromFile( szQueryName, PATH_FILE_QUERY);					
	    			m_map_query_sparql.put(szQueryName,szQuery);
				} catch (Sw4jException e) {
					e.printStackTrace();
				}
    		}
    	}
    
    	return m_map_query_sparql;
    }
    
    Map<String,String> m_map_query_xquery= null;
    private Map<String,String> translateSparql2Xquery(){
    	if (null==m_map_query_sparql){
    		return m_map_query_xquery;
    	}

    	if (null==m_map_query_xquery){
    		m_map_query_xquery = new TreeMap<String,String> ();
    		    		
    		for (String szQueryName: m_map_query_sparql.keySet()){
    			String szQuerySparql = m_map_query_sparql.get(szQueryName);
    			
				String szQuery = S2XTranslator.translateSparqlQuery(szQuerySparql);
    			m_map_query_xquery.put(szQueryName,szQuery);
    		}
    	}
    
    	return m_map_query_xquery;
    }

    Map<String,Object> m_map_query_sparql_result = null;
    private Map<String,Object> executeSparqlQuery(){
    	if (null==m_map_query_sparql){
    		return null;
    	}

    	if (null==m_map_query_sparql_result){
    		m_map_query_sparql_result = new TreeMap<String,Object>();
    		    		
    		for (String szQueryName: m_map_query_sparql.keySet()){
    			String szQuerySparql = m_map_query_sparql.get(szQueryName);
    			
    			DataSource ds = DatasetFactory.create();
    			Model m_full = ModelFactory.createDefaultModel();
    			for(String szUrl: this.m_map_url_data.keySet()){
    				Model m = m_map_url_data.get(szUrl);
    				if (szUrl.length()>0)
    					ds.addNamedModel(szUrl, m);
    				m_full.add(m);
    			}
				ds.setDefaultModel(m_full);
    			
            	Object results = new AgentSparql().exec(szQuerySparql, ds, RDFSYNTAX.SPARQL_XML);
            	m_map_query_sparql_result.put(szQueryName,results);
    		}
    	}
    
    	return m_map_query_sparql_result;
    }

    Map<String,String> m_map_query_xquery_result = null;
    private Map<String,String> executeXquery(){
    	if (null==m_map_query_xquery){
    		return null;
    	}

    	if (null==m_map_query_xquery_result){
    		m_map_query_xquery_result = new TreeMap<String,String>();
    		    		
    		for (String szQueryName: m_map_query_xquery.keySet()){
    			String szQueryXquery = m_map_query_xquery.get(szQueryName);
    			

			    	ToolMarkLogicQueryRunner cq;
					try {
						cq = new ToolMarkLogicQueryRunner(new URI(connectionUri));
				    	String results = cq.executeToSingleString(szQueryXquery,"\n");
						
				    	m_map_query_xquery_result.put(szQueryName,results);
						
					} catch (XccConfigException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RequestException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    		}
    	}
    
    	return m_map_query_xquery_result;
    }
     
    private int countResults(String results){
    	Pattern pattern = Pattern.compile("<result>");
    	Matcher matcher = pattern.matcher(results);
    	int cnt=0;
    	while ( matcher.find())
    		cnt++;

    	return cnt;
    	
    }
}
