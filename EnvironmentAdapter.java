
/**
* EnvironmentAdapter Java class communicate with the roboCup Environment
*/

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

//***************************************************************************
//
//	This is main object class
//
//***************************************************************************
class EnvironmentAdapter extends Thread implements SendCommand {

	// ---------------------------------------------------------------------------
	// This constructor opens socket for connection with server
	public EnvironmentAdapter(InetAddress host, int port, String team) throws SocketException {
		m_socket = new DatagramSocket();
		m_host = host;
		m_port = port;
		m_team = team;
		m_playing = true;
	}

	// ---------------------------------------------------------------------------
	// This destructor closes socket to server
	public void finalize() {
		m_socket.close();
	}

	// ===========================================================================
	// Protected member functions

	// ---------------------------------------------------------------------------
	// This is main loop for player
	protected void mainLoop() throws IOException {

		byte[] buffer = new byte[MSG_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE);

		// first we need to initialize connection with server
		init();

		m_socket.receive(packet);
		parseInitCommand(new String(buffer));
		m_port = packet.getPort();
		start();
	}

	/* Thread running to perceive from environment */

	public void run() {
		try {

			while (m_playing) {
				parseSensorInformation(receive());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finalize();
	}
	// ===========================================================================
	// Implementation of SendCommand Interface

	// ---------------------------------------------------------------------------
	// This function sends move command to the server
	public void move(double x, double y) {
		send("(move " + Double.toString(x) + " " + Double.toString(y) + ")");
	}

	// ---------------------------------------------------------------------------
	// This function sends turn command to the server
	public void turn(double moment) {
		System.out.println("Turn");
		send("(turn " + Double.toString(moment) + ")");
	}

	public void turn_neck(double moment) {
		send("(turn_neck " + Double.toString(moment) + ")");
	}

	// ---------------------------------------------------------------------------
	// This function sends dash command to the server
	public void dash(double power) {
		System.out.println("Dash");
		send("(dash " + Double.toString(power) + ")");
	}

	// ---------------------------------------------------------------------------
	// This function sends kick command to the server
	public void kick(double power, double direction) {
		send("(kick " + Double.toString(power) + " " + Double.toString(direction) + ")");
	}

	// ---------------------------------------------------------------------------
	// This function sends say command to the server
	public void say(String message) {
		send("(say " + message + ")");
	}

	// ---------------------------------------------------------------------------
	// This function sends chage_view command to the server
	public void changeView(String angle, String quality) {
		send("(change_view " + angle + " " + quality + ")");
	}

	// ---------------------------------------------------------------------------
	// This function sends bye command to the server
	public void bye() {
		m_playing = false;
		send("(bye)");
	}

	// ---------------------------------------------------------------------------
	// This function parses initial message from the server
	protected void parseInitCommand(String message) throws IOException {
		Matcher m = Pattern.compile("^\\(init\\s(\\w)\\s(\\d{1,2})\\s(\\w+?)\\).*$").matcher(message);
		if (!m.matches()) {
			throw new IOException(message);
		}

		// initialize player's brain
		m_brain = new Brain(this, m_team, m.group(1).charAt(0), Integer.parseInt(m.group(2)), m.group(3));
	}

	// ===========================================================================
	// Here comes collection of communication function
	// ---------------------------------------------------------------------------
	// This function sends initialization command to the server
	private void init() {
		send("(init " + m_team + ")");
	}

	// ---------------------------------------------------------------------------
	// This function parses sensor information
	public void parseSensorInformation(String message) throws IOException {
		// First check kind of information
		Matcher m = message_pattern.matcher(message);
		// System.out.println(message);
		if (!m.matches()) {
			throw new IOException(message);
		}
		if (m.group(1).compareTo("see") == 0) {
			VisualInfo info = new VisualInfo(message);
			tempV = info;
			info.parse();
			m_brain.see(info);
		} else if (m.group(1).compareTo("hear") == 0)
			parseHear(message);
	}

	// ---------------------------------------------------------------------------
	// This function parses hear information
	private void parseHear(String message) throws IOException {
		// get hear information
		Matcher m = hear_pattern.matcher(message);
		int time;
		String sender;
		String uttered;
		if (!m.matches()) {
			throw new IOException(message);
		}
		time = Integer.parseInt(m.group(1));
		sender = m.group(2);
		uttered = m.group(3);
		if (sender.compareTo("referee") == 0)
			m_brain.hear(time, uttered);
		// else if( coach_pattern.matcher(sender).find())
		// m_brain.hear(time,sender,uttered);
		else if (sender.compareTo("self") != 0)
			m_brain.hear(time, Integer.parseInt(sender), uttered);
	}

	// ---------------------------------------------------------------------------
	// This function sends via socket message to the server
	private void send(String message) {
		byte[] buffer = Arrays.copyOf(message.getBytes(), MSG_SIZE);
		try {
			DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE, m_host, m_port);
			m_socket.send(packet);
		} catch (IOException e) {
			System.err.println("socket sending error " + e);
		}

	}

	// ---------------------------------------------------------------------------

	// This function waits for new message from server
	public String receive() {
		byte[] buffer = new byte[MSG_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, MSG_SIZE);
		try {
			m_socket.receive(packet);
		} catch (SocketException e) {
			System.out.println("shutting down...");
		} catch (IOException e) {
			System.err.println("socket receiving error " + e);
		}
		return new String(buffer);
	}

	// ===========================================================================
	// Private members
	// class members
	private DatagramSocket m_socket; // Socket to communicate with server
	private InetAddress m_host; // Server address
	private int m_port; // server port
	private String m_team; // team name
	public SensorInput m_brain; // input for sensor information
	private boolean m_playing; // controls the MainLoop
	private Pattern message_pattern = Pattern.compile("^\\((\\w+?)\\s.*");
	private Pattern hear_pattern = Pattern.compile("^\\(hear\\s(\\w+?)\\s(\\w+?)\\s(.*)\\).*");
	// private Pattern coach_pattern = Pattern.compile("coach");
	// constants
	private static final int MSG_SIZE = 4096; // Size of socket buffer
	public VisualInfo tempV;
}
