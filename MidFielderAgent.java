
/** MidFielderAgent class extends Jason Architecture class to create Midfielder Agent and perform the actions  
 * */
import jason.architecture.AgArch;
import jason.asSemantics.*;
import jason.asSyntax.*;

import java.net.InetAddress;
import java.util.*;
import java.util.logging.*;

public class MidFielderAgent extends AgArch implements Runnable {

	/* Private members */
	private String name;

	private boolean running;
	private static Logger logger = Logger.getLogger(MidFielderAgent.class.getName());

	private char playmode = '0';
	private char playerside = '0';

	private double ball_direction = 0.0;
	private double goal_direction = 0.0;
	private double ball_distance = 0.0;

	private String team = "";

	private EnvironmentAdapter player = null;
	private Brain tempBrain = null;

	/** Constructor for MidFielder agent - create a agent */

	public MidFielderAgent(String id, String team) {

		running = false;
		this.team = team;

		/* set up the Jason agent -- MidFielder Agent */
		try {

			Agent ag = new Agent();
			new TransitionSystem(ag, null, null, this);
			this.name = id;
			ag.initAg("src/asl/midfielder.asl"); // Calling midfielder.asl

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Init error", e);
		}
	}

	/** Thread run for reasoning cycle */

	public void run() {

		try {
			running = true;

			player = new EnvironmentAdapter(InetAddress.getByName("localhost"), 6000, team); // Initializing Player
			player.mainLoop();
			tempBrain = (Brain) player.m_brain; // Initializing Player Brain

			while (isRunning()) {
				// calls the Jason engine to perform one reasoning cycle
				logger.fine("MidFielder Agent " + getAgName() + " is reasoning....");
				getTS().reasoningCycle();

				if (getTS().canSleep()) {
					sleep();
				}
			}
			logger.fine("MidFielder Agent " + getAgName() + " stopped.");
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Run error", e);
		}
	}

	/** get agent id - name */
	public String getAgName() {
		return name;
	}

	/** This method adds perceptions for the agent */
	@Override
	public List<Literal> perceive() {

		// Update the history, get the list of literals to send to the agent
		List<Literal> perceptionLiterals = new ArrayList<Literal>();
		ObjectInfo ballObject, goalpostObject, lineObject, ownPlayerObject, flagt, flagb, flagt3, flagt2, flagb3,
				flagb2;

		ball_direction = 0.0;
		goal_direction = 0.0;
		ball_distance = 0.0;

		/* Time over */

		if (tempBrain.m_timeOver) {
			perceptionLiterals.add(Literal.parseLiteral("time(over)"));
		}

		/* Line object */
		lineObject = tempBrain.m_memory.getObject("line");
		if (lineObject == null) {
		} else {
			if (lineObject.m_distance <= 5.0) {
				perceptionLiterals.add(Literal.parseLiteral("visible(line)"));
				System.out.println("Line D: " + lineObject.m_distance);
			}
		}

		/* play mode */
		playmode = tempBrain.m_playMode.charAt(tempBrain.m_playMode.length() - 1);
		playerside = tempBrain.m_side;
		if (playmode != playerside && (playmode == 'l' || playmode == 'r')) {
			perceptionLiterals.add(Literal.parseLiteral("visible(defend)"));
		}

		/* ball object */
		ballObject = tempBrain.m_memory.getObject("ball");

		if (ballObject == null) {
		} else {
			perceptionLiterals.add(Literal.parseLiteral("visible(ball)"));
			if (ballObject.m_distance > 1.0) {

				if (ballObject.m_direction != 0) {
					ball_direction = ballObject.m_direction;
					perceptionLiterals.add(Literal.parseLiteral("visible(setdirection)"));
				} else {
					ball_distance = ballObject.m_distance;
					perceptionLiterals.add(Literal.parseLiteral("visible(balldirection)"));
				}
			} else {

				perceptionLiterals.add(Literal.parseLiteral("visible(ballclose)"));

				/* opponent goal object */
				if (tempBrain.m_side == 'l')
					goalpostObject = tempBrain.m_memory.getObject("goal r");
				else
					goalpostObject = tempBrain.m_memory.getObject("goal l");

				/* goal not visible */
				if (goalpostObject == null) {
					/*
					 * check flags object if goal is not visible to pass the ball to own team player
					 */
					if (tempBrain.m_side == 'l') {
						flagt = tempBrain.m_memory.getObject("flag r t");
						flagb = tempBrain.m_memory.getObject("flag r b");
						flagt3 = tempBrain.m_memory.getObject("flag r t 30");
						flagt2 = tempBrain.m_memory.getObject("flag r t 20");
						flagb3 = tempBrain.m_memory.getObject("flag r b 30");
						flagb2 = tempBrain.m_memory.getObject("flag r b 20");
					} else {
						flagt = tempBrain.m_memory.getObject("flag l t");
						flagb = tempBrain.m_memory.getObject("flag l b");
						flagt3 = tempBrain.m_memory.getObject("flag l t 30");
						flagt2 = tempBrain.m_memory.getObject("flag l t 20");
						flagb3 = tempBrain.m_memory.getObject("flag l b 30");
						flagb2 = tempBrain.m_memory.getObject("flag l b 20");
					}

					/* looking for own team player */
					if ((flagt != null) || (flagb != null) || (flagt3 != null) || (flagt2 != null) || (flagb3 != null)
							|| (flagb2 != null)) {
						ownPlayerObject = tempBrain.m_memory.getObject("player");
						if (ownPlayerObject != null) {
							goal_direction = ownPlayerObject.m_bodyDir;
							perceptionLiterals.add(Literal.parseLiteral("visible(player)"));
						}
					}

				} else { // goal visible so go for the goalpost
					goal_direction = goalpostObject.m_direction;
					perceptionLiterals.add(Literal.parseLiteral("visible(goal)"));
				}
			}
		}

		System.out.println("MidFielder Agent " + getAgName() + " Perceiving perception " + perceptionLiterals);

		return perceptionLiterals;
	}

	/**
	 * This method gets the agent actions. This is called back by the agent code
	 */
	@Override
	public void act(ActionExec action) {
		// Get the action term
		Structure actionTerm = action.getActionTerm();
		System.out.println("MidFielder Agent " + getAgName() + " is doing: " + action.getActionTerm());

		// Define terms for possible actions
		Term initialposition = Literal.parseLiteral("initialposition");
		Term findball = Literal.parseLiteral("findball");
		Term gonearball = Literal.parseLiteral("gonearball");
		Term setdirection = Literal.parseLiteral("setdirection");
		Term findgoalpost = Literal.parseLiteral("findgoalpost");
		Term kick = Literal.parseLiteral("kick");
		Term defend = Literal.parseLiteral("defend");
		Term bye = Literal.parseLiteral("bye");
		Term move = Literal.parseLiteral("move");
		Term returnback = Literal.parseLiteral("returnback");

		try {
			if (actionTerm.equals(initialposition)) { // initialposition
				logger.info("executing : " + actionTerm + " agent: " + getAgName());
				player.move(-Math.random() * 52.5, 34 - Math.random() * 68.0);

			} else if (actionTerm.equals(move)) { // move

				logger.info("executing : " + actionTerm + " agent: " + getAgName());
				player.move(Math.random() * 52.5, 34 - Math.random() * 68.0);

			} else if (actionTerm.equals(returnback)) { // return back

				logger.info("executing : " + actionTerm + " agent: " + getAgName());
				player.turn(40);
				ObjectInfo flagc = tempBrain.m_memory.getObject("flag c");
				if (flagc != null) {
					Thread.sleep(100);
					player.dash(20);
				}

			} else if (actionTerm.equals(findball)) { // findball

				logger.info("executing : " + actionTerm + " agent: " + getAgName());
				player.turn(40);

			} else if (actionTerm.equals(gonearball)) { // gonearball

				logger.info("executing: " + actionTerm + " agent: " + getAgName());
				player.dash(10 * ball_distance);

			} else if (actionTerm.equals(setdirection)) { // setdirection

				logger.info("executing: " + actionTerm + " agent: " + getAgName());
				player.turn(ball_direction);

			} else if (actionTerm.equals(findgoalpost)) { // findgoalpost

				logger.info("executing: " + actionTerm + " agent: " + getAgName());
				player.turn(40);

			} else if (actionTerm.equals(kick)) { // kick

				logger.info("executing: " + actionTerm + " agent: " + getAgName());
				player.kick(100, goal_direction);

			} else if (actionTerm.equals(defend)) { // defend

				logger.info("executing: " + actionTerm + " agent: " + getAgName());
				ObjectInfo post;
				if (tempBrain.m_side == 'l')
					post = tempBrain.m_memory.getObject("goal l");
				else
					post = tempBrain.m_memory.getObject("goal r");
				if (post == null) {
					player.turn(40);
				} else {
					player.dash(10 * post.m_distance);
				}

			} else if (actionTerm.equals(bye)) { // bye
				player.bye();
				stop();

			} else {
				logger.info("executing: " + actionTerm + ", but not implemented!");
			}
			if (!actionTerm.equals(bye)) {
				Thread.sleep(200);
			}

		} catch (Exception e) {
			perceive();
		}

		// Set that the execution was OK and flag it as complete.
		action.setResult(true);
		actionExecuted(action);
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void stop() {
		running = false;
	}

	/** agent sleep after every reasoning cycle */
	public void sleep() {
		System.out.println("Snoozing");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
	}
}