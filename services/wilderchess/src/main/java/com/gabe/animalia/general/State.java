package com.gabe.animalia.general;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;

import com.gabe.animalia.ability.Move;
import com.gabe.animalia.critter.Bat;
import com.gabe.animalia.critter.Bull;
import com.gabe.animalia.critter.Donkey;
import com.gabe.animalia.critter.Dove;
import com.gabe.animalia.critter.Hawk;
import com.gabe.animalia.critter.Fox;
import com.gabe.animalia.critter.Heron;
import com.gabe.animalia.critter.Lion;
import com.gabe.animalia.critter.Newt;
import com.gabe.animalia.critter.Pig;
import com.gabe.animalia.critter.Turtle;
import com.gabe.animalia.critter.Wolf;
import com.gabe.animalia.items.CrimberryPack;
import com.gabe.animalia.items.WarHorn;
import com.gabe.animalia.ml.server.GameLogger;
import com.gabe.animalia.items.OilBomb;
import com.gabe.animalia.items.RatlandJestersHat;
import com.gabe.animalia.items.ThiefGloves;
import com.gabe.animalia.items.SmokeBomb;

public class State {
	private static State anInstance = null;
	int callCount = 0;
	int turnCount = 0;
	int totalGameCount = 0;
	int numBotGamesAtOnce = 4;
	static Critter selectDove = new Dove("Dove", new Square("fake", ""), null, null, null);
	static Critter selectFox = new Fox("Fox", new Square("fake", ""), null, null, null);
	static Critter selectLion = new Lion("Lion", new Square("fake", ""), null, null, null);
	static Critter selectDonkey = new Donkey("Donkey", new Square("fake", ""), null, null, null);
	static Critter selectWolf = new Wolf("Wolf", new Square("fake", ""), null, null, null);
	static Critter selectTurtle = new Turtle("Turtle", new Square("fake", ""), null, null, null);
	static Critter selectBat = new Bat("Bat", new Square("fake", ""), null, null, null);
	static Critter selectBull = new Bull("Bull", new Square("fake", ""), null, null, null);
	static Critter selectHeron = new Heron("Heron", new Square("fake", ""), null, null, null);
	static Critter selectHawk = new Hawk("Hawk", new Square("fake", ""), null, null, null);
	static Critter selectNewt = new Newt("Newt", new Square("fake", ""), null, null, null);
	static Critter selectPig = new Pig("Pig", new Square("fake", ""), null, null, null);
	static Critter[] selectCritters = { selectDove, selectFox, selectLion,
			selectDonkey, selectWolf, selectTurtle, selectBat, selectBull,
			selectHeron, selectHawk, selectNewt, selectPig };
	static Item selectCrimberryPack = new CrimberryPack(null, null);
	static Item selectWarHorn = new WarHorn(null, null);
	static Item selectOilBomb = new OilBomb(null, null);
	static Item selectRatlandJestersHat = new RatlandJestersHat(null,
			null);
	static Item selectThiefGloves = new ThiefGloves(null, null);
	static Item selectSmokeBomb = new SmokeBomb(null, null);
	static Item[] selectItems = { selectCrimberryPack, selectWarHorn,
			selectOilBomb, selectRatlandJestersHat,
			selectThiefGloves, selectSmokeBomb };

	ArrayList<User> searchQueue = new ArrayList<User>();
	ArrayList<Game> games = new ArrayList<Game>();
	GameLogger gameLogger = new GameLogger();

	private State() {

	}

	public synchronized void update(RemoteEndpoint endpoint, String message) {
		System.out.println("~~~~update message:" + message + "~~~~~");

		String tokens[] = message.split(",");
		if (tokens[0].equals("selectinitrequest")) {
			selectInit(endpoint);
		} else if (tokens[0].equals("entercritterpit")) {
			String str = "";
			if (tokens[1].equals("possum")) {
				str = "entercritterpit,success";
			} else {
				str = "entercritterpit,fail";
			}
			try {
				endpoint.sendString(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (tokens[0].equals("playbot")) {
			User a = new User(endpoint, totalGameCount * 2 + 1);
			User b = new User(null, -1);
			b.setDifficulty(tokens[1]);
			Game game = new Game(a, b, gameLogger);
			try {
				endpoint.sendString("setid," + (totalGameCount * 2 + 1));
				endpoint.sendString("startgame");
			} catch (IOException e) {
				e.printStackTrace();
			}
			game.init();
			games.add(game);
			totalGameCount++;

		} else if (tokens[0].equals("searchqueue")) {
			if (tokens[1].equals("add")) {
				if (!searchQueue.isEmpty()) {
					Game game = new Game(searchQueue.get(0), new User(endpoint,
							totalGameCount * 2 + 2), gameLogger);

					try {
						endpoint.sendString("setid," + (totalGameCount * 2 + 2));
						endpoint.sendString("startgame");
						searchQueue.get(0).getEndpoint()
								.sendString("startgame");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					game.init();
					games.add(game);
					totalGameCount++;
					searchQueue.remove(0);
				} else {
					searchQueue.add(new User(endpoint, totalGameCount * 2 + 1));
					try {
						endpoint.sendString("setid," + (totalGameCount * 2 + 1));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if (tokens[1].equals("remove")) {
				for (int i = 0; i < searchQueue.size(); i++) {
					if (searchQueue.get(i).getEndpoint().equals(endpoint)) {
						searchQueue.remove(i);
					}
				}
			}
		} else if (tokens[0].equals("user")) {
			if (tokens[1].equals("disconnect")) {
				for (int i = 0; i < searchQueue.size(); i++) {
					if (searchQueue.get(i).getEndpoint().equals(endpoint)) {
						searchQueue.remove(i);
					}
				}
			}
		} else if (tokens[0].equals("bugreport")) {
			sendBugReport(tokens[1], tokens[2]);
		} else {

			for (int i = 0; i < games.size(); i++) {
				for (int j = 0; j < 2; j++) {
					if (Integer.parseInt(tokens[tokens.length - 1]) == games
							.get(i).getUsers()[j].getID()) {
						if (games.get(i).getUsers()[j].getPlayer() != null) {
							games.get(i).getUsers()[j].getPlayer().setEndpoint(
									endpoint);
						}
						games.get(i).update(endpoint, message);
					}
				}
			}
		}

	}

	public synchronized boolean sendBugReport(String name, String message) {
		if (!name.equals("") && !message.equals("")) {
			final String username = "critterpitbugreporter@gmail.com";
			final String password = "1a7q4zx93";

			Properties props = new Properties();
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");

			Session session = Session.getInstance(props,
					new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(username,
									password);
						}
					});

			try {
				Message msg = new MimeMessage(session);

				msg.setFrom(new InternetAddress(username));
				msg.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse("gabriel.jsh@gmail.com"));
				msg.setSubject("Critter Pit Bug Report");
				msg.setText(name + "\n\n" + message);

				Transport.send(msg);

				System.out.println("Done");
			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}

			return true;
		}
		System.out.println("bug report not valid");
		return false;
	}

	public static synchronized Critter identifyCritter(String name) {

		for (Critter f : selectCritters) {
			if (name.toLowerCase().equals(f.getName().toLowerCase())) {
				return f;
			}
		}

		return null;
	}

	public static synchronized Item identifyItem(String name) {

		for (Item i : selectItems) {
			if (name.toLowerCase().equals(i.getName().toLowerCase())) {
				return i;
			}
		}

		return null;
	}

	public static synchronized State getInstance() {
		if (anInstance == null) {
			anInstance = new State();
		}
		return anInstance;
	}

	public synchronized void incrementCount() {
		callCount++;
	}

	public synchronized int getTurnCount() {
		return turnCount;
	}

	public synchronized int getCount() {
		return callCount;
	}

	public synchronized void selectInit(RemoteEndpoint endpoint) {
		String str = "";
		for (Critter c : selectCritters) {
			if (c.isComingSoon()) {
				str += "|" + c.getName() + ",comingsoon";
			} else {
				str += "|" + c.getName();
				for (Action a : c.getAbilities()) {
					String typeString = (a.getType() != null) ? a.getType().name().toLowerCase() : "none";

					str += "," + typeString + "," + a.getInfo();
				}
				str += "," + c.getPassiveSelectDescription();
			}

		}
		str += "~";
		for (Item i : selectItems) {
			str += i.getName() + "," + i.getInfo() + "|";
		}
		try {
			// System.out.println(str);
			endpoint.sendString("selectinit," + str);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
