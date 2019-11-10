//
//	File:			Brain.java

class Brain implements SensorInput {
	// ---------------------------------------------------------------------------
	// This constructor:
	// - stores connection to agents
	public Brain(SendCommand agent, String team, char side, int number, String playMode) {
		m_timeOver = false;
		m_krislet = agent;
		m_memory = new Memory();
		m_side = side;
		m_playMode = playMode;
	}

	// ===========================================================================
	// Here are supporting functions for implement logic

	// ===========================================================================
	// Implementation of SensorInput Interface

	// ---------------------------------------------------------------------------
	// This function sends see information
	public void see(VisualInfo info) {
		m_memory.store(info);
	}

	// ---------------------------------------------------------------------------
	// This function receives hear information from player
	public void hear(int time, int direction, String message) {
	}

	// ---------------------------------------------------------------------------
	// This function receives hear information from referee
	// Change Play mode depending on the message
	public void hear(int time, String message) {
		if (message.compareTo("time_over") == 0) {
			m_timeOver = true;
		} 
		/* Storing play mode */
		else if (message.compareTo("goal_l") == 0 || message.compareTo("goal_r") == 0
				|| message.compareTo("goal_kick_l") == 0 || message.compareTo("goal_kick_r") == 0
				|| message.compareTo("free_kick_l") == 0 || message.compareTo("free_kick_r") == 0
				|| message.compareTo("kick_off_l") == 0 || message.compareTo("kick_off_r") == 0
				|| message.compareTo("play_on") == 0) {
			this.m_playMode = message;
		}
	}

	// ===========================================================================
	// Private members
	public SendCommand m_krislet; // robot which is controlled by this brain
	public Memory m_memory; // place where all information is stored
	public char m_side;
	volatile public boolean m_timeOver;
	public String m_playMode;

}
