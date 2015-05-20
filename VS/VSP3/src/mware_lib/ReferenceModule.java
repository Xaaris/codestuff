package mware_lib;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import accessor_two.ClassOneAT;

/**
 * 
 * @author Francis und Fabian
 * 
 */

public class ReferenceModule {

	private static Map<RemoteObjectRef, Object> mapRemoteServant = new HashMap<RemoteObjectRef, Object>();
	private static Map<RemoteObjectRef, Object> mapRemoteProxy = new HashMap<RemoteObjectRef, Object>();

	public static RemoteObjectRef createNewRemoteRef(Object myObject) {

		InetAddress inetAddress = null;
		int port = -1;
		int objectNumber = -1;
		List<String> interfaces = new ArrayList<String>();
		// if(myObject instanceof ClassOneAO){
		//
		// }else if(myObject instanceof ClassTwoAO){
		//
		// }else
		if (myObject instanceof ClassOneAT) {
			objectNumber = 3;
			interfaces.add("methodOne(String param1, double param2)");
			interfaces.add("methodTwo(String param1, double param2)");
		}

		RemoteObjectRef rawObjRef = null;
		try {
			rawObjRef = new RemoteObjectRef(
					InetAddress.getByName("localhost"), port,
					System.currentTimeMillis(), objectNumber, interfaces);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		servantToTable(rawObjRef, myObject);
		return rawObjRef;
	}

	private static void servantToTable(RemoteObjectRef rawObjRef,
			Object myObject) {
		mapRemoteServant.put(rawObjRef, myObject);
	}

	public static boolean contains(Object rawObjRef) {
		return mapRemoteProxy.containsKey(rawObjRef);
	}

	public static Object getProxy(RemoteObjectRef rawObjRef) {
		for (Entry<RemoteObjectRef, Object> item : mapRemoteProxy.entrySet()) {
			if (item.getKey().equals(rawObjRef)) {
				return item.getValue();
			}
		}
		return null;
	}

	public static void add(RemoteObjectRef rawObj, Object remoteObj) {
		mapRemoteProxy.put(rawObj, remoteObj);
	}

	public static Object getServant(RemoteObjectRef rawObjRef) {
		for (Entry<RemoteObjectRef, Object> item : mapRemoteServant.entrySet()) {
			if (item.getKey().equals(rawObjRef)) {
				return item.getValue();
			}
		}
		return null;
	}
}
