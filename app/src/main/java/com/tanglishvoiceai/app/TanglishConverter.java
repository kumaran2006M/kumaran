package com.tanglishvoiceai.app;

import java.util.HashMap;
import java.util.Map;

/** Simple offline script-to-Latin conversion. This is NOT AI translation.
 * It can produce awkward spellings; a later release can add a language model. */
public final class TanglishConverter {
    private static final Map<Character,String> BASE = new HashMap<>();
    private static final Map<Character,String> SIGNS = new HashMap<>();
    private static final Map<String,String> COMMON = new HashMap<>();
    static {
        String[][] bases = {
            {"அ","a"},{"ஆ","aa"},{"இ","i"},{"ஈ","ee"},{"உ","u"},{"ஊ","oo"},
            {"எ","e"},{"ஏ","ae"},{"ஐ","ai"},{"ஒ","o"},{"ஓ","oh"},{"ஔ","au"},
            {"க","k"},{"ங","ng"},{"ச","s"},{"ஞ","nj"},{"ட","t"},{"ண","n"},
            {"த","th"},{"ந","n"},{"ப","p"},{"ம","m"},{"ய","y"},{"ர","r"},
            {"ல","l"},{"வ","v"},{"ழ","zh"},{"ள","l"},{"ற","r"},{"ன","n"},
            {"ஜ","j"},{"ஷ","sh"},{"ஸ","s"},{"ஹ","h"},{"ஃ","h"}
        };
        for (String[] p : bases) BASE.put(p[0].charAt(0), p[1]);
        String[][] signs = {
            {"ா","aa"},{"ி","i"},{"ீ","ee"},{"ு","u"},{"ூ","oo"},
            {"ெ","e"},{"ே","ae"},{"ை","ai"},{"ொ","o"},{"ோ","oh"},{"ௌ","au"}
        };
        for (String[] p : signs) SIGNS.put(p[0].charAt(0), p[1]);
        String[][] common = {
            {"எப்படி","eppadi"},{"இப்போ","ippo"},{"இப்போது","ippo"},
            {"நான்","naan"},{"நீ","nee"},{"நீங்க","neenga"},{"என்ன","enna"},
            {"ஏன்","yen"},{"சரி","seri"},{"இருக்கு","irukku"},
            {"இருக்கா","irukka"},{"இருக்கேன்","irukken"},{"இருக்கீங்க","irukeenga"},
            {"மா","ma"},{"வா","vaa"},{"போ","po"},{"போனேன்","ponen"},
            {"சாப்பிட்டியா","saaptiya"},{"சாப்பிட்டீங்களா","saapteengala"},
            {"குடிச்சியா","kudichiya"},{"ரொம்ப","romba"},{"கொஞ்சம்","konjam"},
            {"கால்","call"},{"பண்ணு","pannu"},{"பண்ணேன்","pannen"},
            {"இல்ல","illa"},{"இல்லை","illai"},{"ஆமா","aama"},
            {"நல்லா","nalla"},{"வணக்கம்","vanakkam"}
        };
        for (String[] p : common) COMMON.put(p[0], p[1]);
    }
    private TanglishConverter() {}

    public static String convert(String source) {
        if (source == null || source.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (Character.isWhitespace(ch) || isPunctuation(ch)) {
                flushWord(token, out);
                out.append(ch);
            } else token.append(ch);
        }
        flushWord(token, out);
        return out.toString();
    }

    private static boolean isPunctuation(char c) {
        return ",.!?;:\"'()[]{}-–—/\\\n\r।".indexOf(c) != -1;
    }
    private static void flushWord(StringBuilder token, StringBuilder result) {
        if (token.length() == 0) return;
        String word = token.toString();
        result.append(COMMON.containsKey(word) ? COMMON.get(word) : transliterate(word));
        token.setLength(0);
    }
    private static String transliterate(String word) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            String mapped = BASE.get(c);
            if (mapped == null) {
                if (c == '்') continue;
                String sign = SIGNS.get(c);
                if (sign != null) out.append(sign);
                else out.append(c);
                continue;
            }
            if (c >= 'அ' && c <= 'ஔ') { out.append(mapped); continue; }
            // Look ahead for a Tamil dependent vowel or the pulli (virama).
            if (i + 1 < word.length()) {
                char next = word.charAt(i + 1);
                if (next == '்') { out.append(mapped); i++; continue; }
                String vowel = SIGNS.get(next);
                if (vowel != null) { out.append(mapped).append(vowel); i++; continue; }
            }
            out.append(mapped).append('a');
        }
        return out.toString();
    }
}
