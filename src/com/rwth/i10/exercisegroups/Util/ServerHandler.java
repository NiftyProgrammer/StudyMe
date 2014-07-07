package com.rwth.i10.exercisegroups.Util;

import de.contextdata.ContextData;


/**
 * 
 * Singleton class use to create 'MyContext' object with once entered api key, version etc
 * 
 * */
public class ServerHandler {

	private static MyContextData contextData;
	
	public ServerHandler(String mUsername, String mPassword){
		contextData = new MyContextData("http://api.learning-context.de/", 3, mUsername, mPassword, 23, 
				"pcawfzywxcuztk2rbpl58vnts0rzv0mq1bsk9kqk75a52wafph");
	}
	
	public ServerHandler(MyContextData contextData){
		this.contextData = contextData;
	}
	
	public static MyContextData createInstance(String mUsername, String mPassword){
		return new MyContextData("http://api.learning-context.de/", 3, mUsername, mPassword, 23, 
				"pcawfzywxcuztk2rbpl58vnts0rzv0mq1bsk9kqk75a52wafph");
	}
	public static MyContextData createMyInstance(String mUsername, String mPassword){
		return new MyContextData("http://api.learning-context.de/", 3, mUsername, mPassword, 23, 
				"pcawfzywxcuztk2rbpl58vnts0rzv0mq1bsk9kqk75a52wafph");
	}	
	public static void setTimeout(int timeout){
		contextData.setTimeout(timeout);
	}
	public void setGETListener(MyContextData.Listener listener){
		contextData.registerGETListener(listener);
	}
	public void setPOSTListener(MyContextData.Listener listener){
		contextData.registerPOSTListener(listener);
	}
	public static void setListener(MyContextData.Listener listener){
		contextData.registerGETListener(listener);
		contextData.registerPOSTListener(listener);
	}
	
	public static void sendGETRequest(String event, String json){
		contextData.get(event, json);
	}
	public static void sendPOSTRequest(String event, String json){
		contextData.post(event, json);
	}
	
	public static MyContextData getContextData(){
		return contextData;
	}
}
