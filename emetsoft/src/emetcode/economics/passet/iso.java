package emetcode.economics.passet;

import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

public class iso {

	public static final String[][] currencies_array = {
			{ "AED", "United Arab Emirates dirham", "د.إ" },
			{ "AFN", "Afghan afghani", "$" }, { "ALL", "Albanian lek", "L" },
			{ "AMD", "Armenian dram", "դր." },
			{ "ANG", "Netherlands Antillean guilder", "ƒ" },
			{ "AOA", "Angolan kwanza", "Kz" },
			{ "ARS", "Argentine peso", "$" },
			{ "AUD", "Australian dollar", "$" },
			{ "AWG", "Aruban florin", "ƒ" },
			{ "AZN", "Azerbaijani manat", "$" },
			{ "BAM", "Bosnia mark", "KM" }, { "BBD", "Barbadian dollar", "$" },
			{ "BDT", "Bangladeshi taka", "$" },
			{ "BGN", "Bulgarian lev", "лв" },
			{ "BHD", "Bahraini dinar", ".د.ب" },
			{ "BIF", "Burundian franc", "Fr" },
			{ "BMD", "Bermudian dollar", "$" },
			{ "BND", "Brunei dollar", "$" },
			{ "BOB", "Bolivian boliviano", "Bs." },
			{ "BRL", "Brazilian real", "R$" },
			{ "BSD", "Bahamian dollar", "$" },
			{ "BTN", "Bhutanese ngultrum", "Nu." },
			{ "BWP", "Botswana pula", "P" },
			{ "BYR", "Belarusian ruble", "Br" },
			{ "BZD", "Belize dollar", "$" }, { "CAD", "Canadian dollar", "$" },
			{ "CDF", "Congolese franc", "Fr" }, { "CHF", "Swiss franc", "Fr" },
			{ "CLP", "Chilean peso", "$" }, { "CNY", "Chinese yuan", "¥" },
			{ "COP", "Colombian peso", "$" },
			{ "CRC", "Costa Rican colón", "₡" },
			{ "CUC", "Cuban convertible peso", "$" },
			{ "CUP", "Cuban peso", "$" },
			{ "CVE", "Cape Verdean escudo", "$" },
			{ "CZK", "Czech koruna", "Kč" },
			{ "DJF", "Djiboutian franc", "Fr" },
			{ "DKK", "Danish krone", "kr" }, { "DOP", "Dominican peso", "$" },
			{ "DZD", "Algerian dinar", "د.ج" },
			{ "EGP", "Egyptian pound", " ج.م" },
			{ "ERN", "Eritrean nakfa", "Nfk" },
			{ "ETB", "Ethiopian birr", "Br" }, { "EUR", "Euro", "€" },
			{ "FJD", "Fijian dollar", "$" },
			{ "FKP", "Falkland Islands pound", "£" },
			{ "GBP", "British pound", "£" }, { "GEL", "Georgian lari", "ლ" },
			{ "GHS", "Ghana cedi", "₵" }, { "GIP", "Gibraltar pound", "£" },
			{ "GMD", "Gambian dalasi", "D" }, { "GNF", "Guinean franc", "Fr" },
			{ "GTQ", "Guatemalan quetzal", "Q" },
			{ "GYD", "Guyanese dollar", "$" },
			{ "HKD", "Hong Kong dollar", "$" },
			{ "HNL", "Honduran lempira", "L" },
			{ "HRK", "Croatian kuna", "kn" }, { "HTG", "Haitian gourde", "G" },
			{ "HUF", "Hungarian forint", "Ft" },
			{ "IDR", "Indonesian rupiah", "Rp" },
			{ "ILS", "Israeli new shekel", "₪" },
			{ "INR", "Indian rupee", "$" }, { "IQD", "Iraqi dinar", "ع.د" },
			{ "IRR", "Iranian rial", "$" }, { "ISK", "Icelandic króna", "kr" },
			{ "JMD", "Jamaican dollar", "$" },
			{ "JOD", "Jordanian dinar", "د.ا" },
			{ "JPY", "Japanese yen", "¥" }, { "KES", "Kenyan shilling", "Sh" },
			{ "KGS", "Kyrgyzstani som", "лв" },
			{ "KHR", "Cambodian riel", "$" },
			{ "KMF", "Comorian franc", "Fr" },
			{ "KPW", "North Korean won", "₩" },
			{ "KRW", "South Korean won", "₩" },
			{ "KWD", "Kuwaiti dinar", "د.ك" },
			{ "KYD", "Cayman Islands dollar", "$" },
			{ "KZT", "Kazakhstani tenge", "₸" }, { "LAK", "Lao kip", "₭" },
			{ "LBP", "Lebanese pound", "ل.ل" },
			{ "LKR", "Sri Lankan rupee", "Rs" },
			{ "LRD", "Liberian dollar", "$" }, { "LSL", "Lesotho loti", "L" },
			{ "LTL", "Lithuanian litas", "Lt" },
			{ "LVL", "Latvian lats", "Ls" }, { "LYD", "Libyan dinar", "ل.د" },
			{ "MAD", "Moroccan dirham", "د.م." },
			{ "MDL", "Moldovan leu", "L" }, { "MGA", "Malagasy ariary", "Ar" },
			{ "MKD", "Macedonian denar", "ден" },
			{ "MMK", "Burmese kyat", "Ks" },
			{ "MNT", "Mongolian tögrög", "₮" },
			{ "MOP", "Macanese pataca", "P" },
			{ "MRO", "Mauritanian ouguiya", "UM" },
			{ "MUR", "Mauritian rupee", "₨" },
			{ "MVR", "Maldivian rufiyaa", "$" },
			{ "MWK", "Malawian kwacha", "MK" }, { "MXN", "Mexican peso", "$" },
			{ "MYR", "Malaysian ringgit", "RM" },
			{ "MZN", "Mozambican metical", "MT" },
			{ "NAD", "Namibian dollar", "$" },
			{ "NGN", "Nigerian naira", "₦" },
			{ "NIO", "Nicaraguan córdoba", "C$" },
			{ "NOK", "Norwegian krone", "kr" },
			{ "NPR", "Nepalese rupee", "₨" },
			{ "NZD", "New Zealand dollar", "$" },
			{ "OMR", "Omani rial", "ر.ع." },
			{ "PAB", "Panamanian balboa", "B/." },
			{ "PEN", "Peruvian nuevo sol", "S/." },
			{ "PGK", "Papua New Guinean kina", "K" },
			{ "PHP", "Philippine peso", "₱" },
			{ "PKR", "Pakistani rupee", "₨" }, { "PLN", "Polish złoty", "zł" },
			{ "PYG", "Paraguayan guaraní", "₲" },
			{ "QAR", "Qatari riyal", "ر.ق" }, { "RON", "Romanian leu", "L" },
			{ "RSD", "Serbian dinar", "дин." },
			{ "RUB", "Russian ruble", "р." }, { "RWF", "Rwandan franc", "Fr" },
			{ "SAR", "Saudi riyal", "ر.س" },
			{ "SBD", "Solomon Islands dollar", "$" },
			{ "SCR", "Seychellois rupee", "₨" },
			{ "SDG", "Sudanese pound", "£" }, { "SEK", "Swedish krona", "kr" },
			{ "SGD", "Singapore dollar", "$" },
			{ "SHP", "Saint Helena pound", "£" },
			{ "SLL", "Sierra Leonean leone", "Le" },
			{ "SOS", "Somali shilling", "Sh" },
			{ "SRD", "Surinamese dollar", "$" },
			{ "SSP", "South Sudanese pound", "£" },
			{ "STD", "São Tomé dobra", "Db" },
			{ "SVC", "Salvadoran colón", "₡" },
			{ "SYP", "Syrian pound", " ل.س" },
			{ "SZL", "Swazi lilangeni", "L" }, { "THB", "Thai baht", "฿" },
			{ "TJS", "Tajikistani somoni", "ЅМ" },
			{ "TMT", "Turkmenistan manat", "m" },
			{ "TND", "Tunisian dinar", "د.ت" },
			{ "TOP", "Tongan paʻanga", "T$" }, { "TRY", "Turkish lira", "$" },
			{ "TTD", "Trinidad and Tobago dollar", "$" },
			{ "TWD", "New Taiwan dollar", "$" },
			{ "TZS", "Tanzanian shilling", "Sh" },
			{ "UAH", "Ukrainian hryvnia", "₴" },
			{ "UGX", "Ugandan shilling", "Sh" },
			{ "USD", "United States dollar", "$" },
			{ "UYU", "Uruguayan peso", "$" },
			{ "UZS", "Uzbekistani som", "лв" },
			{ "VEF", "Venezuelan bolívar", "Bs F" },
			{ "VND", "Vietnamese đồng", "₫" }, { "VUV", "Vanuatu vatu", "Vt" },
			{ "WST", "Samoan tālā", "T" },
			{ "XAF", "Central African CFA franc", "Fr" },
			{ "XCD", "East Caribbean dollar", "$" },
			{ "XOF", "West African CFA franc", "Fr" },
			{ "XPF", "CFP franc", "Fr" }, { "YER", "Yemeni rial", "$" },
			{ "ZAR", "South African rand", "R" },
			{ "ZMK", "Zambian kwacha", "ZK" },
			{ "ZWL", "Zimbabwean dollar", "$" } };

	public static final String[][] country_codes_array = {
			{ "AC", "Ascension Island" }, { "AD", "Andorra" },
			{ "AE", "United Arab Emirates" }, { "AF", "Afghanistan" },
			{ "AG", "Antigua and Barbuda" }, { "AI", "Anguilla" },
			{ "AL", "Albania" }, { "AM", "Armenia" },
			{ "AN", "Netherlands Antilles" }, { "AO", "Angola" },
			{ "AQ", "Antarctica" }, { "AR", "Argentina" },
			{ "AS", "American Samoa" }, { "AT", "Austria" },
			{ "AU", "Australia" }, { "AW", "Aruba" },
			{ "AX", "Aland Islands" }, { "AZ", "Azerbaijan" },
			{ "BA", "Bosnia and Herzegovina" }, { "BB", "Barbados" },
			{ "BD", "Bangladesh" }, { "BE", "Belgium" },
			{ "BF", "Burkina Faso" }, { "BG", "Bulgaria" },
			{ "BH", "Bahrain" }, { "BI", "Burundi" }, { "BJ", "Benin" },
			{ "BL", "Saint Barthélemy" }, { "BM", "Bermuda" },
			{ "BN", "Brunei Darussalam" }, { "BO", "Bolivia" },
			{ "BQ", "Bonaire" }, { "BR", "Brazil" }, { "BS", "Bahamas" },
			{ "BT", "Bhutan" }, { "BU", "Burma" }, { "BV", "Bouvet Island" },
			{ "BW", "Botswana" }, { "BY", "Belarus" }, { "BZ", "Belize" },
			{ "CA", "Canada" }, { "CC", "Cocos (Keeling) Islands" },
			{ "CD", "Congo" }, { "CF", "Central African Republic" },
			{ "CG", "Congo" }, { "CH", "Switzerland" },
			{ "CI", "Cote d'Ivoire" }, { "CK", "Cook Islands" },
			{ "CL", "Chile" }, { "CM", "Cameroon" }, { "CN", "China" },
			{ "CO", "Colombia" }, { "CP", "Clipperton Island" },
			{ "CR", "Costa Rica" }, { "CS", "Czechoslovakia" },
			{ "CT", "Canton and Enderbury Islands" }, { "CU", "Cuba" },
			{ "CV", "Cape Verde" }, { "CW", "Curaçao" },
			{ "CX", "Christmas Island" }, { "CY", "Cyprus" },
			{ "CZ", "Czech Republic" }, { "DD", "German Democratic Republic" },
			{ "DE", "Germany" }, { "DG", "Diego Garcia" },
			{ "DJ", "Djibouti" }, { "DK", "Denmark" }, { "DM", "Dominica" },
			{ "DO", "Dominican Republic" }, { "DY", "Dahomey" },
			{ "DZ", "Algeria" }, { "EA", "Ceuta" }, { "EC", "Ecuador" },
			{ "EE", "Estonia" }, { "EG", "Egypt" }, { "EH", "Western Sahara" },
			{ "ER", "Eritrea" }, { "ES", "Spain" }, { "ET", "Ethiopia" },
			{ "EU", "European Union" }, { "EW", "Estonia" },
			{ "FI", "Finland" }, { "FJ", "Fiji" },
			{ "FK", "Falkland Islands (Malvinas)" }, { "FL", "Liechtenstein" },
			{ "FM", "Micronesia" }, { "FO", "Faroe Islands" },
			{ "FQ", "Southern French Territories" }, { "FR", "France" },
			{ "FX", "Metropolitan France" }, { "GA", "Gabon" },
			{ "GB", "United Kingdom" }, { "GD", "Grenada" },
			{ "GE", "Georgia" }, { "GF", "French Guiana" },
			{ "GG", "Guernsey" }, { "GH", "Ghana" }, { "GI", "Gibraltar" },
			{ "GL", "Greenland" }, { "GM", "Gambia" }, { "GN", "Guinea" },
			{ "GP", "Guadeloupe" }, { "GQ", "Equatorial Guinea" },
			{ "GR", "Greece" },
			{ "GS", "South Georgia and the South Sandwich Islands" },
			{ "GT", "Guatemala" }, { "GU", "Guam" }, { "GW", "Guinea-Bissau" },
			{ "GY", "Guyana" }, { "HK", "Hong Kong" },
			{ "HM", "Heard Island and McDonald Islands" },
			{ "HN", "Honduras" }, { "HR", "Croatia" }, { "HT", "Haiti" },
			{ "HU", "Hungary" }, { "HV", "Upper Volta" },
			{ "IC", "Canary Islands" }, { "ID", "Indonesia" },
			{ "IE", "Ireland" }, { "IL", "Israel" }, { "IM", "Isle of Man" },
			{ "IN", "India" }, { "IO", "British Indian Ocean Territory" },
			{ "IQ", "Iraq" }, { "IR", "Iran" }, { "IS", "Iceland" },
			{ "IT", "Italy" }, { "JA", "Jamaica" }, { "JE", "Jersey" },
			{ "JM", "Jamaica" }, { "JO", "Jordan" }, { "JP", "Japan" },
			{ "JT", "Johnston Island" }, { "KE", "Kenya" },
			{ "KG", "Kyrgyzstan" }, { "KH", "Cambodia" }, { "KI", "Kiribati" },
			{ "KM", "Comoros" }, { "KN", "Saint Kitts and Nevis" },
			{ "KP", "Korea" }, { "KR", "Korea" }, { "KW", "Kuwait" },
			{ "KY", "Cayman Islands" }, { "KZ", "Kazakhstan" },
			{ "LA", "Lao People's Democratic Republic" }, { "LB", "Lebanon" },
			{ "LC", "Saint Lucia" }, { "LF", "Libya Fezzan" },
			{ "LI", "Liechtenstein" }, { "LK", "Sri Lanka" },
			{ "LR", "Liberia" }, { "LS", "Lesotho" }, { "LT", "Lithuania" },
			{ "LU", "Luxembourg" }, { "LV", "Latvia" }, { "LY", "Libya" },
			{ "MA", "Morocco" }, { "MC", "Monaco" }, { "MD", "Moldova" },
			{ "ME", "Montenegro" }, { "MF", "Saint Martin (French part)" },
			{ "MG", "Madagascar" }, { "MH", "Marshall Islands" },
			{ "MI", "Midway Islands" }, { "MK", "Macedonia" },
			{ "ML", "Mali" }, { "MM", "Myanmar" }, { "MN", "Mongolia" },
			{ "MO", "Macao" }, { "MP", "Northern Mariana Islands" },
			{ "MQ", "Martinique" }, { "MR", "Mauritania" },
			{ "MS", "Montserrat" }, { "MT", "Malta" }, { "MU", "Mauritius" },
			{ "MV", "Maldives" }, { "MW", "Malawi" }, { "MX", "Mexico" },
			{ "MY", "Malaysia" }, { "MZ", "Mozambique" }, { "NA", "Namibia" },
			{ "NC", "New Caledonia" }, { "NE", "Niger" },
			{ "NF", "Norfolk Island" }, { "NG", "Nigeria" },
			{ "NH", "New Hebrides" }, { "NI", "Nicaragua" },
			{ "NL", "Netherlands" }, { "NO", "Norway" }, { "NP", "Nepal" },
			{ "NQ", "Dronning Maud Land" }, { "NR", "Nauru" },
			{ "NT", "Neutral Zone" }, { "NU", "Niue" },
			{ "NZ", "New Zealand" }, { "OM", "Oman" }, { "PA", "Panama" },
			{ "PC", "Pacific Islands" }, { "PE", "Peru" },
			{ "PF", "French Polynesia" }, { "PG", "Papua New Guinea" },
			{ "PH", "Philippines" }, { "PI", "Philippines" },
			{ "PK", "Pakistan" }, { "PL", "Poland" },
			{ "PM", "Saint Pierre and Miquelon" }, { "PN", "Pitcairn" },
			{ "PR", "Puerto Rico" }, { "PS", "Palestinian Territory" },
			{ "PT", "Portugal" },
			{ "PU", "U.S. Miscellaneous Pacific Islands" }, { "PW", "Palau" },
			{ "PY", "Paraguay" }, { "PZ", "Panama Canal Zone" },
			{ "QA", "Qatar" }, { "RA", "Argentina" }, { "RB", "Botswana" },
			{ "RC", "China" }, { "RE", "Reunion" }, { "RH", "Haiti" },
			{ "RI", "Indonesia" }, { "RL", "Lebanon" }, { "RM", "Madagascar" },
			{ "RN", "Niger" }, { "RO", "Romania" }, { "RP", "Philippines" },
			{ "RS", "Serbia" }, { "RU", "Russian Federation" },
			{ "RW", "Rwanda" }, { "SA", "Saudi Arabia" },
			{ "SB", "Solomon Islands" }, { "SC", "Seychelles" },
			{ "SD", "Sudan" }, { "SE", "Sweden" }, { "SF", "Finland" },
			{ "SG", "Singapore" }, { "SH", "Saint Helena" },
			{ "SI", "Slovenia" }, { "SJ", "Svalbard and Jan Mayen" },
			{ "SK", "Slovakia" }, { "SL", "Sierra Leone" },
			{ "SM", "San Marino" }, { "SN", "Senegal" }, { "SO", "Somalia" },
			{ "SR", "Suriname" }, { "SS", "South Sudan" },
			{ "ST", "Sao Tome and Principe" }, { "SU", "USSR" },
			{ "SV", "El Salvador" }, { "SX", "Sint Maarten (Dutch part)" },
			{ "SY", "Syrian Arab Republic" }, { "SZ", "Swaziland" },
			{ "TA", "Tristan da Cunha" }, { "TC", "Turks and Caicos Islands" },
			{ "TD", "Chad" }, { "TF", "French Southern Territories" },
			{ "TG", "Togo" }, { "TH", "Thailand" }, { "TJ", "Tajikistan" },
			{ "TK", "Tokelau" }, { "TL", "Timor-Leste" },
			{ "TM", "Turkmenistan" }, { "TN", "Tunisia" }, { "TO", "Tonga" },
			{ "TP", "East Timor" }, { "TR", "Turkey" },
			{ "TT", "Trinidad and Tobago" }, { "TV", "Tuvalu" },
			{ "TW", "Taiwan" }, { "TZ", "Tanzania" }, { "UA", "Ukraine" },
			{ "UG", "Uganda" }, { "UK", "United Kingdom" },
			{ "UM", "United States Minor Outlying Islands" },
			{ "US", "United States" }, { "UY", "Uruguay" },
			{ "UZ", "Uzbekistan" }, { "VA", "Holy See (Vatican City State)" },
			{ "VC", "Saint Vincent and the Grenadines" }, { "VD", "Vietnam" },
			{ "VE", "Venezuela" }, { "VG", "British Virgin Islands" },
			{ "VI", "U.S. Virgin Islands" }, { "VN", "Viet Nam" },
			{ "VU", "Vanuatu" }, { "WF", "Wallis and Futuna" },
			{ "WG", "Grenada" }, { "WK", "Wake Island" },
			{ "WL", "Saint Lucia" }, { "WS", "Samoa" },
			{ "WV", "Saint Vincent" }, { "YD", "Yemen" }, { "YE", "Yemen" },
			{ "YT", "Mayotte" }, { "YU", "Yugoslavia" }, { "YV", "Venezuela" },
			{ "ZA", "South Africa" }, { "ZM", "Zambia" }, { "ZR", "Zaire" },
			{ "ZW", "Zimbabwe" } };

	public static final Map<String, Integer> currencies_map = new TreeMap<String, Integer>();

	public static final Map<String, String> country_codes = new TreeMap<String, String>();

	public static void init_currencies() {
		Map<String, Integer> dir = currencies_map;
		for (int aa = 0; aa < iso.currencies_array.length; aa++) {
			String kk = iso.currencies_array[aa][0];
			Integer val = aa;
			if (!dir.containsKey(kk)) {
				dir.put(kk, val);
			}
		}
	}

	public static void init_country_codes() {
		Map<String, String> dir = country_codes;
		for (int aa = 0; aa < iso.country_codes_array.length; aa++) {
			assert (iso.country_codes_array[aa].length == 2);
			String kk = iso.country_codes_array[aa][0];
			String val = iso.country_codes_array[aa][1];
			if (!dir.containsKey(kk)) {
				dir.put(kk, val);
			}
		}
	}

	public static int get_currency_idx(String code) {
		if (currencies_map.containsKey(code)) {
			return currencies_map.get(code);
		}
		return -1;
	}

	public static boolean is_valid_currency_idx(int curr_idx) {
		boolean c1 = (curr_idx >= 0);
		boolean c2 = (curr_idx < iso.currencies_array.length);
		return (c1 && c2);
	}

	public static String get_currency_name(int curr_idx) {
		if (!is_valid_currency_idx(curr_idx)) {
			throw new bad_passet(2);
		}
		String nm = iso.currencies_array[curr_idx][1];
		return nm;
	}

	public static String get_currency_code(int curr_idx) {
		if (!is_valid_currency_idx(curr_idx)) {
			throw new bad_passet(2);
		}
		String nm = iso.currencies_array[curr_idx][0];
		return nm;
	}

	public static boolean is_currency_code(String cc) {
		return (get_currency_idx(cc) != -1);
	}

	public static boolean is_valid_country_idx(int cntry_idx) {
		boolean c1 = (cntry_idx >= 0);
		boolean c2 = (cntry_idx < iso.country_codes_array.length);
		return (c1 && c2);
	}

	public static boolean is_country_code(String cc) {
		return country_codes.containsKey(cc);
	}

	public static String get_country_code(int curr_idx) {
		if (!is_valid_country_idx(curr_idx)) {
			throw new bad_passet(2);
		}
		String nm = iso.country_codes_array[curr_idx][0];
		return nm;
	}

	public static void print_currencies(PrintStream os) {
		String[][] arr = iso.currencies_array;
		for (int aa = 0; aa < arr.length; aa++) {
			String[] curr = arr[aa];
			String cod = curr[0];
			String nam = curr[1];
			os.println(cod + " for " + nam);
		}
	}

	public static void print_countries(PrintStream os) {
		String[][] arr = iso.country_codes_array;
		for (int aa = 0; aa < arr.length; aa++) {
			String[] cntry = arr[aa];
			String cod = cntry[0];
			String nam = cntry[1];
			os.println(cod + " for " + nam);
		}
	}

	static {
		init_country_codes();
		init_currencies();
	};

}
