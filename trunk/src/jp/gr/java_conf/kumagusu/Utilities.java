package jp.gr.java_conf.kumagusu;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.gr.java_conf.kumagusu.control.ConfirmDialog;
import jp.gr.java_conf.kumagusu.control.InputDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;

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

    /**
     * パスワードを2回入力し、一致すればOK処理を実行する. キャンセルを押したとき、キャンセル処理を実行する.
     *
     * @param act アクティビティ
     * @param okListener OK処理（nullなら実行しない）
     * @param cancelListener キャンセル処理（nullなら実行しない）
     */
    public static void inputPassword(final Activity act, final DialogInterface.OnClickListener okListener,
            final DialogInterface.OnClickListener cancelListener)
    {
        // パスワード入力（１回目）
        final InputDialog dialog = new InputDialog();
        dialog.showDialog(act, act.getResources().getString(R.string.ui_td_input_password), InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface d, int which)
            {
                // OK処理
                final String tryPassword1 = dialog.getText();

                if (tryPassword1.length() == 0)
                {
                    // 入力されていない
                    ConfirmDialog.showDialog(act,
                            act.getResources().getDrawable(android.R.drawable.ic_menu_info_details), act.getResources()
                                    .getString(R.string.ui_td_input_password_empty), null,
                            ConfirmDialog.PositiveCaptionKind.OK, null, null);
                    return;
                }

                // パスワード入力（２回目）
                dialog.showDialog(act, act.getResources().getString(R.string.ui_td_reinput_password),
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface d, int which)
                            {
                                // OK処理
                                String tryPassword2 = dialog.getText();

                                if (tryPassword1.equals(tryPassword2))
                                {
                                    // 入力パスワード保存
                                    MainApplication.getInstance(act).addPassword(tryPassword1);
                                    MainApplication.getInstance(act).setLastCorrectPassword(tryPassword1);

                                    if (okListener != null)
                                    {
                                        okListener.onClick(d, which);
                                    }
                                }
                                else
                                {
                                    // パスワードが一致しない
                                    ConfirmDialog.showDialog(act,
                                            act.getResources().getDrawable(android.R.drawable.ic_menu_info_details),
                                            act.getResources().getString(R.string.ui_td_input_password_incorrect),
                                            null, ConfirmDialog.PositiveCaptionKind.OK, null, null);
                                }
                            }
                        }, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface d, int which)
                            {
                                // キャンセル処理
                                if (cancelListener != null)
                                {
                                    cancelListener.onClick(d, which);
                                }
                            }
                        });

            }
        }, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface d, int which)
            {
                // キャンセル処理
                if (cancelListener != null)
                {
                    cancelListener.onClick(d, which);
                }
            }
        });
    }
}
