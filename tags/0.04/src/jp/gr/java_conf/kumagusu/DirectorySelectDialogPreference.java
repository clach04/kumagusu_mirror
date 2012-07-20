package jp.gr.java_conf.kumagusu;

import jp.gr.java_conf.kumagusu.control.DirectorySelectDialog;
import jp.gr.java_conf.kumagusu.memoio.MemoUtilities;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

/**
 * 設定のメモフォルダ選択処理のリスナ.
 *
 * @author tarshi
 *
 */
public final class DirectorySelectDialogPreference extends DialogPreference implements
        DirectorySelectDialog.OnDirectoryListDialogListener
{
    /**
     * 設定のメモフォルダ選択処理のリスナを初期化する.
     *
     * @param context コンテキスト
     * @param attrs 属性
     */
    public DirectorySelectDialogPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onBindView(View view)
    {
        SharedPreferences pref = getSharedPreferences();

        String summry = MemoUtilities.getDefaultMemoFolderPath();

        if (null != pref)
        {
            summry = pref.getString(getKey(), summry);
        }
        setSummary(summry);
        super.onBindView(view);
    }

    @Override
    protected void onClick()
    {
        SharedPreferences pref = getSharedPreferences();

        String summry = MemoUtilities.getDefaultMemoFolderPath();

        if (null != pref)
        {
            summry = pref.getString(getKey(), summry);
        }

        DirectorySelectDialog dlg = new DirectorySelectDialog(getContext());
        dlg.setOnFileListDialogListener(this);
        dlg.show(summry);
    }

    @Override
    public void onClickFileList(String path)
    {
        if (null != path)
        {
            SharedPreferences.Editor editor = getEditor();
            editor.putString(getKey(), path);
            editor.commit();
            notifyChanged();
        }
    }
}
