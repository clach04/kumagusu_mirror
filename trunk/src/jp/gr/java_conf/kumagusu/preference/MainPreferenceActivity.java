package jp.gr.java_conf.kumagusu.preference;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.kumagusu.R;
import jp.gr.java_conf.kumagusu.memoio.MemoUtilities;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * 設定画面のActivity.
 *
 * @author tarshi
 *
 */
public final class MainPreferenceActivity extends PreferenceActivity implements OnPreferenceChangeListener
{
    /**
     * メモ並び替え方法(タイトル(辞書順)).
     */
    public static final int MEMO_SORT_METHOD_TITLE_ASC = 0;
    /**
     * メモ並び替え方法(タイトル(辞書逆順)).
     */
    public static final int MEMO_SORT_METHOD_TITLE_DESC = 1;
    /**
     * メモ並び替え方法(更新日時(最古～)).
     */
    public static final int MEMO_SORT_METHOD_LAST_MODIFIED_ASC = 10;
    /**
     * メモ並び替え方法(更新日時(最新～)).
     */
    public static final int MEMO_SORT_METHOD_LAST_MODIFIED_DESC = 11;
    /**
     * メモ並び替え方法(サイズ(最小～)).
     */
    public static final int MEMO_SORT_METHOD_SIZE_ASC = 20;
    /**
     * メモ並び替え方法(サイズ(最大～)).
     */
    public static final int MEMO_SORT_METHOD_SIZE_DESC = 21;

    /**
     * メモの自動クローズ時間（ミリ秒）.
     */
    private static final int PREF_DEFAULT_VALUE_AUTO_CLOSE_TIME = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        // 「バックグラウンド時にメモを閉じる」の設定値をSummaryに表示
        ListPreference autoCloseDelayTimePreference = (ListPreference) getPreferenceScreen().findPreference(
                "ls_autoclose_delaytime");
        autoCloseDelayTimePreference.setSummary(autoCloseDelayTimePreference.getEntry());
        autoCloseDelayTimePreference.setOnPreferenceChangeListener(this);

        ListPreference encodingNaemPreference = (ListPreference) getPreferenceScreen().findPreference(
                "ls_encoding_name");
        encodingNaemPreference.setSummary(encodingNaemPreference.getEntry());
        encodingNaemPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
        // 「バックグラウンド時にメモを閉じる」の設定値をSummaryに表示
        if (preference.getKey().equals("ls_autoclose_delaytime"))
        {
            String[] values = getResources().getStringArray(R.array.autoclose_delaytime_values);
            String[] names = getResources().getStringArray(R.array.autoclose_delaytime_entries);

            preference.setSummary("");

            for (int i = 0; i < values.length; i++)
            {
                if (values[i].equals((String) newValue))
                {
                    preference.setSummary(names[i]);
                    break;
                }
            }

            return true;
        }

        // 「エンコーディング」の設定値をSummaryに表示
        if (preference.getKey().equals("ls_encoding_name"))
        {
            String[] values = getResources().getStringArray(R.array.encoding_values);
            String[] names = getResources().getStringArray(R.array.encoding_entries);

            preference.setSummary("");

            for (int i = 0; i < values.length; i++)
            {
                if (values[i].equals((String) newValue))
                {
                    preference.setSummary(names[i]);
                    break;
                }
            }

            return true;
        }

        return false;
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
     * 「定型文」を保存する.
     *
     * @param con コンテキスト
     * @param fixedPhraseStrings 定型文
     */
    public static void setFixedPhraseStrings(Context con, List<String> fixedPhraseStrings)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(con);
        Editor editor = sp.edit();

        int fixedPhraseStringsCount = sp.getInt("list_fixed_phrase_strings_count", 0);

        // 一旦すべて削除
        for (int i = 0; i < fixedPhraseStringsCount; i++)
        {
            editor.remove("list_fixed_phrase_strings_" + i);
        }

        // 保存
        for (int i = 0; i < fixedPhraseStrings.size(); i++)
        {
            editor.putString("list_fixed_phrase_strings_" + i, fixedPhraseStrings.get(i));
        }

        editor.putInt("list_fixed_phrase_strings_count", fixedPhraseStrings.size());

        editor.commit();
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

    /**
     * 「並び替え方法」を取得する.
     *
     * @param con コンテキスト
     * @param saveNum 保存番号
     * @return 並び替え方法
     */
    public static int getMemoSortMethod(Context con, int saveNum)
    {
        int method = PreferenceManager.getDefaultSharedPreferences(con).getInt(
                "memo_sort_method_" + String.valueOf(saveNum), MEMO_SORT_METHOD_TITLE_ASC);

        return method;
    }

    /**
     * 「並び替え方法」を保存する.
     *
     * @param con コンテキスト
     * @param method 並び替え方法
     * @param saveNum 保存番号
     */
    public static void setMemoSortMethod(Context con, int method, int saveNum)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(con);
        Editor editor = sp.edit();

        editor.putInt("memo_sort_method_" + String.valueOf(saveNum), method);

        editor.commit();
    }
}
