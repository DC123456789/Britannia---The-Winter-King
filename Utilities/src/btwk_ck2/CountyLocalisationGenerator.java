package btwk_ck2;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class CountyLocalisationGenerator {

	public static void main(String[] args) {
		File provinceHistoryFolder = new File("C:/Users/chand/Documents/Paradox Interactive/Crusader Kings II/mod/Britannia/history/provinces");
		File provinceNamesFile = new File("C:/Users/chand/Documents/Paradox Interactive/Crusader Kings II/mod/Britannia/localisation/00 Province Names.csv");
		File mapDefaultFile = new File("C:/Users/chand/Documents/Paradox Interactive/Crusader Kings II/mod/Britannia/map/default.map");
		File[] provinceHistories = provinceHistoryFolder.listFiles();
		
		int num_provinces = -1;
		
		try {
			// Read in number of provinces
			BufferedReader mapDefaultInFile = new BufferedReader(new FileReader(mapDefaultFile));
			String line = "";
			while(mapDefaultInFile.ready()) {
				line = mapDefaultInFile.readLine();
				//System.out.println(line);
				String[] splitLine = line.trim().split(" ");
				if (splitLine[0].equals("max_provinces")) {
					num_provinces = Integer.parseInt(splitLine[2]);
					break;
				}
			}
			
			//System.out.println(num_provinces);
			mapDefaultInFile.close();
			
			String[][] countyArray = new String[num_provinces][2];
			
			// Read in each province file
			for (File provinceHistoryFile : provinceHistories) {
			    if (provinceHistoryFile.isFile()) {
					String[] splitName = provinceHistoryFile.getName().trim().split(" ");
			    	int province_num = Integer.parseInt(splitName[0]);
					BufferedReader provinceHistoryInFile = new BufferedReader(new FileReader(provinceHistoryFile));
					// Read the county tag
					while(provinceHistoryInFile.ready()) {
						line = provinceHistoryInFile.readLine();
						//System.out.println(line);
						String[] splitLine = line.trim().split(" ");
						if (splitLine[0].equals("title")) {
							countyArray[province_num - 1][0] = splitLine[2];
							break;
						}
					}
					provinceHistoryInFile.close();
			    }
			}
			
			// Read in province localisation
			BufferedReader provinceNamesInFile = new BufferedReader(new FileReader(provinceNamesFile));
			while(provinceNamesInFile.ready()) {
				line = provinceNamesInFile.readLine();
				//System.out.println(line);
				String[] splitLine = line.trim().split(";");
				if (splitLine[0].indexOf("PROV") == 0) {
					int province_num = Integer.parseInt(splitLine[0].substring(4));
					countyArray[province_num - 1][1] = splitLine[1];
				}
			}
			provinceNamesInFile.close();
			
			for (int i = 0; i < countyArray.length - 1; i++) {
				if (countyArray[i][0] != null) {
					System.out.println(countyArray[i][0] + ";" + countyArray[i][1] + ";;;;;;;;;;;;;;x");
				}
			}
			//outFile.close();
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
