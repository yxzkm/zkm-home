package com.zhangkm.demo.base.activemq;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

public class MsgConsumer {
	private static final String MSG_SERVER_IP = "127.0.0.1";
	private static final String MSG_SERVER_PORT = "61616";
	private static final String MSG_QUEUE_NAME = "log";
	
	private static Connection amqConn;
	private static Session sess;
	private static MessageConsumer consumer;

	private static Logger logger = Logger.getLogger(MsgConsumer.class);

	private MsgConsumer(){}

	public static void receiveMessage() throws Exception{

		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://"+MSG_SERVER_IP+":"+MSG_SERVER_PORT);
		amqConn = factory.createConnection();
		amqConn.start();
		sess = amqConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = sess.createConsumer(sess.createTopic(MSG_QUEUE_NAME));
		consumer.setMessageListener(new MyMessageListener());

		System.out.println("MsgConsumer is waiting for message...");
	}
	
	public static class MyMessageListener implements MessageListener {

		public void onMessage(Message message) {
			try {
				if(message instanceof TextMessage){
					String msgBody = ((TextMessage)message).getText();
					System.out.println(msgBody);
					logger.debug(msgBody);

					if(msgBody.equalsIgnoreCase("quit")){
						consumer.close();
						sess.close();
						amqConn.close();
						System.exit(1);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String args[]) {
		try {
			MsgConsumer.receiveMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
