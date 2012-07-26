package jp.gr.java_conf.kumagusu;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;

/**
 * ユーティリティー.
 *
 * @author tarshi
 *
 */
public final class Utilities
{
    /**
     * ユーティリティーをインスタンス化させない.
     */
    private Utilities()
    {
    }

    /**
     * 現在の日付を埋め込んだ文字列を返す.
     *
     * @param con コンテキスト
     * @param baseString 元の文字列
     * @return 現在の日付を埋め込んだ文字列
     */
    public static String getDateTimeFormattedString(Context con, String baseString)
    {
        return getDateTimeFormattedString(con, baseString, new Date());
    }

    /**
     * 現在の日付を埋め込んだ文字列を返す.
     *
     * @param con コンテキスト
     * @param baseString 元の文字列
     * @param date 日時
     * @return 現在の日付を埋め込んだ文字列
     */
    public static String getDateTimeFormattedString(Context con, String baseString, Date date)
    {
        String[] keyStrings = con.getResources().getStringArray(R.array.fixed_phrase_escape_item_values);
        String[] formatLetterStrings = con.getResources()
                .getStringArray(R.array.fixed_phrase_escape_item_format_values);
        assert keyStrings.length == formatLetterStrings.length;

        String patternString = baseString;

        for (int i = 0; i < keyStrings.length; i++)
        {
            String[] formatLeSplitStrings = formatLetterStrings[i].split(",");
            String formatLetter;
            Locale locale;

            if (formatLeSplitStrings.length >= 1)
            {
                formatLetter = formatLeSplitStrings[0];
            }
            else
            {
                assert false;
                continue;
            }

            if ((formatLeSplitStrings.length >= 2) && (formatLeSplitStrings[1].equals("1")))
            {
                locale = Locale.ENGLISH;
            }
            else
            {
                locale = Locale.getDefault();
            }

            if ((keyStrings[i].equals("%%")) || (keyStrings[i].equals("''")))
            {
                continue;
            }

            if (keyStrings[i].equals("%n"))
            {
                patternString = patternString.replace(keyStrings[i], formatLetter);
            }
            else
            {
                SimpleDateFormat formatter = new SimpleDateFormat(formatLetter, locale);
                String dateString = formatter.format(date);

                patternString = patternString.replace(keyStrings[i], dateString);
            }
        }

        patternString = patternString.replace("%%", "%");

        return patternString;

    }
}
