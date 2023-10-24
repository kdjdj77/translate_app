package kr.juggler.translatebutton;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OCRHelper {
    private TessBaseAPI tessBaseAPI;

    public OCRHelper(Context context) {
        tessBaseAPI = new TessBaseAPI();
        // Tesseract의 데이터 파일이 저장된 경로를 지정합니다.
        String datapath = context.getFilesDir() + "/tesseract/";
        String language = "kor"; // 인식할 언어를 선택합니다. 예: "eng" (영어), "kor" (한국어) 등

        checkAndCopyLanguageData(context, datapath, language);
        tessBaseAPI.init(datapath, language);
    }

    private void checkAndCopyLanguageData(Context context, String datapath, String language) {
        File tessdataDir = new File(datapath, "tessdata");
        if (!tessdataDir.exists()) {
            tessdataDir.mkdirs();
        }

        String trainedDataFilePath = datapath + "/tessdata/" + language + ".traineddata";
        if (!new File(trainedDataFilePath).exists()) {
            try {
                InputStream inputStream = context.getAssets().open("tessdata/" + language + ".traineddata");
                OutputStream outputStream = new FileOutputStream(trainedDataFilePath);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private final int SIMILAR = 3;
    public String recognizeText(Bitmap bitmap) {

        StringBuilder sb = new StringBuilder();
        tessBaseAPI.setImage(bitmap);
        String text = tessBaseAPI.getUTF8Text();
        Log.i("text", text);
        text = text.replaceAll("[^가-힣\\n]", " ");
        text = text.replaceAll("\\s+", " ");
        return text;
    }

    private final int HANGEUL_BASE = 0xAC00;    // '가'
    private final int HANGEUL_END = 0xD7AF;
    // 이하 cho, jung, jong은 계산 결과로 나온 자모에 대해 적용
    private final int CHO_BASE = 0x1100;
    private final int JUNG_BASE = 0x1161;
    private final int JONG_BASE = (int)0x11A8 - 1;
    // 이하 ja, mo는 단독으로 입력된 자모에 대해 적용
    private final int JA_BASE = 0x3131;
    private final int MO_BASE = 0x314F;
    private String splitKor(String text) {
        List<Character> list = new ArrayList<>();

        for(char c : text.toCharArray()) {
            if((c <= 10 && c <= 13) || c == 32) {
                list.add(c);
                continue;
            } else if (c >= JA_BASE && c <= JA_BASE + 36) {
                list.add(c);
                continue;
            } else if (c >= MO_BASE && c <= MO_BASE + 58) {
                list.add((char)0);
                continue;
            } else if (c >= HANGEUL_BASE && c <= HANGEUL_END){
                int choInt = (c - HANGEUL_BASE) / 28 / 21;
                int jungInt = ((c - HANGEUL_BASE) / 28) % 21;
                int jongInt = (c - HANGEUL_BASE) % 28;
                char cho = (char) (choInt + CHO_BASE);
                char jung = (char) (jungInt + JUNG_BASE);
                char jong = jongInt != 0 ? (char) (jongInt + JONG_BASE) : 0;

                list.add(cho);
                list.add(jung);
                if (jong != 0) list.add(jong);
            } else {
                list.add(c);
            }

        }
        StringBuilder sb = new StringBuilder();
        for(char c : list) sb.append(c);
        return sb.toString();
    }

    private int calculateDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException("Input strings cannot be null.");
        }

        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int insert = dp[i][j - 1] + 1;
                    int delete = dp[i - 1][j] + 1;
                    int replace = dp[i - 1][j - 1] + 1;
                    dp[i][j] = Math.min(insert, Math.min(delete, replace));
                }
            }
        }

        return dp[m][n];
    }

    private int similar(String s1, String s2) {
        int distance = calculateDistance(s1, s2);
        Log.i("dist", s1 + " " + s2 + " " + distance);
        return distance;
    }

    public void release() {
        tessBaseAPI.end();
    }
}
