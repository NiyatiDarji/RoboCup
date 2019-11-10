/**
* RoboCupEnvironment Java class is the Environment which creates and initialized 5 agents in the roboCup Environment
*/

import jason.environment.*;

public class RoboCupEnvironment extends Environment {

	/** Called before the MAS execution with the args informed in .mas2j */

	@Override

	public void init(String[] args) {
		/*creating five agents and placing those in roboCup environment*/
		try {
			Thread player1 = new Thread(new DefenderAgent("player 1", args[0]));    // Defender Agent
			player1.start();
			Thread player2 = new Thread(new ForwarderAgent("player 2", args[0]));    // Forwarder Agent
			player2.start();
			Thread player3 = new Thread(new ForwarderAgent("player 3", args[0]));    // Forwarder Agent
			player3.start();
			Thread player4 = new Thread(new MidFielderAgent("player 4", args[0]));    // MidFielder Agent
			player4.start();
			Thread player5 = new Thread(new MidFielderAgent("player 5", args[0]));    // MidFielder Agent
			player5.start();

		} catch (Exception e) {
		}
	}
	/** Called before the end of MAS execution */

	@Override

	public void stop() {

		super.stop();

	}
}