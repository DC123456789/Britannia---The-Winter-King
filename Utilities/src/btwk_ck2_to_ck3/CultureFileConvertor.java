package btwk_ck2_to_ck3;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import ck_objects.BaseName;
import ck_objects.CharacterName;
import ck_objects.Culture;
import ck_objects.CultureGroup;
import ck_objects.Dynasty;

public class CultureFileConvertor {

	public static final String CK2_CULTURE_PATH = "\\common\\cultures";
	public static final String CK2_DYNASTY_PATH = "\\common\\dynasties";
	public static final String CK3_CULTURE_PATH = "\\common\\culture\\cultures";
	public static final String CK3_DYNASTY_PATH = "\\common\\dynasties";

	private Hashtable<String, Culture> cultures = new Hashtable<String, Culture>();
	private List<CultureGroup> cultureGroups = new ArrayList<CultureGroup>();
	private Hashtable<String, BaseName> baseNames = new Hashtable<String, BaseName>();		// Name to object
	private Hashtable<String, List<CharacterName>> charNames = new Hashtable<String, List<CharacterName>>();
	private Hashtable<String, Dynasty> dynasties = new Hashtable<String, Dynasty>();
	private Hashtable<String, String> dynastyPrefixes = new Hashtable<String, String>();	// Name to key
	private Hashtable<String, String> dynastyNames = new Hashtable<String, String>();		// Name to key
	private Hashtable<String, String> patronymPrefixes = new Hashtable<String, String>();		// Name to key
	private Hashtable<String, String> patronymSuffixes = new Hashtable<String, String>();		// Name to key
	
	public void convertNames() {
		// Read culture files
		final File btwkCk2CultureFolder = new File(Utils.BTWK_CK2_PATH + CK2_CULTURE_PATH);
		
		try (Stream<Path> walk = Files.walk(Paths.get(Utils.BTWK_CK2_PATH + CK2_CULTURE_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : btwkCk2CultureFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				if (file.contains("99_animals.txt"))
					continue;
				BufferedReader inCultureFile = new BufferedReader(new FileReader(file));
				while(inCultureFile.ready()) {
					// Enter into culture group block
					String token = Utils.readNextToken(inCultureFile);
					if (token.length() == 0) {
						continue;
					}
					String cultureGroupKey = token;
					CultureGroup newCultureGroup = new CultureGroup(cultureGroupKey);
					cultureGroups.add(newCultureGroup);
					
					// Consume tokens
					Utils.readNextToken(inCultureFile);
					Utils.readNextToken(inCultureFile);
					
					// Look for culture names
					while(inCultureFile.ready()) {					
						token = Utils.readNextToken(inCultureFile);
						if (token.equals("alternate_start") || token.equals("graphical_cultures")) {					
							Utils.readNextToken(inCultureFile);
							Utils.consumeBlock(inCultureFile);
						}
						else if (!token.equals("}")) {
							Culture newCulture = new Culture(token);
							cultures.put(token, newCulture);
							newCultureGroup.cultures.add(newCulture);
							
							Utils.readNextToken(inCultureFile);
							Utils.readNextToken(inCultureFile);
							
							String male_patronym = null;
							String female_patronym = null;
							boolean is_patronym_prefix = false;
						
							// Read in details for each culture
							while(inCultureFile.ready()) {
								token = Utils.readNextToken(inCultureFile);
								if (token.equals("color")) {
									Utils.readNextToken(inCultureFile);
									Utils.readNextToken(inCultureFile);
									float c0 = Float.parseFloat(Utils.readNextToken(inCultureFile));
									float c1 = Float.parseFloat(Utils.readNextToken(inCultureFile));
									float c2 = Float.parseFloat(Utils.readNextToken(inCultureFile));
									// Indicates floats
									if (c0 <= 1) {	
										newCulture.colour = new Color(c0, c1, c2);								
									}
									// Indicates ints
									else {
										newCulture.colour = new Color((int)c0, (int)c1, (int)c2);											
									}
									Utils.readNextToken(inCultureFile);	
								}
								else if (token.equals("male_names") || token.equals("female_names")) {
									String gender = token.split("_")[0];
									boolean isMale = gender.equals("male");
									List<CharacterName> namelist = newCulture.getNamelist(gender);
									Utils.readNextToken(inCultureFile);
									Utils.readNextToken(inCultureFile);
									
									// Read in each name
									while(inCultureFile.ready()) {
										token = Utils.readNextToken(inCultureFile);
										if (!token.equals("}")) {
											String name = token;
											
											// Strip out the frequency label, if relevant
											if (name.indexOf(":") > -1) {
												name = name.substring(0, name.indexOf(":"));
											}
											
											String[] splitName = name.split("_");
											
											// Find the base name, if it exists
											String charName;
											String baseName;
											if (splitName.length > 1) {
												charName = splitName[0];
												baseName = splitName[1];
											}
											else {
												charName = name;
												baseName = null;
											}
											
											// Find or construct the charname/basename objects
											CharacterName newCharName = null;
											List<CharacterName> existingCharNames = charNames.get(charName);
											if (existingCharNames != null) {
												for (CharacterName oldCharName: charNames.get(charName)) {
													// If we found an already existing name, check that the base name is the same
													if ((baseName != null && oldCharName.baseName != null 
															&& baseName.equals(oldCharName.baseName.name)) 
															|| baseName == null && oldCharName.baseName == null) {
														newCharName = oldCharName;
														break;
													}
													else {
														// If the name doesn't have a basename but we find another instance that 
														// does have a basename, we consider them the same
														if (baseName == null && oldCharName.baseName != null) {
															newCharName = oldCharName;
															break;															
														}
														// If the name has a basename but we find another instance that doesn't
														// have a basename, we also use the old character name but add in the
														// new basename
														else if (baseName != null && oldCharName.baseName == null) {
															BaseName newBaseName = baseNames.get(baseName);
															if (newBaseName == null) {
																newBaseName = new BaseName(convertToKey(baseName), baseName, isMale);
																baseNames.put(baseName, newBaseName);
															}
															oldCharName.baseName = newBaseName;
															newCharName = oldCharName;
															newBaseName.characterNames.add(oldCharName);
															
															break;															
														}
														// Otherwise, notify if we actually have two different base names
														else {
															System.err.println("Character name " + charName + " has different base names "
																	+ String.valueOf((Object)baseName) + " and " 
																	+ String.valueOf((Object)oldCharName.baseName));															
														}
													}
												}												
											}
											
											// If the name already exists, just add it in the list
											if (newCharName != null) {
												namelist.add(newCharName);
												newCharName.cultures.add(newCulture);
											}
											// Otherwise, need to make a new name
											else {
												String charNameKey = convertToKey(charName);
												// If multiple instances of the name exist, need to add a suffix
												if (existingCharNames != null && existingCharNames.size() > 0) {
													charNameKey = charNameKey + "_" + String.valueOf(existingCharNames.size());
												}
														
												newCharName = new CharacterName(charNameKey, charName);
												namelist.add(newCharName);
												newCharName.cultures.add(newCulture);
												
												// Add to the hash table
												if (existingCharNames != null) {
													existingCharNames.add(newCharName);
												}
												else {
													List<CharacterName> newList = new ArrayList<CharacterName>();
													newList.add(newCharName);
													charNames.put(charName, newList);
												}

												// Handle the base name
												if (baseName != null) {
													BaseName newBaseName = baseNames.get(baseName);
													if (newBaseName == null) {
														newBaseName = new BaseName(convertToKey(baseName), baseName, isMale);
														baseNames.put(baseName, newBaseName);
													}
													newCharName.baseName = newBaseName;
													newBaseName.characterNames.add(newCharName);
												}
												// If there is no base name, try to see if the name is the same as a basename
												// and add it in
											}											
										}
										else {		// Ending bracket
											break;
										}
									}
								}
								else if (token.equals("from_dynasty_prefix")) {
									Utils.readNextToken(inCultureFile);
									String prefix = Utils.readNextToken(inCultureFile);
									if (dynastyPrefixes.get(prefix) == null) {
										dynastyPrefixes.put(prefix, convertToKey(prefix));
									}
									newCulture.from_prefix = convertToKey(prefix);
								}
								else if (token.equals("male_patronym")) {
									Utils.readNextToken(inCultureFile);
									male_patronym = Utils.readNextToken(inCultureFile);
								}
								else if (token.equals("female_patronym")) {
									Utils.readNextToken(inCultureFile);
									female_patronym = Utils.readNextToken(inCultureFile);
								}
								else if (token.equals("prefix")) {
									Utils.readNextToken(inCultureFile);
									is_patronym_prefix = Utils.parseYesNo(Utils.readNextToken(inCultureFile));
								}
								else if (token.equals("pat_grf_name_chance")) {
									Utils.readNextToken(inCultureFile);
									newCulture.pat_grf_name_chance = Utils.readNextToken(inCultureFile);
								}
								else if (token.equals("mat_grf_name_chance")) {
									Utils.readNextToken(inCultureFile);
									newCulture.mat_grf_name_chance = Utils.readNextToken(inCultureFile);
								}
								else if (token.equals("father_name_chance")) {
									Utils.readNextToken(inCultureFile);
									newCulture.father_name_chance = Utils.readNextToken(inCultureFile);
								}
								else if (token.equals("pat_grm_name_chance")) {
									Utils.readNextToken(inCultureFile);
									newCulture.pat_grm_name_chance = Utils.readNextToken(inCultureFile);
								}
								else if (token.equals("mat_grm_name_chance")) {
									Utils.readNextToken(inCultureFile);
									newCulture.mat_grm_name_chance = Utils.readNextToken(inCultureFile);
								}
								else if (token.equals("mother_name_chance")) {
									Utils.readNextToken(inCultureFile);
									newCulture.mother_name_chance = Utils.readNextToken(inCultureFile);
								}
								else if (token.equals("dynasty_title_names")) {
									Utils.readNextToken(inCultureFile);
									newCulture.dynasty_title_names = Utils.readNextToken(inCultureFile);
								}
								else if (token.equals("founder_named_dynasties")) {
									Utils.readNextToken(inCultureFile);
									newCulture.founder_named_dynasties = Utils.readNextToken(inCultureFile);
								}
								else if (token.equals("dynasty_name_first")) {
									Utils.readNextToken(inCultureFile);
									newCulture.dynasty_name_first = Utils.readNextToken(inCultureFile);
								}
								else if (token.equals("bastard_dynasty_prefix")) {
									Utils.readNextToken(inCultureFile);
									String prefix = Utils.readNextToken(inCultureFile);
									if (dynastyPrefixes.get(prefix) == null) {
										dynastyPrefixes.put(prefix, convertToKey(prefix.toLowerCase()));
									}
									newCulture.bastard_dynasty_prefix = convertToKey(prefix.toLowerCase());
								}
								else if (token.equals("grammar_transform")) {
									Utils.readNextToken(inCultureFile);
									newCulture.grammar_transform = Utils.readNextToken(inCultureFile);
								}
								// Ignore these (no longer relevant)
								else if (token.equals("graphical_cultures") || token.equals("modifier")
										|| token.equals("dukes_called_kings") || token.equals("allow_looting")
										|| token.equals("seafarer") || token.equals("alternate_start")
										|| token.equals("parent") || token.equals("baron_titles_hidden")
										|| token.equals("count_titles_hidden") || token.equals("disinherit_from_blinding")
										|| token.equals("from_dynasty_suffix") || token.equals("allow_in_ruler_designer")
										|| token.equals("character_modifier") || token.equals("used_for_random")
										|| token.equals("horde") || token.equals("nomadic_in_alt_start")) {
									Utils.readNextToken(inCultureFile);
									Utils.consumeBlock(inCultureFile);
								}
								else if (!token.equals("}")) {	
									System.err.println("Unknown token in culture block " + newCulture.key + ": " + token);
									Utils.readNextToken(inCultureFile);
									Utils.consumeBlock(inCultureFile);
								}
								else {		// Ending bracket	
									break;
								}
							}
							
							// Handle patronyms
							if (male_patronym != null) {
								if (is_patronym_prefix) {
									if (patronymPrefixes.get(male_patronym) == null) {
										patronymPrefixes.put(male_patronym, convertToKey(male_patronym));
									}
									newCulture.patronym_prefix_male = convertToKey(male_patronym);									
								}
								else {
									if (patronymSuffixes.get(male_patronym) == null) {
										patronymSuffixes.put(male_patronym, convertToKey(male_patronym));
									}
									newCulture.patronym_suffix_male = convertToKey(male_patronym);									
								}
							}
							if (female_patronym != null) {
								if (is_patronym_prefix) {
									if (patronymPrefixes.get(female_patronym) == null) {
										patronymPrefixes.put(female_patronym, convertToKey(female_patronym));
									}
									newCulture.patronym_prefix_female = convertToKey(female_patronym);									
								}
								else {
									if (patronymSuffixes.get(female_patronym) == null) {
										patronymSuffixes.put(female_patronym, convertToKey(female_patronym));
									}
									newCulture.patronym_suffix_female = convertToKey(female_patronym);									
								}
							}
						}
						else {		// Ending bracket
							break;
						}
					}
				}
				inCultureFile.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		

		// Read dynasty files
		final File btwkCk2DynastyFolder = new File(Utils.BTWK_CK2_PATH + CK2_DYNASTY_PATH);
		
		try (Stream<Path> walk = Files.walk(Paths.get(Utils.BTWK_CK2_PATH + CK2_DYNASTY_PATH))) {

	        List<String> result = new ArrayList<>();
	        
	        for (final File f : btwkCk2DynastyFolder.listFiles()) {
	            if (f.isFile()) {
	                result.add(f.getAbsolutePath());
	            }
	        }

			for (String file : result) {
				BufferedReader inDynastyFile = new BufferedReader(new FileReader(file));
				while(inDynastyFile.ready()) {
					// Enter into dynasty block
					String token = Utils.readNextToken(inDynastyFile);
					if (token.length() == 0) {
						continue;
					}
					String dynastyKey = token;
					Dynasty newDynasty = new Dynasty(dynastyKey);
					dynasties.put(dynastyKey, newDynasty);
					
					// Consume tokens
					Utils.readNextToken(inDynastyFile);
					Utils.readNextToken(inDynastyFile);
					
					// Look for dynasties
					while(inDynastyFile.ready()) {
						token = Utils.readNextToken(inDynastyFile);
						if (token.equals("name")) {
							Utils.readNextToken(inDynastyFile);
							String name = Utils.readNextToken(inDynastyFile);
							String[] splitName = name.split(" ");
							if (!Utils.DYNASTY_PREFIXES.contains(splitName[0].toLowerCase())) {
								//if (splitName.length > 1 && splitName[0].length() <= 4) {
								//	System.out.println("Found dynasty name with short starting word: " + name);
								//}						
							}
							else {
								String prefix = splitName[0] + " ";
								// Fix accent on Uí
								if (prefix == "Ui")
									prefix = "Uí";
								if (dynastyPrefixes.get(prefix) == null) {
									dynastyPrefixes.put(prefix, convertToKey(prefix));
								}
								newDynasty.prefix = convertToKey(prefix);
								name = name.substring(name.indexOf(" ") + 1);
							}
							
							newDynasty.name = convertToKey(name);
							if (dynastyNames.get(name) == null) {
								dynastyNames.put(name, convertToKey(name));
							}
						}
						else if (token.equals("culture")) {
							Utils.readNextToken(inDynastyFile);
							newDynasty.culture = cultures.get(Utils.readNextToken(inDynastyFile));
							newDynasty.culture.dynasties.add(newDynasty);
						}
						else if (token.equals("used_for_random")) {
							Utils.readNextToken(inDynastyFile);
							newDynasty.used_for_random = Utils.parseYesNo(Utils.readNextToken(inDynastyFile));
						}
						// Ignore these (no longer relevant)
						else if (token.equals("can_appear") || token.equals("coat_of_arms")) {
							Utils.readNextToken(inDynastyFile);
							Utils.consumeBlock(inDynastyFile);
						}
						else if (!token.equals("}")) {	
							System.err.println("Unknown token in dynasty block " + newDynasty.key + ": " + token);
							Utils.readNextToken(inDynastyFile);
							Utils.consumeBlock(inDynastyFile);
						}
						else {		// Ending bracket	
							break;
						}
					}
				}
				inDynastyFile.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Actually print out/create the culture files
		for (CultureGroup cultureGroup: cultureGroups) {
			String fileName = "00_" + cultureGroup.key + ".txt";
			try {
				PrintWriter outFile = new PrintWriter(new FileWriter(fileName));
				String cultureGroupName = cultureGroup.key;
				if (cultureGroupName.indexOf("_group") == -1)
					cultureGroupName += "_group";
				outFile.println(cultureGroupName + " = {");
				outFile.println("\tgraphical_cultures = {");
				outFile.println("\t");
				outFile.println("\t}");
				
				for (Culture culture: cultureGroup.cultures) {
					outFile.println("\t");
					outFile.println("\t" + culture.key + " = {");
					outFile.println("\t\tcolor = { " + culture.colour.getRed() + " " + culture.colour.getGreen() + 
							" " + culture.colour.getBlue() + " }");	
					outFile.println("\t\t");
					
					if (culture.dynasty_name_first != null) {
						outFile.println("\t\tdynasty_name_first = " + culture.dynasty_name_first);	
						outFile.println("\t\t");
					}
					
					Collections.sort(culture.dynasties, new Comparator<Dynasty>() {
					    @Override
					    public int compare(Dynasty lhs, Dynasty rhs) {
					        return lhs.name.compareTo(rhs.name);
					    }
					});
					outFile.println("\t\tcadet_dynasty_names = {");	
					for (Dynasty dynasty: culture.dynasties) {
						if (dynasty.used_for_random) {
							if (dynasty.prefix != null) {
								outFile.println("\t\t\t{ \"dynnp_" + dynasty.prefix + "\" \"dynn_" + dynasty.name + "\" }" );							
							}
							else {
								outFile.println("\t\t\t{ \"dynn_" + dynasty.name + "\" }" );							
							}
						}
					}
					outFile.println("\t\t}");
					outFile.println("\t\t");
					outFile.println("\t\tdynasty_names = {");	
					for (Dynasty dynasty: culture.dynasties) {
						if (dynasty.used_for_random) {
							if (dynasty.prefix != null) {
								outFile.println("\t\t\t{ \"dynnp_" + dynasty.prefix + "\" \"dynn_" + dynasty.name + "\" }" );							
							}
							else {
								outFile.println("\t\t\t{ \"dynn_" + dynasty.name + "\" }" );							
							}
						}
					}
					outFile.println("\t\t}");
					outFile.println("\t\t");

					Collections.sort(culture.maleNames, new Comparator<CharacterName>() {
					    @Override
					    public int compare(CharacterName lhs, CharacterName rhs) {
					        return lhs.key.compareTo(rhs.key);
					    }
					});
					outFile.println("\t\tmale_names = {");	
					outFile.print("\t\t\t");
					for (int i = 0; i < culture.maleNames.size(); i++) {
						CharacterName name = culture.maleNames.get(i);
						outFile.print(name.key + " ");
						if ((i + 1) % 20 == 0) {
							outFile.print("\n\t\t\t");
						}
					}
					outFile.println("\n\t\t}");

					Collections.sort(culture.femaleNames, new Comparator<CharacterName>() {
					    @Override
					    public int compare(CharacterName lhs, CharacterName rhs) {
					        return lhs.key.compareTo(rhs.key);
					    }
					});
					outFile.println("\t\tfemale_names = {");	
					outFile.print("\t\t\t");
					for (int i = 0; i < culture.femaleNames.size(); i++) {
						CharacterName name = culture.femaleNames.get(i);
						outFile.print(name.key + " ");
						if ((i + 1) % 20 == 0) {
							outFile.print("\n\t\t\t");
						}
					}
					outFile.println("\n\t\t}");
					outFile.println("\t\t");
					
					if (culture.from_prefix != null) {
						outFile.println("\t\tdynasty_of_location_prefix = \"dynnp_" + culture.from_prefix + "\"");						
					}
					if (culture.grammar_transform != null) {
						outFile.println("\t\tgrammar_transform = " + culture.grammar_transform);						
					}
					if (culture.patronym_prefix_male != null) {
						outFile.println("\t\tpatronym_prefix_male = \"dynnpat_pre_" + culture.patronym_prefix_male + "\"");						
					}
					if (culture.patronym_prefix_female != null) {
						outFile.println("\t\tpatronym_prefix_female = \"dynnpat_pre_" + culture.patronym_prefix_female + "\"");						
					}
					if (culture.patronym_suffix_male != null) {
						outFile.println("\t\tpatronym_suffix_male = \"dynnpat_suf_" + culture.patronym_suffix_male + "\"");						
					}
					if (culture.patronym_suffix_female != null) {
						outFile.println("\t\tpatronym_suffix_female = \"dynnpat_suf_" + culture.patronym_suffix_female + "\"");						
					}
					if (cultureGroup.key.equals("celtic") || culture.key.equals("proto_norse") || culture.key.equals("dane") || culture.key.equals("norse_gael")
							 || culture.key.equals("angle") || culture.key.equals("oldsaxon") || culture.key.equals("saxon")
							 || culture.key.equals("oldfrisian") || culture.key.equals("frisian") || culture.key.equals("jute")
							 || culture.key.equals("anglosaxon")) {
						outFile.println("\t\talways_use_patronym = yes");
					}
					outFile.println("\t\t");

					if (culture.dynasty_title_names != null) {
						outFile.println("\t\tdynasty_title_names = " + culture.dynasty_title_names);						
					}
					if (culture.founder_named_dynasties != null) {
						outFile.println("\t\tfounder_named_dynasties = " + culture.founder_named_dynasties);						
					}
					if (culture.bastard_dynasty_prefix != null) {
						outFile.println("\t\tbastard_dynasty_prefix = \"dynnp_" + culture.bastard_dynasty_prefix + "\"");						
					}
					outFile.println("\t\t");
					
					if (culture.pat_grf_name_chance != null) {
						outFile.println("\t\t# Chance of male children being named after their paternal or maternal grandfather, or their father. Sum must not exceed 100.");
						outFile.println("\t\tpat_grf_name_chance = " + culture.pat_grf_name_chance);						
					}
					if (culture.mat_grf_name_chance != null) {
						outFile.println("\t\tmat_grf_name_chance = " + culture.mat_grf_name_chance);						
					}
					if (culture.father_name_chance != null) {
						outFile.println("\t\tfather_name_chance = " + culture.father_name_chance);						
					}
					if (culture.pat_grm_name_chance != null) {
						outFile.println("\t\t# Chance of female children being named after their paternal or maternal grandmother, or their mother. Sum must not exceed 100.");
						outFile.println("\t\tpat_grm_name_chance = " + culture.pat_grm_name_chance);						
					}
					if (culture.mat_grm_name_chance != null) {
						outFile.println("\t\tmat_grm_name_chance = " + culture.mat_grm_name_chance);						
					}
					if (culture.mother_name_chance != null) {
						outFile.println("\t\tmother_name_chance = " + culture.mother_name_chance);						
					}
					outFile.println("\t\t");					

					outFile.println("\t}");
				}

				outFile.println("}");
				outFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Print out the cultural name files
		String culturalNamesfileName = "00_names.txt";
		try {
			PrintWriter outFile = new PrintWriter(new FileWriter(culturalNamesfileName));

			List<BaseName> baseNameList = new ArrayList<BaseName>();
			for (Entry<String, BaseName> baseNameEntry: baseNames.entrySet()) {
				baseNameList.add(baseNameEntry.getValue());
			}
			Collections.sort(baseNameList, new Comparator<BaseName>() {
			    @Override
			    public int compare(BaseName lhs, BaseName rhs) {
			        return lhs.key.toLowerCase().compareTo(rhs.key.toLowerCase());
			    }
			});
			
			for (BaseName baseName: baseNameList) {
				outFile.print(baseName.key.toLowerCase() + "_" + baseName.getGender() + " = { ");
				for (CharacterName charName: baseName.characterNames) {
					outFile.print(charName.key + " ");
				}
				outFile.println("}");
			}
			outFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Print out the name localisation files
		String charLocfileName = "BTWK_character_names_l_english.yml";
		try {
			PrintWriter outFile = new PrintWriter(new FileWriter(charLocfileName));
			outFile.println("l_english:");
			
			List<Entry<String, String>> nameKeys = new ArrayList<Entry<String, String>>();
			for (Entry<String, List<CharacterName>> charNameEntry: charNames.entrySet()) {
				for (CharacterName charName: charNameEntry.getValue()) {
					nameKeys.add(new AbstractMap.SimpleEntry<String, String>(charName.key, charName.name));
				}
			}			
			Collections.sort(nameKeys, new Comparator<Entry<String, String>>() {
			    @Override
			    public int compare(Entry<String, String> lhs, Entry<String, String> rhs) {
			        return lhs.getKey().compareTo(rhs.getKey());
			    }
			});
			
			for (Entry<String, String> charName: nameKeys) {
				outFile.println(" " + charName.getKey() + ":0 \"" + charName.getValue() + "\"");
			}
			outFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Print out the dynasty localisation files
		String dynastyLocfileName = "BTWK_dynasty_names_l_english.yml";
		try {
			PrintWriter outFile = new PrintWriter(new FileWriter(dynastyLocfileName));
			outFile.println("l_english:");
			outFile.println(" FOUNDER_BASED_NAME_POSTFIX:0 \"ing\"");
			
			outFile.println(" ############################################################");
			outFile.println(" # Prefixes");
			outFile.println(" ############################################################");			
			List<Entry<String, String>> dynastyPrefixesList = new ArrayList<Entry<String, String>>();
			for (Entry<String, String> dynastyPrefix: dynastyPrefixes.entrySet()) {
				dynastyPrefixesList.add(dynastyPrefix);
			}			
			Collections.sort(dynastyPrefixesList, new Comparator<Entry<String, String>>() {
			    @Override
			    public int compare(Entry<String, String> lhs, Entry<String, String> rhs) {
			        return lhs.getValue().compareTo(rhs.getValue());
			    }
			});			
			for (Entry<String, String> dynastyPrefix: dynastyPrefixesList) {
				outFile.println(" dynnp_" + dynastyPrefix.getValue() + ":0 \"" + dynastyPrefix.getKey() + "\"");
			}
			outFile.println(" ");
			
			outFile.println(" # PATRONYMS — PREFIXES AND SUFFIXES");
			List<Entry<String, String>> patronymPrefixesList = new ArrayList<Entry<String, String>>();
			for (Entry<String, String> patronymPrefix: patronymPrefixes.entrySet()) {
				patronymPrefixesList.add(patronymPrefix);
			}			
			Collections.sort(patronymPrefixesList, new Comparator<Entry<String, String>>() {
			    @Override
			    public int compare(Entry<String, String> lhs, Entry<String, String> rhs) {
			        return lhs.getValue().compareTo(rhs.getValue());
			    }
			});			
			for (Entry<String, String> patronymPrefix: patronymPrefixesList) {
				outFile.println(" dynnpat_pre_" + patronymPrefix.getValue() + ":0 \"" + patronymPrefix.getKey() + "\"");
			}
			outFile.println(" ");
			
			List<Entry<String, String>> patronymSuffixesList = new ArrayList<Entry<String, String>>();
			for (Entry<String, String> patronymSuffix: patronymSuffixes.entrySet()) {
				patronymSuffixesList.add(patronymSuffix);
			}			
			Collections.sort(patronymSuffixesList, new Comparator<Entry<String, String>>() {
			    @Override
			    public int compare(Entry<String, String> lhs, Entry<String, String> rhs) {
			        return lhs.getValue().compareTo(rhs.getValue());
			    }
			});			
			for (Entry<String, String> patronymSuffix: patronymSuffixesList) {
				outFile.println(" dynnpat_suf_" + patronymSuffix.getValue() + ":0 \"" + patronymSuffix.getKey() + "\"");
			}
			outFile.println(" ");
			

			outFile.println(" ############################################################");
			outFile.println(" # Names");
			outFile.println(" ############################################################");			
			List<Entry<String, String>> dynastyNamesList = new ArrayList<Entry<String, String>>();
			for (Entry<String, String> dynastyName: dynastyNames.entrySet()) {
				dynastyNamesList.add(dynastyName);
			}			
			Collections.sort(dynastyNamesList, new Comparator<Entry<String, String>>() {
			    @Override
			    public int compare(Entry<String, String> lhs, Entry<String, String> rhs) {
			        return lhs.getValue().compareTo(rhs.getValue());
			    }
			});			
			for (Entry<String, String> dynastyName: dynastyNamesList) {
				outFile.println(" dynn_" + dynastyName.getValue() + ":0 \"" + dynastyName.getKey() + "\"");
			}
			outFile.println(" ");
			
			outFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Removes all special characters from a string
	 * @param name
	 * @return
	 */
	public static String convertToKey(String name) {
		// Remove leading/trailing spaces
		name = name.strip();
		
		// If the whole string is ASCII and has no strings, just return it
		if (name.chars().allMatch(c -> (c < 128 && c != ' ' && c != '\''))) {
			return name;			
		}
		
		// Otherwise, need to build character-by-character
		String key = "";
		for (int i = 0; i < name.length(); i++) {
			char currentChar = name.charAt(i);
			if (currentChar < 128 && currentChar != ' ' && currentChar != '\'')
				key += currentChar;
			else {
				String converted_char = Utils.SPECIAL_CHARACTERS_TO_ASCII.get(currentChar);
				if (converted_char == null) {
					System.err.println("Unknown special character: " + currentChar);
				}
				key += Utils.SPECIAL_CHARACTERS_TO_ASCII.get(currentChar);
			}
		}
		return key;
		
	}
	
	public static void main(String[] args) {
		CultureFileConvertor newConvertor = new CultureFileConvertor();
		System.out.println("Converting culture files...");
		newConvertor.convertNames();
		System.out.println("Done!");
	}
	
}
