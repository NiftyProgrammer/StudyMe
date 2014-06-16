package com.rwth.i10.exercisegroups.Util;

import de.contextdata.ContextData;

public class ServerHandler {

	private static ContextData contextData;
	
	public ServerHandler(String mUsername, String mPassword){
		contextData = new ContextData("http://api.learning-context.de/", 3, mUsername, mPassword, 23, 
				"pcawfzywxcuztk2rbpl58vnts0rzv0mq1bsk9kqk75a52wafph");
	}
	
	public ServerHandler(ContextData contextData){
		this.contextData = contextData;
	}
	
	public static ContextData createInstance(String mUsername, String mPassword){
		return new ContextData("http://api.learning-context.de/", 3, mUsername, mPassword, 23, 
				"pcawfzywxcuztk2rbpl58vnts0rzv0mq1bsk9kqk75a52wafph");
	}
	public static MyContextData createMyInstance(String mUsername, String mPassword){
		return new MyContextData("http://api.learning-context.de/", 3, mUsername, mPassword, 23, 
				"pcawfzywxcuztk2rbpl58vnts0rzv0mq1bsk9kqk75a52wafph");
	}	
	
	public void setGETListener(ContextData.Listener listener){
		contextData.registerGETListener(listener);
	}
	public void setPOSTListener(ContextData.Listener listener){
		contextData.registerPOSTListener(listener);
	}
	public static void setListener(ContextData.Listener listener){
		contextData.registerGETListener(listener);
		contextData.registerPOSTListener(listener);
	}
	
	public static void sendGETRequest(String event, String json){
		contextData.get(event, json);
	}
	public static void sendPOSTRequest(String event, String json){
		contextData.post(event, json);
	}
	
	public static ContextData getContextData(){
		return contextData;
	}
}
