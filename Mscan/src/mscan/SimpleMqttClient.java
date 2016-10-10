/*
 * taken from "seminar week 11" this class listens Mqtt messages
 * the only change is it listens out for the word ready on its topic '
 * and uses the mscan.play() method when it recives the ready message from
 * mbed 2 
 */

package mscan;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class SimpleMqttClient implements MqttCallback {

  MqttClient myClient;
	MqttConnectOptions connOpt;
	static final String BROKER_URL = "tcp://doughnut.kent.ac.uk:1883";
	static String name;

	public SimpleMqttClient(String name) {
		this.name = name;
	}

	/**
	 * 
	 * connectionLost
	 * This callback is invoked upon losing the MQTT connection.
	 * 
	 */
	@Override
	public void connectionLost(Throwable t) {
		System.out.println("Connection lost!");
		// code to reconnect to the broker would go here if desired
	}

	/**
	 * 
	 * deliveryComplete
	 * This callback is invoked when a message published by this client
	 * is successfully received by the broker.
         * note needed for this example but needed to 
         * over-ride the superclas
	 * 
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
	  try {
	    System.out.println("Pub complete"
			     + new String(token.getMessage().getPayload()));
	  }
	  catch (MqttException e) {
	    System.err.println("Mqtt error in deliveryComplete");
	  }
	}

	/**
	 * 
	 * messageArrived
	 * This callback is invoked when a message is received on a 
	 * subscribed topic.
	 * 
	 */
	@Override
	public void messageArrived(String topic, MqttMessage message)
	  {
		String rMessage = new String(message.getPayload());
		System.out.println("-------------------------------------------------");
		System.out.println("| Topic:" + topic);
		System.out.println("| Message: " + rMessage);
		System.out.println("-------------------------------------------------");
		//checks if the message is "ready" from mbed 2
		if(rMessage == "ready"){
			//uses the Mscan.play() for the response.
			Mscan.play(name);
		}
	  }

	/**
	 * 
	 * MAIN
	 * 
	 */
	public static void main(String[] args) {
		SimpleMqttClient smc = new SimpleMqttClient(name);
		smc.runClient();
	}
	
	/**
	 * 
	 * runClient
	 * The main functionality of this simple example.
	 * Create a MQTT client, connect to broker, subscribe,
	 * print messages, disconnect.
	 * 
	 */
	public void runClient() {
		// setup MQTT Client
		String clientID = "Richard laptop";
		connOpt = new MqttConnectOptions();
		
		connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(30);
		
		// Connect to Broker
		try {
			myClient = new MqttClient(BROKER_URL, clientID);
			myClient.setCallback(this);
			myClient.connect(connOpt);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		System.out.println("Connected to " + BROKER_URL);

		// setup topic
		String myTopic = "rt360/assessment5/Mlights/mbed2";
		MqttTopic topic = myClient.getTopic(myTopic);

		// subscribe to topic 
		try {
		  int subQoS = 0;
		  myClient.subscribe(myTopic, subQoS);
		} catch (Exception e) {
		  e.printStackTrace();
		}
		
		// disconnect
		try {
		  // wait for a while, receiving subscribed messages
		  Thread.sleep(50000);
		  myClient.disconnect();
		} catch (Exception e) {
		  e.printStackTrace();
		}
	}



}
