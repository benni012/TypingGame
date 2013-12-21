import org.newdawn.slick.*;
import java.io.File;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import java.util.Random;
import java.io.PrintWriter;

class TypingGame extends BasicGame{
	String[] words;
	String[] text;
	String[] highscoreNames = new String[10];
	int[] highscoreScores = new int[10];
	int word;
	int timeLeft;
	int time;

	private static final String HIGHSCOREFILE = "highscores";
	private static final int WORDCOUNT = 500;
	private static final int TIME = 60000;

	String name = "";
	int score;

	private static final byte STATE_GAME = 0;
	private static final byte STATE_WPM = 1;
	private static final byte STATE_NAME = 2;
	private static final byte STATE_HIGHSCORE = 3;
	byte state = 0;

	boolean useRandom = false;

	int cpm;
	int wpm;

	Color primary = Color.black;
	Color secondary = Color.gray;
	Color background = Color.white;
	Color error = Color.red;

	String curWord;

	Pattern p;

	boolean mayType = true;

	boolean started = false;

	java.awt.Font tFont1 = new java.awt.Font("Consolas", java.awt.Font.PLAIN, 50);
	TrueTypeFont font1;

	java.awt.Font tFont2 = new java.awt.Font("Consolas", java.awt.Font.PLAIN, 60);
	TrueTypeFont font2;

	public TypingGame(){
		super("TypingGame");
	}

	public static void main(String[] args) {
		//Init
		System.setProperty("java.library.path", "lib");
		System.setProperty("org.lwjgl.librarypath", new File("lib/natives").getAbsolutePath());
		try{
			AppGameContainer app = new AppGameContainer(new TypingGame());
			app.setDisplayMode(1024, 768, false);
			app.start();
		}catch(SlickException e){
			e.printStackTrace();
		}
	}

	@Override
	public void init(GameContainer container) throws SlickException{
		//Get that annoying FPS sign away...
		container.setShowFPS(false);
		
		if (new File("random").exists()) {
			useRandom = true;
		}

		if(useRandom){
			Random r = new Random();
			try{
				words = readFile("words", ";");
			}catch (Exception e) {
				e.printStackTrace();
			}

			text = new String[WORDCOUNT];

			for (int i = 0; i < WORDCOUNT; i++) {
				int ra = r.nextInt(words.length);
				//System.out.println(ra);
				text[i] = words[ra];
			}
		}else{
			//Read the file
			try{
				text = readFile("text", " ");
			}catch(IOException e){
				e.printStackTrace();
			}
		}

		//Declaration
		curWord = "";
		font1 = new TrueTypeFont(tFont1, true);
		font2 = new TrueTypeFont(tFont2, true);
		word = 0;
		time = 0;
		timeLeft = TIME - time;
		p = Pattern.compile("^[a-zA-Z0-9,.:!?;\"-]$");
	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException{
		//Background
		g.setColor(background);
		g.fillRect(0, 0, 1024, 768);
		
		//Check if the game is over
		if(state == STATE_GAME){
			int tmp = 0;

			//Draw the left time and the word the user has to currently type
			g.setFont(font1);
			g.setColor(primary);
			g.drawString("" + (Math.round(timeLeft / 1000)), 475f, 600f);
			g.drawString(text[word], 475f, 200f);

			if(!(text[word].startsWith(curWord))){
				g.setColor(error);
			}

			//Draw the user input
			g.drawString(curWord, 475f, 400f);

			//Draw the next few words
			g.setColor(secondary);

			if (text.length >= word + 2){
				tmp += text[word].length() * 30;
				g.drawString(text[word + 1], 475f + tmp, 200f);
			}

			if (text.length >= word + 3) {
				tmp += text[word + 1].length() * 30;
				g.drawString(text[word + 2], 475f + tmp, 200f);
			}

			if (text.length >= word + 4) {
				tmp += text[word + 2].length() * 30;
				g.drawString(text[word + 3], 475f + tmp, 200f);
			}
		}else if(state == STATE_WPM) {
			//If the game is over then draw the wpm
			g.setFont(font2);
			g.setColor(primary);
			g.drawString(wpm + " WPM", 300f, 300f);
		}else if(state == STATE_NAME) {
			g.setFont(font1);
			g.setColor(primary);
			g.drawString("Name:", 400f, 200f);
			g.drawString(name, 400f, 300f);
		}else if (state == STATE_HIGHSCORE) {
			g.setFont(font1);
			g.setColor(primary);
			for (int i = 0; i < 10; i++) {
				if (highscoreScores[i] == 0 && highscoreNames[i].equals("null")) {
					g.drawString("-", 100f, 50f * i);
					g.drawString("-", 500f, 50f * i);
				}else {
					g.drawString("" + highscoreNames[i], 100f, 50f * i);
					g.drawString("" + highscoreScores[i], 500f, 50f * i);	
				}
				g.drawString("" + (i + 1), 20f, 50f * i);
			}
		}

	}

	@Override
	public void update(GameContainer container, int delta) throws SlickException{
		if(started){
			if(state == STATE_GAME){
				time += delta;
				timeLeft = (TIME - time >= 0 ? TIME - time : 0);
				if(time >= TIME){
					end(time);
				}
			}
		}
	}

	public void keyPressed(int key, char c){
		// Backspace = 14
		// Space = 57
		if(state == STATE_GAME){
			if(mayType){
				if (p.matcher("" + c).find()) {
					curWord += c;
				}else if(key == 14){
					if(curWord.length() != 0){
						curWord = curWord.substring(0, curWord.length() - 1);
					}
				}else if (key == 57) {
					if(text.length >= word + 2){
						if(curWord.equals(text[word])){
							word++;
							curWord = "";
						}
					}else{
						end(time);
					}
				}
			}
		}else if (state == STATE_WPM) {
			if(key == 57){
				state++;
			}
		}else if(state == STATE_NAME) {
			if (p.matcher("" + c).find()) {
					name += c;
				}else if(key == 14){
					if(name.length() != 0){
						name = name.substring(0, name.length() - 1);
					}
				}else if (key == 57 && !name.equals("")) {
					score = wpm;
					try{
						loadHighscore();
						addHighscore();
						saveHighscore();
					}catch (Exception e) {
						e.printStackTrace();
					}
					state++;
				}
		}

		started = true;
	}

	String[] readFile(String filename, String spr) throws IOException {
		//Get a Path for the filename
		Path path = Paths.get(filename);
		int i = 0;

		String s = "";

		//Read the file line for line
		try (Scanner scanner =  new Scanner(path)){
			while (scanner.hasNextLine()){
				s += scanner.nextLine();
			}
		}

		//Split the words
		return s.split(spr);
	}

	void end(int time){
		//word++;
		mayType = false;
		state++;
		calculateWPM(time);
	}

	void calculateCPM(){
		for (int i = 0; i < word; i++) {
			cpm += text[i].length();
		}
	}

	void calculateWPM(int time){
		calculateCPM();
		wpm = Math.round(cpm/5 * ((TIME / 1000) / (time / 1000)));
	}

	void loadHighscore() throws IOException{
		Path path = Paths.get(HIGHSCOREFILE);

		String s = "";
		int i = 0;

		String[] tmp = new String[2];

		try (Scanner scanner =  new Scanner(path)){
			while (scanner.hasNextLine() && i< 10){
				s = scanner.nextLine();
				tmp = s.split(":");
				highscoreNames[i] = tmp[0];
				highscoreScores[i] = Integer.parseInt(tmp[1]);
				i++;
			}
		}
	}

	void addHighscore(){
		//System.out.println("addHighscore");

		int[] highscoreScoresN = new int[10];
		String[] highscoreNamesN = new String[10];

		int index = 0;
		int i = 0;
		int i2 = 0;

		while (score < highscoreScores[index]) {
			index++;
		}

		while (i < highscoreScores.length) {
			if(i == index){
				highscoreScoresN[i] = score;
				highscoreNamesN[i] = name;
			}else{
				highscoreScoresN[i] = highscoreScores[i2];
				highscoreNamesN[i] = highscoreNames[i2];
				i2++;
			}
			i++;
		}

		highscoreNames = highscoreNamesN;
		highscoreScores = highscoreScoresN;
	}

	void saveHighscore() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(HIGHSCOREFILE, "UTF-8");
		for (int i = 0; i < highscoreScores.length; i++) {
			writer.println(highscoreNames[i] + ":" + highscoreScores[i]);
		}
		writer.close();
	}
}