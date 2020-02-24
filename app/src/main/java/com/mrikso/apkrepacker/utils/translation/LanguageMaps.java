package com.mrikso.apkrepacker.utils.translation;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class LanguageMaps {
    private static LinkedHashMap langMap;

    static {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        langMap = linkedHashMap;
        linkedHashMap.put("-aa", "Afar");
        langMap.put("-ab", "Abkhazian");
        langMap.put("-af", "Afrikaans");
        langMap.put("-ak", "Akan");
        langMap.put("-sq", "Albanian");
        langMap.put("-am", "Amharic");
        langMap.put("-ar", "Arabic");
        langMap.put("-an", "Aragonese");
        langMap.put("-hy", "Armenian");
        langMap.put("-as", "Assamese");
        langMap.put("-av", "Avaric");
        langMap.put("-ae", "Avestan");
        langMap.put("-ay", "Aymara");
        langMap.put("-az", "Azerbaijani");
        langMap.put("-ba", "Bashkir");
        langMap.put("-bm", "Bambara");
        langMap.put("-eu", "Basque");
        langMap.put("-be", "Belarusian");
        langMap.put("-bn", "Bengali");
        langMap.put("-bh", "Bihari languages+B372");
        langMap.put("-bi", "Bislama");
        langMap.put("-bs", "Bosnian");
        langMap.put("-br", "Breton");
        langMap.put("-bg", "Bulgarian");
        langMap.put("-my", "Burmese");
        langMap.put("-ca", "Catalan; Valencian");
        langMap.put("-cs", "Czech");
        langMap.put("-ch", "Chamorro");
        langMap.put("-ce", "Chechen");
        langMap.put("-zh", "Chinese");
        langMap.put("cu", "Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic");
        langMap.put("-cv", "Chuvash");
        langMap.put("-kw", "Cornish");
        langMap.put("-co", "Corsican");
        langMap.put("-cr", "Cree");
        langMap.put("-da", "Danish");
        langMap.put("-dv", "Divehi; Dhivehi; Maldivian");
        langMap.put("-nl", "Dutch; Flemish");
        langMap.put("-dz", "Dzongkha");
        langMap.put("-el", "Greek, Modern (1453-)");
        langMap.put("-en", "English");
        langMap.put("-eo", "Esperanto");
        langMap.put("-et", "Estonian");
        langMap.put("-ee", "Ewe");
        langMap.put("-fo", "Faroese");
        langMap.put("-fj", "Fijian");
        langMap.put("-fi", "Finnish");
        langMap.put("-fr", "French");
        langMap.put("-fy", "Western Frisian");
        langMap.put("-ff", "Fulah");
        langMap.put("-ka", "Georgian");
        langMap.put("-de", "German");
        langMap.put("-gd", "Gaelic; Scottish Gaelic");
        langMap.put("-ga", "Irish");
        langMap.put("-gl", "Galician");
        langMap.put("-gv", "Manx");
        langMap.put("-gn", "Guarani");
        langMap.put("-gu", "Gujarati");
        langMap.put("-ht", "Haitian; Haitian Creole");
        langMap.put("-ha", "Hausa");
        langMap.put("-iw", "Hebrew");
        langMap.put("-he", "Hebrew");
        langMap.put("-hz", "Herero");
        langMap.put("-hi", "Hindi");
        langMap.put("-ho", "Hiri Motu");
        langMap.put("-hr", "Croatian");
        langMap.put("-hu", "Hungarian");
        langMap.put("-ig", "Igbo");
        langMap.put("-is", "Icelandic");
        langMap.put("-io", "Ido");
        langMap.put("-ii", "Sichuan Yi; Nuosu");
        langMap.put("-iu", "Inuktitut");
        langMap.put("-ie", "Interlingue; Occidental");
        langMap.put("-ia", "Interlingua (International Auxiliary Language Association)");
        langMap.put("-in", "Indonesian");
        langMap.put("-id", "Indonesian");
        langMap.put("-ik", "Inupiaq");
        langMap.put("-it", "Italian");
        langMap.put("-jv", "Javanese");
        langMap.put("-ja", "Japanese");
        langMap.put("-kl", "Kalaallisut; Greenlandic");
        langMap.put("-kn", "Kannada");
        langMap.put("-ks", "Kashmiri");
        langMap.put("-kr", "Kanuri");
        langMap.put("-kk", "Kazakh");
        langMap.put("-km", "Central Khmer");
        langMap.put("-ki", "Kikuyu; Gikuyu");
        langMap.put("-rw", "Kinyarwanda");
        langMap.put("-ky", "Kirghiz; Kyrgyz");
        langMap.put("-kv", "Komi");
        langMap.put("-kg", "Kongo");
        langMap.put("-ko", "Korean");
        langMap.put("-kj", "Kuanyama; Kwanyama");
        langMap.put("-ku", "Kurdish");
        langMap.put("-lo", "Lao");
        langMap.put("-la", "Latin");
        langMap.put("-lv", "Latvian");
        langMap.put("-li", "Limburgan; Limburger; Limburgish");
        langMap.put("-ln", "Lingala");
        langMap.put("-lt", "Lithuanian");
        langMap.put("-lb", "Luxembourgish; Letzeburgesch");
        langMap.put("-lu", "Luba-Katanga");
        langMap.put("-lg", "Ganda");
        langMap.put("-mk", "Macedonian");
        langMap.put("-mh", "Marshallese");
        langMap.put("-ml", "Malayalam");
        langMap.put("-mi", "Maori");
        langMap.put("-mr", "Marathi");
        langMap.put("-ms", "Malay");
        langMap.put("-mg", "Malagasy");
        langMap.put("-mt", "Maltese");
        langMap.put("-mn", "Mongolian");
        langMap.put("-na", "Nauru");
        langMap.put("-nv", "Navajo; Navaho");
        langMap.put("-nr", "Ndebele, South; South Ndebele");
        langMap.put("-nd", "Ndebele, North; North Ndebele");
        langMap.put("-ng", "Ndonga");
        langMap.put("-ne", "Nepali");
        langMap.put("-nn", "Norwegian Nynorsk; Nynorsk, Norwegian");
        langMap.put("-nb", "Bokmål, Norwegian; Norwegian Bokmål");
        langMap.put("-no", "Norwegian");
        langMap.put("-ny", "Chichewa; Chewa; Nyanja");
        langMap.put("-oc", "Occitan (post 1500)");
        langMap.put("-oj", "Ojibwa");
        langMap.put("-or", "Oriya");
        langMap.put("-om", "Oromo");
        langMap.put("-os", "Ossetian; Ossetic");
        langMap.put("-pa", "Panjabi; Punjabi");
        langMap.put("-fa", "Persian");
        langMap.put("-pi", "Pali");
        langMap.put("-pl", "Polish");
        langMap.put("-pt", "Portuguese");
        langMap.put("-ps", "Pushto; Pashto");
        langMap.put("-qu", "Quechua");
        langMap.put("-rm", "Romansh");
        langMap.put("-ro", "Romanian; Moldavian; Moldovan");
        langMap.put("-rn", "Rundi");
        langMap.put("-ru", "Russian");
        langMap.put("-sg", "Sango");
        langMap.put("-sa", "Sanskrit");
        langMap.put("-si", "Sinhala; Sinhalese");
        langMap.put("-sk", "Slovak");
        langMap.put("-sl", "Slovenian");
        langMap.put("-se", "Northern Sami");
        langMap.put("-sm", "Samoan");
        langMap.put("-sn", "Shona");
        langMap.put("-sd", "Sindhi");
        langMap.put("-so", "Somali");
        langMap.put("-st", "Sotho, Southern");
        langMap.put("-es", "Spanish; Castilian");
        langMap.put("-sc", "Sardinian");
        langMap.put("-sr", "Serbian");
        langMap.put("-ss", "Swati");
        langMap.put("-su", "Sundanese");
        langMap.put("-sw", "Swahili");
        langMap.put("-sv", "Swedish");
        langMap.put("-ty", "Tahitian");
        langMap.put("-ta", "Tamil");
        langMap.put("-tt", "Tatar");
        langMap.put("-te", "Telugu");
        langMap.put("-tg", "Tajik");
        langMap.put("-tl", "Tagalog");
        langMap.put("-th", "Thai");
        langMap.put("-bo", "Tibetan");
        langMap.put("-ti", "Tigrinya");
        langMap.put("-to", "Tonga (Tonga Islands)");
        langMap.put("-tn", "Tswana");
        langMap.put("-ts", "Tsonga");
        langMap.put("-tk", "Turkmen");
        langMap.put("-tr", "Turkish");
        langMap.put("-tw", "Twi");
        langMap.put("-ug", "Uighur; Uyghur");
        langMap.put("-uk", "Ukrainian");
        langMap.put("-ur", "Urdu");
        langMap.put("-uz", "Uzbek");
        langMap.put("-ve", "Venda");
        langMap.put("-vi", "Vietnamese");
        langMap.put("-vo", "Volapük");
        langMap.put("-cy", "Welsh");
        langMap.put("-wa", "Walloon");
        langMap.put("-wo", "Wolof");
        langMap.put("-xh", "Xhosa");
        langMap.put("-ji", "Yiddish");
        langMap.put("-yi", "Yiddish");
        langMap.put("-yo", "Yoruba");
        langMap.put("-za", "Zhuang; Chuang");
        langMap.put("-zu", "Zulu");
    }

    public static int getMapSize() {
        return langMap.size();
    }

    public static String getLanguage(String str) {
        if (str.equals("")) {
            return "Default";
        }
        int indexOf = str.indexOf(45, 1);
        String str2 = (String) langMap.get(indexOf != -1 ? str.substring(0, indexOf) : str);
        return str2 != null ? str2 + " (" + str + ")" : " (" + str + ")";
    }

    public static void addLang(String[] strArr, String[] strArr2) {
        int i = 0;
        Iterator it = langMap.entrySet().iterator();
        while (true) {
            int i2 = i;
            if (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                strArr[i2] = (String) entry.getKey();
                strArr2[i2] = (String) entry.getValue();
                i = i2 + 1;
            } else {
                return;
            }
        }
    }
}
