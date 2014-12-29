package org.nick.wwwjdic.krad;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import android.util.Log;

public class KradDb {
	private final static boolean D = false;
    private static final String TAG = "KradDb";

    private Map<String, Set<String>> radicalToKanjis = new TreeMap<String, Set<String>>();
    private Map<String, Set<String>> kanjiToRadicals = new TreeMap<String, Set<String>>();

    public void readLine(String line) {
    	if (line != null) {
	        String[] fields = line.split(":");
	        if (fields.length < 3) {
	            return;
	        }
	        String radical = fields[0];
	        int len = fields[2].length();
	        if (D) {
	        	Log.d(TAG, "radical = " + radical + " fields[2].length == " +  len);
	        }
	        Set<String> kanjis = new TreeSet<String>();
	        for (int i = 0; i < len; i++) {
	        	String c = fields[2].substring(i, i + 1);
	            kanjis.add(c);
	            Set<String> radicals = kanjiToRadicals.get(c);
	            if (radicals == null) {
	                radicals = new TreeSet<String>();
	                kanjiToRadicals.put(c, radicals);
	            }
	            radicals.add(radical);
	        }
	        radicalToKanjis.put(radical, kanjis);
	        if (D) {
	            Log.d(TAG, String.format("loaded %d radicals, %d kanji",
	            	radicalToKanjis.size(), kanjiToRadicals.size()));
		    }
    	}
    }

    public Set<String> getKanjiForRadical(String radical) {
        return radicalToKanjis.get(radical);
    }

    public Set<String> getKanjisForRadicals(Set<String> radicals) {
        Set<String> result = new TreeSet<String>();
        for (String radical : radicals) {
            Set<String> kanjis = getKanjiForRadical(radical);
        	if (kanjis != null) {
	            if (result.isEmpty()) {
	            	result.addAll(kanjis);
	            } else {
	            	result.retainAll(kanjis);
	            }
        	}
        }
        return result;
    }

    public Set<String> getRadicalsForKanji(String kanji) {
        return kanjiToRadicals.get(kanji);
    }

    public Set<String> getRadicalsForKanjis(Set<String> kanjis) {
        Set<String> result = new TreeSet<String>();
        for (String kanji : kanjis) {
            Set<String> radicals = getRadicalsForKanji(kanji);
            result.addAll(radicals);
        }
        return result;
    }
}
