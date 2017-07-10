package com.zhangkm.demo.base.activemq;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class MsgProducer {
	private static final String MSG_SERVER_IP = "127.0.0.1";
	private static final String MSG_SERVER_PORT = "61616";
	private static final String MSG_QUEUE_NAME = "log";

	private MsgProducer(){}
	
	public static void sendMessage() throws JMSException {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://"+MSG_SERVER_IP+":"+MSG_SERVER_PORT);
			connectionFactory.setSendAcksAsync(false);
			Connection connection = connectionFactory.createConnection();
			connection.start();
			Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
			Destination destination = session.createTopic(MSG_QUEUE_NAME);
			MessageProducer producer = session.createProducer(destination);

			TextMessage message = session.createTextMessage("log-" + System.currentTimeMillis());
			message.setStringProperty("prop1", "my1");
			message.setStringProperty("prop2", "my2");

			System.out.println("Producer send msg now...");
			producer.send(message);
			
			//BytesMessage bm = session.createBytesMessage();
			//bm.writeBytes((expectedBody + "  正义网   " + new Date()).getBytes("utf-8"));
			//producer.send(bm);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			MsgProducer.sendMessage();
			System.out.println("sender over.");
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}

}
