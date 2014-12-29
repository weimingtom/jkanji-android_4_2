package com.iteye.weimingtom.jkanji;

/**
 * FIXME: 缺少重复符和破折号
 * @author Administrator
 *
 */
public class JapaneseKeyboard {
	private final static char[][] SoftKeyboard = new char[][] {
		{'あ', 'い', 'う', 'え', 'お'},
		{'か', 'き', 'く', 'け', 'こ'},
		{'さ', 'し', 'す', 'せ', 'そ'},
		{'た', 'ち', 'つ', 'て', 'と'},
		{'な', 'に', 'ぬ', 'ね', 'の'},
		{'は', 'ひ', 'ふ', 'へ', 'ほ'},
		{'ま', 'み', 'む', 'め', 'も'},
		{'や', '\0', 'ゆ', '\0', 'よ'},
		{'ら', 'り', 'る', 'れ', 'ろ'},
		{'わ', 'ゐ', '\0', 'ゑ', 'を'},
		{'ー', '々', '°', '\0', 'ん'},
		//
		{'が', 'ぎ', 'ぐ', 'げ', 'ご'},
		{'ざ', 'じ', 'ず', 'ぜ', 'ぞ'},
		{'だ', 'ぢ', 'づ', 'で', 'ど'},
		{'ば', 'び', 'ぶ', 'べ', 'ぼ'},
		//
		{'ぱ', 'ぴ', 'ぷ', 'ぺ', 'ぽ'},
		//
		{'ぁ', 'ぃ', 'ぅ', 'ぇ', 'ぉ'},
		{'\0', '\0', 'っ', '\0', '\0'},
		{'ゃ', '\0', 'ゅ', '\0', 'ょ'},
		{'ゎ', '\0', '\0', '\0', '\0'},
		//
		{'ヴ', 'ヵ', 'ヶ', '\0', '\0'},
	};
	
	private static String[][] EnglishKeyboard = new String[][] {
		{"ぁ", "la"}, {"ぁ", "xa"}, {"あ", "a"}, {"ぃ", "li"}, {"ぃ", "lyi"},
		{"ぃ", "xi"}, {"ぃ", "xyi"}, {"い", "i"}, {"い", "yi"}, {"いぇ", "ye"},
		{"ぅ", "lu"}, {"ぅ", "xu"}, {"う", "u"}, {"う", "whu"}, {"う", "wu"},
		{"うぁ", "wha"}, {"うぃ", "whi"}, {"うぃ", "wi"}, {"うぇ", "we"}, {"うぇ", "whe"},
		//
		{"うぉ", "who"}, {"ぇ", "le"}, {"ぇ", "lye"}, {"ぇ", "xe"}, {"ぇ", "xye"},
		{"え", "e"}, {"ぉ", "lo"}, {"ぉ", "xo"}, {"お", "o"}, {"か", "ca"},
		{"か", "ka"}, {"が", "ga"}, {"き", "ki"}, {"きぃ", "kyi"}, {"きぇ", "kye"},
		{"きゃ", "kya"}, {"きゅ", "kyu"}, {"きょ", "kyo"}, {"ぎ", "gi"}, {"ぎぃ", "gyi"},
		//
		{"ぎぇ", "gye"}, {"ぎゃ", "gya"}, {"ぎゅ", "gyu"}, {"ぎょ", "gyo"}, {"く", "cu"},
		{"く", "ku"}, {"く", "qu"}, {"くぁ", "kwa"}, {"くぁ", "qa"}, {"くぁ", "qwa"},
		{"くぃ", "qi"}, {"くぃ", "qwi"}, {"くぃ", "qyi"}, {"くぅ", "qwu"}, {"くぇ", "qe"},
		{"くぇ", "qwe"}, {"くぇ", "qye"}, {"くぉ", "qo"}, {"くぉ", "qwo"}, {"くゃ", "qya"},
		//
		{"くゅ", "qyu"}, {"くょ", "qyo"}, {"ぐ", "gu"}, {"ぐぁ", "gwa"}, {"ぐぃ", "gwi"},
		{"ぐぅ", "gwu"}, {"ぐぇ", "gwe"}, {"ぐぉ", "gwo"}, {"け", "ke"}, {"げ", "ge"},
		{"こ", "co"}, {"こ", "ko"}, {"ご", "go"}, {"さ", "sa"}, {"ざ", "za"},
		{"し", "ci"}, {"し", "shi"}, {"し", "si"}, {"しぃ", "syi"}, {"しぇ", "she"},
		//
		{"しぇ","sye"}, {"しゃ","sha"}, {"しゃ","sya"}, {"しゅ","shu"}, {"しゅ","syu"},
		{"しょ","sho"}, {"しょ","syo"}, {"じ","ji"}, {"じ","zi"}, {"じぃ","jyi"},
		{"じぃ","zyi"}, {"じぇ","je"}, {"じぇ","jye"}, {"じぇ","zye"}, {"じゃ","ja"},
		{"じゃ","jya"}, {"じゃ","zya"}, {"じゅ","ju"}, {"じゅ","jyu"}, {"じゅ","zyu"},
		//
		{"じょ","jo"}, {"じょ","jyo"}, {"じょ","zyo"}, {"す","su"}, {"すぁ","swa"},
		{"すぃ","swi"}, {"すぅ","swu"}, {"すぇ","swe"}, {"すぉ","swo"}, {"ず","zu"},
		{"せ","ce"}, {"せ","se"}, {"ぜ","ze"}, {"そ","so"}, {"ぞ","zo"},
		{"た","ta"}, {"だ","da"}, {"ち","chi"}, {"ち","ti"}, {"ちぃ","cyi"},
		//
		{"ちぃ","tyi"}, {"ちぇ","che"}, {"ちぇ","cye"}, {"ちぇ","tye"}, {"ちゃ","cha"},
		{"ちゃ","cya"}, {"ちゃ","tya"}, {"ちゅ","chu"}, {"ちゅ","cyu"}, {"ちゅ","tyu"},
		{"ちょ","cho"}, {"ちょ","cyo"}, {"ちょ","tyo"}, {"ぢ","di"}, {"ぢぃ","dyi"},
		{"ぢぇ","dye"}, {"ぢゃ","dya"}, {"ぢゅ","dyu"}, {"ぢょ","dyo"}, {"っ","ltsu"},
		//
		{"っ","ltu"}, {"っ","xtu"}, {"つ","tsu"}, {"つ","tu"}, {"つぁ","tsa"},
		{"つぃ","tsi"}, {"つぇ","tse"}, {"つぉ","tso"}, {"づ","du"}, {"て","te"},
		{"てぃ","thi"}, {"てぇ","the"}, {"てゃ","tha"}, {"てゅ","thu"}, {"てょ","tho"},
		{"で","de"}, {"でぃ","dhi"}, {"でぇ","dhe"}, {"でゃ","dha"}, {"でゅ","dhu"},
		//
		{"でょ","dho"}, {"と","to"}, {"とぁ","twa"}, {"とぃ","twi"}, {"とぅ","twu"},
		{"とぇ","twe"}, {"とぉ","two"}, {"ど","do"}, {"どぁ","dwa"}, {"どぃ","dwi"},
		{"どぅ","dwu"}, {"どぇ","dwe"}, {"どぉ","dwo"}, {"な","na"}, {"に","ni"}, 
		{"にぇ","nye"}, {"にぃ","nyi"}, {"にゃ","nya"}, {"にゅ","nyu"}, {"にょ","nyo"},
		//
		{"ぬ","nu"}, {"ね","ne"}, {"の","no"}, {"は","ha"}, {"ば","ba"}, 
		{"ぱ","pa"}, {"ひ","hi"}, {"ひぃ","hyi"}, {"ひぇ","hye"}, {"ひゃ","hya"},
		{"ひゅ","hyu"}, {"ひょ","hyo"}, {"び","bi"}, {"びぃ","byi"}, {"びぇ","bye"},
		{"びゃ","bya"}, {"びゅ","byu"}, {"びょ","byo"}, {"ぴ","pi"}, {"ぴぃ","pyi"},
		//
		{"ぴぇ","pye"}, {"ぴゃ","pya"}, {"ぴゅ","pyu"}, {"ぴょ","pyo"}, {"ふ","fu"},
		{"ふ","hu"}, {"ふぁ","fa"}, {"ふぁ","fwa"}, {"ふぃ","fi"}, {"ふぃ","fwi"},
		{"ふぃ","fyi"}, {"ふぅ","fwu"}, {"ふぇ","fe"}, {"ふぇ","fwe"}, {"ふぇ","fye"},
		{"ふぉ","fo"}, {"ふぉ","fwo"}, {"ふゃ","fya"}, {"ふゅ","fyu"}, {"ふょ","fyo"},
		//
		{"ぶ","bu"}, {"ぷ","pu"}, {"へ","he"}, {"べ","be"}, {"ぺ","pe"},
		{"ほ","ho"}, {"ぼ","bo"}, {"ぽ","po"}, {"ま","ma"}, {"み","mi"},
		{"みぃ","myi"}, {"みぇ","mye"}, {"みゃ","mya"}, {"みゅ","myu"}, {"みょ","myo"},
		{"む","mu"}, {"め","me"}, {"も","mo"}, {"ゃ","lya"}, {"ゃ","xya"},
		//
		{"や","ya"}, {"ゅ","lyu"}, {"ゅ","xyu"}, {"ゆ","yu"}, {"ょ","lyo"},
		{"ょ","xyo"}, {"よ","yo"}, {"ら","ra"}, {"り","ri"}, {"りぃ","ryi"},
		{"りぇ","rye"}, {"りゃ","rya"}, {"りゅ","ryu"}, {"りょ","ryo"}, {"る","ru"},
		{"れ","re"}, {"ろ","ro"}, {"ゎ","lwa"}, {"ゎ","xwa"}, {"わ","wa"},
		//
		{"を","wo"}, {"ん","nn"}, {"ん","xn"}, {"ヴ","vu"}, {"ヴぁ","va"},
		{"ヴぃ","vi"}, {"ヴぃ","vyi"}, {"ヴぇ","ve"}, {"ヴぇ","vye"}, {"ヴぉ","vo"},
		{"ヴゃ","vya"}, {"ヴゅ","vyu"}, {"ヴょ","vyo"}, {"ヵ","lka"}, {"ヵ","xka"},
		{"ヶ","lke"}, {"ヶ","xke"},
	};
	
	public static String[] getSoftKeyboard() {
		String[] result = new String[SoftKeyboard.length * 5];
		for (int i = 0; i < SoftKeyboard.length; i++) {
			for (int j = 0; j < 5; j++) {
				if (SoftKeyboard[i][j] == '\0') {
					result[i * 5 + j] = "";
				} else {
					result[i * 5 + j] = Character.toString(SoftKeyboard[i][j]);
				}
			}
		}
		return result;
	}
	
	public static String[][] getEnglishKeyboard() {
		return EnglishKeyboard;
	}
	
	private final static String[] NEXT_KEYS = {
		"あぁアァ",
		"いぃイィ",
		"うぅウゥ",
		"えぇエェ",
		"おぉオォ",
		
		"かがカガ",
		"きぎキギ",
		"くぐクグ",
		"けげケゲ",
		"こごコゴ",
		
		"さざサザ",
		"しじシジ",
		"すずスズ",
		"せぜセゼ",
		"そぞソゾ",
		
		"ただタダ",
		"ちぢチヂ",
		"つづっツヅッ",
		"てでテデ",
		"とどトド",
		
		"なナ",
		"にニ",
		"ぬヌ",
		"ねネ",
		"のノ",
		
		"はばぱハバパ",
		"ひびぴヒビピ",
		"ふぶぷフブプ",
		"へべぺヘベペ",
		"ほぼぽホボポ",
		
		"まマ",
		"みミ",
		"むム",
		"めメ",
		"もモ",
		
		"やゃヤャ",
		"ゆゅユュ",
		"よょヨョ",
		
		"らラ",
		"りリ",
		"るル",
		"れレ",
		"ろロ",
		
		"わゎワヮ",
		"ゐヰ",
		"ゑヱ",
		"をヲ",
		"んンー々",
	};
	
	public static String nextChar(String str, int sepIndex) {
		if (str == null || str.length() <= 0 || 
			sepIndex < 0 || sepIndex >= str.length()) {
			return null;
		}
		String right = Character.toString(str.charAt(sepIndex));
		for (int i = 0; i < NEXT_KEYS.length; i++) {
			String list = NEXT_KEYS[i];
			int listLength = list.length();
			int index = list.indexOf(right);
			if (index >= 0) {
				int newIndex = (index + 1) % listLength;
				return Character.toString(list.charAt(newIndex));
			}
		}
		return null;
	}
	
	public static String getAllTransformChars(String str) {
		if (str == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			String strC = Character.toString(ch);
			if (sb.toString().indexOf(strC) < 0) {
				for (int j = 0; j < NEXT_KEYS.length; j++) {
					String keys = NEXT_KEYS[j];
					if (keys.indexOf(strC) >= 0) {
						sb.append(keys);
					}
				}
			}
		}
		return sb.toString();
	}
	
	public static String getAllJapChars() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < NEXT_KEYS.length; i++) {
			String keys = NEXT_KEYS[i];
			sb.append(keys);
		}
		return sb.toString();
	}
}
