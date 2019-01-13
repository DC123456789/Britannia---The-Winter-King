import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;

public class AncrelConvertor {

	public static void main(String[] args) {
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Please enter the name of the file: ");
		String fileName = keyboard.nextLine();
		System.out.print("Please enter the old namespace: ");
		String origNamespace = keyboard.nextLine();
		System.out.print("Please enter the new namespace: ");
		String newNamespace = keyboard.nextLine();
		System.out.print("Please enter the event id difference: ");
		int eventIdDifference = Integer.parseInt(keyboard.nextLine().trim());
		
		File oldFile = new File(fileName);

		try {
			BufferedReader inFile = new BufferedReader(new FileReader(oldFile));
			PrintWriter outFile = new PrintWriter(new FileWriter("new" + oldFile));
			String line = "";
			while(inFile.ready()) {
				line = inFile.readLine();
				String[] splitLine = line.split(" |\t|;");
				for (String word : splitLine) {
					int oldNamespaceIndex = word.indexOf(origNamespace + ".");
					if (oldNamespaceIndex > -1) {		// If old namespace is found
						String newWord = "";
						String eventID = "";
						String postNamespace = word.substring(oldNamespaceIndex + origNamespace.length() + 1).trim();
						int eventIDInt; 
						if (postNamespace.indexOf('.') > -1) {
							try {
								eventIDInt = Integer.parseInt(postNamespace.substring(0, postNamespace.indexOf('.')));
								eventIDInt -= eventIdDifference;
								eventID += eventIDInt;
							}
							catch (NumberFormatException e) {
								eventID = postNamespace;
							}
						}
						else {
							try {
								eventIDInt = Integer.parseInt(postNamespace);
								eventIDInt -= eventIdDifference;
								eventID += eventIDInt;
							}
							catch (NumberFormatException e) {
								eventID = postNamespace;
							}
						}
						if (word.indexOf("EVT") > -1) {	// Standard localisation format
							if (word.indexOf("NAME") > -1)	{	// event title
								newWord += newNamespace;
								newWord += ".";
								newWord += eventID;
								newWord += ".title";					
							}
							else if (word.indexOf("DESC") > -1)	{	// event description
								newWord += newNamespace;
								newWord += ".";
								newWord += eventID;	
								newWord += ".desc";					
							}
							else if (word.indexOf("OPT") > -1)	{	// event option name
								newWord += newNamespace;
								newWord += ".";
								newWord += eventID;
								newWord += ".";	
								newWord += (word.substring(oldNamespaceIndex - 1, oldNamespaceIndex)).toLowerCase();					
							}
							else if (word.indexOf("TOOLTIP") > -1)	{	// event tooltip
								newWord += newNamespace;
								newWord += ".";
								newWord += eventID;	
								newWord += ".tt";					
							}
						}
						else {		// Just copy the word, except for changing the namespace and removing leading 0s on the event id
							newWord += newNamespace;
							newWord += ".";
							newWord += eventID;
							if (postNamespace.indexOf('.') > 0 && postNamespace.indexOf('.') < postNamespace.length() - 1)
								newWord += postNamespace.substring(postNamespace.indexOf('.'));
						}
						//System.out.println(word);
						//System.out.println(newWord);
						line = line.replaceAll(word, newWord);
					}
					else if (word.equals("namespace")) {	// Replace the namespace token
						line = line.replaceAll(origNamespace, newNamespace);						
					}
				}
				outFile.println(line);
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
