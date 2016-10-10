

/*
 * The mscan class in the main class in the mscan package, 
 * to use simply place an mp3 file in the "data" folder and 
 * run the program. When prompted enter the name of the 
 * mp3 file (without extensions) you wish to convert into 
 * a light show. 
 * 
 * The program will take the mp3 file create an a FFT 
 * (Fast Fourier Transform) instance to perform  
 * frequency analysis on the mp3 file which results in 
 * a frequency spectrum the average amplitudes are taken 
 * for the frequency bands. 
 * 
 * These frequency bands are divided into 4 
 * (one for each light) in this case covering the 
 * top 16 most used frequencies as most frequencies
 * are rarely used by most songs.
 *
 *
 * The program will then go through the list of average
 * amplitudes for the frequency bands and compares them 
 * to the average amplitude of the frequency bands.
 * If the amplitude is higher than average the frequency
 * band counts as a "hit" and assigned a "1" on switch if not then it
 * is assigned "0" off switch.
 * 
 * A the switches are sorted into two lists (one for each mbed)
 * and are sent via the MqttPublishSample class to their separate
 * topics. 
 *  
 * Once sent the program will use the SimpleMqttClient class to 
 * listen for the last mbed (mbed 2 in this case) to finish.
 * 	
 * Once a finish has been received the program will send a message
 * to mbed one to start playing and play the mp3 file with the Audio
 * player. 
 * 
 * 
 * This class makes use of the minim library
 * found here
 * https://github.com/ddf/Minim
 * 
 * and also uses proclipsing for using processing software in eclipse
 * https://code.google.com/archive/p/proclipsing/
 * 
 * These allow for the audio analysis and player functions.
 * 
 * Some "System.out.println" lines have been commented out as 
 * they were used for diagnostic purposes. 
 */

package mscan;

import ddf.minim.*;
import ddf.minim.analysis.FFT;
import ddf.minim.spi.AudioRecordingStream;
import processing.core.PApplet;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class Mscan extends PApplet {


static Minim minim;//the minim object is used to deal with audio files	
ArrayList<ArrayList<Integer>> avg_freq;
static AudioPlayer player;//came with the minim library
	
 
 //a constructor with proclipsing
	public void setup() {
	
		minim = new Minim(this);
		 
		avg_freq = new ArrayList<ArrayList<Integer>>();
		
		//the main method used.
		postList();
		
	}
/*
 * This class handles the frequency analysis and
 * returns an  ArrayList of amplitude averages for
 * the spectrum bands.
 */
	public ArrayList<ArrayList<Integer>> analyzeUsingAudioRecordingStream(String name)
	{
	  int fftSize = 1024;
	 
	  //creates a stream for offline(non real time) audio analysis.
	  AudioRecordingStream stream = minim.loadFileStream(name, fftSize, true);
	  
	  player = minim.loadFile(name);
		
	  
	  stream.play();
	  
	  
	  // create the fft  for the analysis
	  FFT fft = new FFT( fftSize, stream.getFormat().getSampleRate() );
	  
	  // create the buffer for reading from the stream
	  MultiChannelBuffer buffer = new MultiChannelBuffer(fftSize, stream.getFormat().getChannels());
	  
	  
	  int totalSamples = (int) ( (stream.getMillisecondLength() / 1000) * stream.getFormat().getSampleRate() );
	  
	  // now analyze the samples per segment
	  int totalSeg = (totalSamples / fftSize) + 1;
	  
	  

	  
	 
	  for(int segIdx = 0; segIdx < totalSeg; ++segIdx)
	  {
		  // a list of frequency bands for each segment
		  ArrayList<Integer> seg = new ArrayList<Integer>();
	    
		  //gets the what's in the buffer of the stream
		  stream.read( buffer );
		  fft.forward( buffer.getChannel(0) );
	    
		  		//Separates the the frequency bands
		  		int start = 0;
		  		int finish = 4;
		  		for (int i = 0; i < 4; ++i){
		  				//gets the amplitudes for each frequency and totals them
		  				float total_a = 0;
		  				for(int x = start; x < finish; ++x)
		  				{
	  
		  					float f = fft.getBand(x); 	
		  					total_a = total_a + f;
	   
		  				}
		  //works out average		
	    int avg_a = (int) (total_a / 4);
	    
	    seg.add(avg_a);
	    //System.out.println("average amplitude of frequency  " + start + " to " + finish + " is: " + avg_a);
	    
	    
	    //goes to the next frequency band. 
	    start = start + 4;
	    finish = finish + 4;
	    }
		  	avg_freq.add(seg);
	  }
	  return avg_freq;
	}
	
	/*
	 * The main method of this class that calls most other methods in 
	 * the program
	 * 
	 * first it asks for the name of the song
	 * then it analyze the song
	 * it then wraps the frequency bands into a strings of switches
	 * finally it will send the string to the mbeds and set up a SimpleMqttClient
	 * to listen for a response.
	 */
	public void postList(){
		//asks for song
		Scanner user_input = new Scanner( System.in );
		System.out.println("Name of mp3 file you wish to process that is in the data folder?");
		String name = user_input.next( ) + ".mp3";
		user_input.close();
		
		
		try{
		//Performs analysis on song
		ArrayList<ArrayList<Integer>> freq = analyzeUsingAudioRecordingStream(name);
		//System.out.println("array's made");
		
		//gets the average for all frequency bands
		int avg = findAvg(freq);
		
		//wraps them for the switches/identifiers to be sent returning with a string of switches for each mbed
		ArrayList<String> send = wrap(freq, avg);
		
		String mbed1 = send.get(0);
		String mbed2 = send.get(1);
		
		//System.out.println("array's wrapped");
		
		String mbed1Topic = "rt360/assessment5/Mlights/mbed1";
		String mbed2Topic = "rt360/assessment5/Mlights/mbed2";
		
		//makes the Mqtt connection
		MqttPublishSample mps = new MqttPublishSample();
		mps.connect(name);
		System.out.println("connected");
		
		
		mps.post(mbed1Topic,mbed1);
	
		mps.post(mbed2Topic,mbed2);
		
		
		
		mps.dissconnect();
		
		//sets up the SimpleMqttClient to listen for mbed2's responce
		SimpleMqttClient smc2 = new SimpleMqttClient(name);
		smc2.runClient();
		
		//int l = freq.size(); 
		//System.out.println(l);
		}
	catch(NullPointerException e){
		System.out.println("error: no songs matching in folder");
	}
	
	
		
	}
	

	
	/*
	 *  This method will take a list of frequency bands and the average
	 *  amplitude of all bands and return a list of
	 *  Strings with switches and identifiers for each mbed
	 *  currently set up for two mbeds
	 */
	public ArrayList<String> wrap(ArrayList<ArrayList<Integer>> freq, int avg){

	
	
	ArrayList<String> send = new ArrayList<String>();
	
	String mbed1 ="";
	String mbed2 = "";
	
	int set = 0;
	String seg = "";
	
	for(ArrayList<Integer> i : freq){
		
		for(int h : i){
			//uses the average of frequency band amplitude to work out a hit
			int c = 0;
				if(h > avg){
					c = 1;
				}	
				//ads the switch to the string
				seg = seg + c;
				set++;
				
				//since the mbed has two leds two switches are needed
				if (set == 2){
					//adds to the string for the mbed and a "*" identifier to tell the mbed identify segments
					mbed1 = mbed1 + seg + "*"; 
					
					seg = "";
				}
				//the next two switches for the next mbed
				else if(set == 4) {
					//adds to the string for the mbed and a "*" identifier to tell the mbed identify segments
					mbed2 = mbed2 + seg + "*"; 
					
					seg = "";
					set = 0;
				}
				
		}
		
	}
	
		//adds and end "!" identifier for the songs for each mbed.
		mbed1 = mbed1 + "!";
		mbed2 = mbed2 + "!";
		
		send.add(mbed1);
		send.add(mbed2);
		
		return send;
		
		
	}
	
	
	/*
	 * finds the average amplitude for all of the spectrum bands
	 */
	public int findAvg(ArrayList<ArrayList<Integer>> freq){
		int total_avg = 0;
		int size = freq.size() * 5;
		
		for (ArrayList<Integer> i : freq){
			for (int x : i){
				total_avg = total_avg + x;
			}
		}
		
		int avg_avg = total_avg / size;
		
		return avg_avg;
		
	}
	
	/*
	 * when called by the SimpleMqttClient this method should play
	 * the mp3 file
	 */
	public static void play(String name){
		
		//gets the stream length in milliseconds
		int fftSize = 1024; 
		AudioRecordingStream stream = minim.loadFileStream(name, fftSize, true);
		int time = stream.getMillisecondLength();
		
		//tells mbed1 to play (mbed2 should be playing already)
		MqttPublishSample mps = new MqttPublishSample();
		String Cname = "laptop play";
		
		mps.connect(Cname);
		String mbed1Topic = "rt360/assessment5/Mlights/mbed1";
		mps.post(mbed1Topic,".");
		mps.dissconnect();
		
		//plays the audio file for the amount of time needed
		player.play();
		try{
			TimeUnit.MILLISECONDS.sleep(time);
		}
		 catch(InterruptedException t) {
			    System.out.println("Song play time error");
			}
		
	}
	
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { mscan.Mscan.class.getName() });
	}
	
	
	
}