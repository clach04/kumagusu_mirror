package jp.gr.java_conf.kumagusu.commons;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.gr.java_conf.kumagusu.MainApplication;
import jp.gr.java_conf.kumagusu.R;
import jp.gr.java_conf.kumagusu.control.ConfirmDialogFragment;
import jp.gr.java_conf.kumagusu.control.ConfirmDialogListenerFolder;
import jp.gr.java_conf.kumagusu.control.DialogListeners;
import jp.gr.java_conf.kumagusu.control.InputDialog;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.Log;

/**
 * ユーティリティー.
 *
 * @author tarshi
 *
 */
public final class Utilities
{
    /**
     * 確認ダイアログID「パスワードが入力されてない」.
     */
    private static final int DIALOG_ID_CONFIRM_PASSWORD_EMPTY = 9001;

    /**
     * 確認ダイアログID「パスワードが一致しない」.
     */
    private static final int DIALOG_ID_CONFIRM_PASSWORD_INCORRECT = 9002;

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

    // TODO Activity破棄時に初期化されるため、修正が必要。
    /**
     * OK処理（nullなら実行しない）.
     */
    private static DialogInterface.OnClickListener okListener = null;

    // TODO Activity破棄時に初期化されるため、修正が必要。
    /**
     * キャンセル処理（nullなら実行しない）.
     */
    private static DialogInterface.OnClickListener cancelListener = null;

    /**
     * パスワードを2回入力し、一致すればOK処理を実行する. キャンセルを押したとき、キャンセル処理を実行する.
     *
     * @param act アクティビティ
     * @param ok OK処理（nullなら実行しない）
     * @param cancel キャンセル処理（nullなら実行しない）
     */
    public static void inputPassword(final FragmentActivity act, final DialogInterface.OnClickListener ok,
            final DialogInterface.OnClickListener cancel)
    {
        // ダイアログを設定
        okListener = ok;
        cancelListener = cancel;

        initConfirmDialogListener(act);

        // パスワード入力（１回目）
        final InputDialog dialog = new InputDialog(act);
        dialog.showDialog(act.getResources().getString(R.string.ui_td_input_password), InputType.TYPE_CLASS_TEXT
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
                    ConfirmDialogFragment.newInstance(DIALOG_ID_CONFIRM_PASSWORD_EMPTY,
                            android.R.drawable.ic_menu_info_details, R.string.ui_td_input_password_empty, 0,
                            ConfirmDialogFragment.POSITIVE_CAPTION_KIND_OK).show(act.getSupportFragmentManager(), "");
                    return;
                }

                // パスワード入力（２回目）
                dialog.showDialog(act.getResources().getString(R.string.ui_td_reinput_password),
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
                                    ConfirmDialogFragment.newInstance(DIALOG_ID_CONFIRM_PASSWORD_INCORRECT,
                                            android.R.drawable.ic_menu_info_details,
                                            R.string.ui_td_input_password_incorrect, 0,
                                            ConfirmDialogFragment.POSITIVE_CAPTION_KIND_OK).show(
                                            act.getSupportFragmentManager(), "");
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

    /**
     * 確認ダイアログのリスナを初期化する.
     *
     * @param act アクティビティ
     */
    private static void initConfirmDialogListener(final FragmentActivity act)
    {
        if (act instanceof ConfirmDialogListenerFolder)
        {
            Log.e("Utilities", "Not ConfirmDialogListenerFolder");
            return;
        }

        ConfirmDialogListenerFolder listenerFolder = (ConfirmDialogListenerFolder) act;

        // パスワードが入力されてない
        listenerFolder.putConfirmDialogListeners(DIALOG_ID_CONFIRM_PASSWORD_EMPTY, new DialogListeners(null,
                null, null));

        // パスワードが一致しない
        listenerFolder.putConfirmDialogListeners(DIALOG_ID_CONFIRM_PASSWORD_INCORRECT, new DialogListeners(
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // リトライ
                        inputPassword(act, okListener, cancelListener);
                    }
                }, null, null));

    }

    /**
     * 画面の縦横を現状で固定する.
     *
     * @param act アクティビティ
     * @param fixed 固定するときtrue
     */
    public static void fixOrientation(Activity act, boolean fixed)
    {
        Configuration configuration = act.getResources().getConfiguration();

        if (fixed)
        {
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                // 縦固定
                act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                // 横固定
                act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
        else
        {
            act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }
}
