package com.rwth.i10.exercisegroups.Util;

public enum MessagesTypes {

	UPDATE_GROUPS, RECEIVE_MESSAGE, GROUP_JOIN_REQUEST, GROUP_ACCEPT_ACK, GROUP_REJECT_ACK;
	
	public static MessagesTypes convert(int type){
		if(type == 0)
			return UPDATE_GROUPS;
		else
			return RECEIVE_MESSAGE;
	}
}
