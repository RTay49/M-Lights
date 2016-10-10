/*
 * taken from "seminar week 11" this class posts Mqtt messages
 * however it has been split up in to different methods to allow 
 * the Mscan more control over it and also allows arguments to be past
 */
package mscan;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;




public class MqttPublishSample {

MqttClient sampleClient; 
MqttConnectOptions connOpts ;

/*
 * connect with a unique name
 */
    public void connect(String name){
    
    	String broker       = "tcp://doughnut.kent.ac.uk:1883";
        String clientId     = "Mscan" + name;
        MemoryPersistence persistence = new MemoryPersistence();
    	
    try{	
    sampleClient = new MqttClient(broker, clientId, persistence);
    connOpts = new MqttConnectOptions();
    connOpts.setCleanSession(true);
    System.out.println("Connecting to broker: "+broker);
    sampleClient.connect(connOpts);
    System.out.println("Connected");
    }
    catch(MqttException me) {
        System.out.println("reason "+me.getReasonCode());
        System.out.println("msg "+me.getMessage());
        System.out.println("loc "+me.getLocalizedMessage());
        System.out.println("cause "+me.getCause());
        System.out.println("excep "+me);
        me.printStackTrace();
    }
   }
    /*
     * send a message taking in the message and topic 
     * as arguments
     */

    public  void post(String totopic, String messagetosend) {

        String topic        = totopic;
        String content  	= messagetosend;
        int qos             = 2;
      

        try {
            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
    /*
     * Disconnects
     */
    public void dissconnect(){
    
    	try{	
    		sampleClient.disconnect();
    		System.out.println("Disconnected");
    		System.exit(0);
    	} catch(MqttException me) {
    		System.out.println("reason "+me.getReasonCode());
    		System.out.println("msg "+me.getMessage());
    		System.out.println("loc "+me.getLocalizedMessage());
    		System.out.println("cause "+me.getCause());
    		System.out.println("excep "+me);
    		me.printStackTrace();
    	}
    }
            
}