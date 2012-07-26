package jp.gr.java_conf.kumagusu.control;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import jp.gr.java_conf.kumagusu.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

/**
 * ディレクトリ選択ダイアログ.
 *
 * @author tarshi
 */
public final class DirectorySelectDialog extends Activity implements DialogInterface.OnClickListener
{
    /**
     * ルートフォルダFile.
     */
    private File rootFolderFile = null;

    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * ディレクトリリスト.
     */
    private ArrayList<File> mDirectoryList;

    /**
     * ディレクトリリスト選択イベントのリスナ.
     */
    private OnDirectoryListDialogListener mListenner;

    /**
     * ディレクトリ選択ダイアログを初期化する.
     *
     * @param context コンテキスト
     */
    public DirectorySelectDialog(Context context)
    {
        mContext = context;
        mDirectoryList = new ArrayList<File>();
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        if ((null != mDirectoryList) && (null != mListenner))
        {
            File file = mDirectoryList.get(which);
            show(file.getAbsolutePath());
        }
    }

    /**
     * ディレクトリ選択ダイアログを表示する.
     *
     * @param path 表示ディレクトリのパス（絶対パス）
     */
    public void show(final String path)
    {
        show(null, path);
    }

    /**
     * ディレクトリ選択ダイアログを表示する.
     *
     * @param chrootPath ルートとして扱うフォルダ（nullなら/）
     * @param path 表示ディレクトリのパス（絶対パス）
     */
    public void show(String chrootPath, final String path)
    {
        try
        {
            File currentFolder = new File(path);

            if (this.rootFolderFile == null)
            {
                if (chrootPath == null)
                {
                    chrootPath = "/";
                }

                this.rootFolderFile = new File(chrootPath);
            }

            File[] mDirectories = currentFolder.listFiles();
            Arrays.sort(mDirectories);

            if (null == mDirectories && null != mListenner)
            {
                mListenner.onClickFileList(null);
            }
            else
            {
                // タイトルに表示するパスを生成
                String showTitle = currentFolder.getAbsolutePath();
                if (!this.rootFolderFile.getAbsolutePath().equals("/"))
                {
                    showTitle = showTitle.substring(this.rootFolderFile.getAbsolutePath().length());
                    if (showTitle.length() == 0)
                    {
                        showTitle = "/";
                    }
                }

                // ディレクトリのリストを作成
                mDirectoryList.clear();
                ArrayList<String> viewList = new ArrayList<String>();

                if (!currentFolder.getPath().equals(this.rootFolderFile.getPath()))
                {
                    // 上位フォルダを登録
                    viewList.add("..");
                    mDirectoryList.add(currentFolder.getParentFile());
                }

                for (File file : mDirectories)
                {
                    if (file.isDirectory())
                    {
                        viewList.add(file.getName() + "/");
                        mDirectoryList.add(file);
                    }
                }

                // ダイアログ表示
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                alertDialogBuilder.setTitle(showTitle);
                alertDialogBuilder.setItems(viewList.toArray(new String[0]), this);

                // 自身のContextではgetStringが失敗する
                alertDialogBuilder.setPositiveButton(mContext.getString(R.string.ui_ok),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Log.d("DirectorySelectDialog", path);
                                mListenner.onClickFileList(path);
                            }
                        });

                // 自身のContextではgetStringが失敗する
                alertDialogBuilder.setNegativeButton(mContext.getString(R.string.ui_cancel),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                mListenner.onClickFileList(null);
                            }
                        });

                alertDialogBuilder.show();
            }
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * ディレクトリリスト選択イベントのリスナを設定する.
     *
     * @param listener ディレクトリリスト選択イベントのリスナ
     */
    public void setOnFileListDialogListener(OnDirectoryListDialogListener listener)
    {
        mListenner = listener;
    }

    /**
     * ディレクトリリスト選択イベントのリスナのインタフェース.
     *
     * @author tarshi
     */
    public interface OnDirectoryListDialogListener
    {
        /**
         * ディレクトリの選択イベントを処理する.
         *
         * @param path 選択ディレクトリのパス（絶対パス）
         */
        void onClickFileList(String path);
    }
}
