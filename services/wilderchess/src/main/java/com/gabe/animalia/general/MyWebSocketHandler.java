package com.gabe.animalia.general;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class MyWebSocketHandler {
	private State state = State.getInstance();
	Session session = null;
	RemoteEndpoint endpoint = null;
	private String user = null;


    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("Close: statusCode=" + statusCode + ", reason=" + reason);
        System.out.println(this.session.getRemoteAddress());
//        for (Game g : state.games) {
//
//			for (int j = 0; j < 2; j++) {
//				System.out.println(this.session.getRemoteAddress().getAddress() + ","  + ((Session) g.getEndpoints()[j]).getRemoteAddress().getAddress());
//				if (this.session.getRemoteAddress().getAddress().equals(((Session) g.getEndpoints()[j]).getRemoteAddress().getAddress())) {
//					g.getReconnectingAddresses()[j] = session.getRemoteAddress().getAddress();
//				}
//			}
//
//        }


    }

    @OnWebSocketError
    public void onError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
    	this.session = session;
    	this.endpoint = session.getRemote();
		System.out.println("Connect: "
				+ session.getRemoteAddress());
		state.incrementCount();

	}


    @OnWebSocketMessage
	public void onMessage(String message) {
		System.out.println("Message: " + message);
		String[] tokens = message.split(",");
		String type = tokens[0];
		// state is updated with the message
		if (session != null) {
			if (session.isOpen()) {
				if (type.equals("login")) {
					user = tokens[1];
					state.update(endpoint, message);
				} else {
					System.out.println("type : " + type);
					state.update(endpoint, message);
				}
			}
		}
	}
}
