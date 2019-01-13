import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;


public class LandedTitlesCultureAdder {

	public static void main(String[] args) {
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Please enter the new culture: ");
		String newCulture = keyboard.nextLine();
		System.out.print("Please enter the culture that " + newCulture + " is based on: ");
		String origCulture = keyboard.nextLine();
		
		File oldFile = new File("landed_titles.txt");

		try {
			BufferedReader inFile = new BufferedReader(new FileReader(oldFile));
			PrintWriter outFile = new PrintWriter(new FileWriter("newlanded_titles.txt"));
			String line = "";
			while(inFile.ready()) {
				line = inFile.readLine();
				//System.out.println(line);
				outFile.println(line);
				if (line.indexOf(origCulture + " =") > -1) {
					outFile.println(line.substring(0, line.indexOf(origCulture + " =")) + newCulture
							+ line.substring(line.indexOf(origCulture + " =") + origCulture.length()));
				}
			}
			
			keyboard.close();
			inFile.close();
			outFile.close();
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
