package com.rwth.i10.exercisegroups.Util;

public enum MessagesTypes {

	UPDATE_GROUPS, RECEIVE_MESSAGE, GROUP_JOIN_REQUEST, GROUP_ACCEPT_ACK, GROUP_REJECT_ACK, NEW_USER_JOINED;
	
	public static MessagesTypes convert(int type){
		switch(type){
		
		case 0: return UPDATE_GROUPS;
		case 1: return RECEIVE_MESSAGE;
		case 2: return GROUP_JOIN_REQUEST;
		case 3: return GROUP_ACCEPT_ACK;
		case 4: return GROUP_REJECT_ACK;
		case 5: return NEW_USER_JOINED;
		default: return UPDATE_GROUPS;
		
		}
	}
}
