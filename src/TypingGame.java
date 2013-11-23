import org.newdawn.slick.*;
import java.io.File;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.io.IOException;
import java.util.regex.Pattern;

class TypingGame extends BasicGame{
	String[] text;
	int word;
	int timeLeft;
	int time;
	private static final int TIME = 60000;

	int cpm;
	int wpm;

	Color primary = Color.black;
	Color secondary = Color.gray;
	Color background = Color.white;
	Color error = Color.red;

	String curWord;

	Pattern p;

	boolean resultScreen = false;

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
		
		//Read the file
		try{
			text = readFile("text");
		}catch(IOException e){
			e.printStackTrace();
		}

		//Declaration
		curWord = "";
		font1 = new TrueTypeFont(tFont1, false);
		font2 = new TrueTypeFont(tFont2, false);
		word = 0;
		time = 0;
		timeLeft = TIME - time;
		p = Pattern.compile("^[a-zA-Z0-9,.:!?;]$");
	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException{
		//Background
		g.setColor(background);
		g.fillRect(0, 0, 1024, 768);
		
		//Check if the game is over
		if(!resultScreen){
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
		}else {
			//If the game is over then draw the wpm
			g.setFont(font2);
			g.setColor(primary);
			g.drawString(wpm + " WPM", 300f, 300f);
		}

	}

	@Override
	public void update(GameContainer container, int delta) throws SlickException{
		if(started){
			if(!resultScreen){
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

		started = true;
	}

	String[] readFile(String filename) throws IOException {
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
		return s.split(" ");
	}

	void end(int time){
		//word++;
		mayType = false;
		resultScreen = true;
		calculateWPM(time);
	}

	void calculateWPM(int time){
		for (int i = 0; i < word; i++) {
			cpm += text[i].length();
		}
		wpm = Math.round(cpm/5 * ((TIME / 1000) / (time / 1000)));
		//wpm = word * ((TIME / 1000) / (time / 1000));
	}
}