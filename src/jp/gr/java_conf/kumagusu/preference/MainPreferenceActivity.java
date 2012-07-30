package jp.gr.java_conf.kumagusu.preference;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.kumagusu.R;
import jp.gr.java_conf.kumagusu.memoio.MemoUtilities;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * 設定画面のActivity.
 *
 * @author tarshi
 *
 */
public final class MainPreferenceActivity extends PreferenceActivity
{
    /**
     * メモの自動クローズ時間（ミリ秒）.
     */
    private static final int PREF_DEFAULT_VALUE_AUTO_CLOSE_TIME = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
    }

    /**
     * 「リストの詳細を表示する」を取得する.
     *
     * @param con コンテキスト
     * @return リストの詳細を表示するときtrue
     */
    public static boolean isListDetailVisibility(Context con)
    {
        return PreferenceManager.getDefaultSharedPreferences(con).getBoolean("cb_list_detail_visibility", true);
    }

    /**
     * 「エンコーディング名」を取得する.
     *
     * @param con コンテキスト
     * @return エンコーディング名
     */
    public static String getEncodingName(Context con)
    {
        return PreferenceManager.getDefaultSharedPreferences(con).getString("ls_encoding_name", "SJIS");
    }

    /**
     * 「エディタのタイトルを表示する」を取得する.
     *
     * @param con コンテキスト
     * @return エディタのタイトルを表示するときtrue
     */
    public static boolean isEnableEditorTitle(Context con)
    {
        return PreferenceManager.getDefaultSharedPreferences(con).getBoolean("cb_enable_editor_title", true);
    }

    /**
     * 「自動リンクを使用する」を取得する.
     *
     * @param con コンテキスト
     * @return 自動リンクを使用するときtrue
     */
    public static boolean isEnableAutoLink(Context con)
    {
        return PreferenceManager.getDefaultSharedPreferences(con).getBoolean("cb_enable_auto_link", true);
    }

    /**
     * 「暗号化時にファイル名をランダムにする」を取得する.
     *
     * @param con コンテキスト
     * @return ランダムにするときtrue
     */
    public static boolean isRandamName(Context con)
    {
        return PreferenceManager.getDefaultSharedPreferences(con).getBoolean("cb_randam_name", false);
    }

    /**
     * 「メモファイル名とタイトルを連動する」を取得する.
     *
     * @param con コンテキスト
     * @return 連動するときtrue
     */
    public static boolean isTitleLink(Context con)
    {
        return PreferenceManager.getDefaultSharedPreferences(con).getBoolean("cb_title_link", true);
    }

    /**
     * 「メモフォルダ」を取得する.
     *
     * @param con コンテキスト
     * @return メモフォルダ
     */
    public static String getMemoLocation(Context con)
    {
        // 設定値取得
        String location = PreferenceManager.getDefaultSharedPreferences(con).getString("ds_memo_location",
                MemoUtilities.getDefaultMemoFolderPath());

        return location;
    }

    /**
     * 「自動クローズ時間」を取得する.
     *
     * @param con コンテキスト
     * @return 自動クローズ時間
     */
    public static long getAutocloseDelaytime(Context con)
    {
        String delayTimeString = PreferenceManager.getDefaultSharedPreferences(con).getString("ls_autoclose_delaytime",
                Integer.toString(PREF_DEFAULT_VALUE_AUTO_CLOSE_TIME));
        long delayTime;
        try
        {
            delayTime = Long.parseLong(delayTimeString);
        }
        catch (NumberFormatException ex)
        {
            delayTime = PREF_DEFAULT_VALUE_AUTO_CLOSE_TIME;
        }

        return delayTime;
    }

    /**
     * 「定型文」を取得する.
     *
     * @param con コンテキスト
     * @return 定型文リスト
     */
    public static List<String> getFixedPhraseStrings(Context con)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(con);

        int fixedPhraseStringsCount = sp.getInt("list_fixed_phrase_strings_count", 0);

        // 設定がなければデフォルト設定を保存
        if (fixedPhraseStringsCount == 0)
        {
            Editor editor = sp.edit();

            String[] defaultPatterns = con.getResources().getStringArray(
                    R.array.fixed_phrase_escape_item_values_default);

            editor.putInt("list_fixed_phrase_strings_count", defaultPatterns.length);

            for (int i = 0; i < defaultPatterns.length; i++)
            {
                editor.putString("list_fixed_phrase_strings_" + i, defaultPatterns[i]);
            }

            editor.commit();

            fixedPhraseStringsCount = defaultPatterns.length;
        }

        // 設定取得
        List<String> resultList = new ArrayList<String>();

        for (int i = 0; i < fixedPhraseStringsCount; i++)
        {
            String fixedPhraseString = sp.getString("list_fixed_phrase_strings_" + i, "");

            resultList.add(fixedPhraseString);
        }

        return resultList;
    }

    /**
     * 「新規メモを暗号化メモにする」を取得する.
     *
     * @param con コンテキスト
     * @return 連動するときtrue
     */
    public static boolean isEnctyptNewMemo(Context con)
    {
        return PreferenceManager.getDefaultSharedPreferences(con).getBoolean("cb_encrypt_new_memo", true);
    }
}
