
public class ReligionTemplate {

	public static void main(String[] args) {
		String[] religions = { "catholic", "pelagian", "nestorian", "priscillian", "arian", "gnostic", "sunni",
				"shiite", "ibadi", "jewish", "manichean", "briton_pagan", "pict_pagan", "gael_pagan", "norse_pagan",
				"germanic_pagan", "frankish_pagan", "suebic_pagan", "gothic_pagan", "alan_pagan", "tengri_pagan",
				"vasconic_pagan", "briton_pagan_reformed", "britannic_pagan", "pict_pagan_reformed", "gael_pagan_reformed", 
				"norse_pagan_reformed", "germanic_pagan_reformed", "frankish_pagan_reformed", "suebic_pagan_reformed", "gothic_pagan_reformed",
				"alan_pagan_reformed", "vasconic_pagan_reformed", "roman_pagan", "roman_pagan_reformed", "sol_invictus",
				"titan_pagan", "promethean_pagan" };

		String[] pagan_religion_roots = { "briton", "pict", "gael", "norse", "germanic", "frankish", "suebic", "gothic",
				"alan", "tengri", "vasconic", "roman" };

		String[] nonpagan_religions = { "catholic", "pelagian", "nestorian", "priscillian", "arian", "gnostic", "sunni",
				"shiite", "ibadi", "jewish", "manichean" };

		String[] pagan_religions = { "briton_pagan", "pict_pagan", "gael_pagan", "norse_pagan",
				"germanic_pagan", "frankish_pagan", "suebic_pagan", "gothic_pagan", "alan_pagan", "tengri_pagan",
				"briton_pagan_reformed", "britannic_pagan", "pict_pagan_reformed", "gael_pagan_reformed", "norse_pagan_reformed",
				"germanic_pagan_reformed", "frankish_pagan_reformed", "suebic_pagan_reformed", "gothic_pagan_reformed",
				"alan_pagan_reformed", "vasconic_pagan_reformed", "tengri_pagan_reformed", "roman_pagan", "roman_pagan_reformed", 
				"sol_invictus", "titan_pagan", "promethean_pagan" };
		
		String[] main_nonpagan_religions = { "catholic", "arian", "sunni", "shiite", "ibadi", "jewish", "manichean" };

		String[] main_reformed_religions = { "catholic", "arian", "sunni", "shiite", "ibadi", "jewish", "manichean", "briton_pagan_reformed",
				"pict_pagan_reformed", "gael_pagan_reformed", "britannic_pagan", "norse_pagan_reformed",
				"germanic_pagan_reformed", "frankish_pagan_reformed", "suebic_pagan_reformed", "gothic_pagan_reformed",
				"alan_pagan_reformed", "vasconic_pagan_reformed", "tengri_pagan_reformed", "roman_pagan_reformed", "sol_invictus" };

		for (String religion : pagan_religion_roots) {
			String capitalized_religion = Character.toUpperCase(religion.charAt(0)) + religion.substring(1);
			System.out.println("string_convert_only_" + religion + "s;");
		}
	}

}
