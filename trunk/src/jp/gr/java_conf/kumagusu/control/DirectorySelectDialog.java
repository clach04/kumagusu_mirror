package jp.gr.java_conf.kumagusu.control;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import jp.gr.java_conf.kumagusu.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * ディレクトリ選択ダイアログ.
 *
 * @author tarshi
 */
public final class DirectorySelectDialog extends OldStyleDialog
{
    /**
     * コンテキスト.
     */
    private Context mContext;

    /**
     * タイトル.
     */
    private String dialogTitle;

    /**
     * ルートフォルダFile.
     */
    private File rootFolderFile = null;

    /**
     * 現在のフォルダ内のFileオブジェクトリスト.
     */
    private ArrayList<File> currentFolderFileList = new ArrayList<File>();

    /**
     * 現在のフォルダ.
     */
    private String currentFolderPath = null;

    /**
     * ディレクトリリスト選択イベントのリスナ.
     */
    private OnDirectoryListDialogListener mListenner;

    /**
     * フォルダリストのアダプター.
     */
    private ArrayAdapter<String> currentFolderListAdapter;

    /**
     * メッセージ表示View.
     */
    private TextView messageTextView = null;

    /**
     * リスト表示View.
     */
    private ListView listView = null;

    /**
     * ディレクトリ選択ダイアログを初期化する.
     *
     * @param context コンテキスト
     * @param title タイトル
     * @param path 初期フォルダパス
     */
    public DirectorySelectDialog(Context context, String title, String path)
    {
        this(context, title, null, path);
    }

    /**
     * ディレクトリ選択ダイアログを初期化する.
     *
     * @param context コンテキスト
     * @param title タイトル
     * @param chrootPath ルートとして扱うフォルダ（nullなら/）
     * @param path 初期フォルダパス
     */
    public DirectorySelectDialog(Context context, String title, String chrootPath, String path)
    {
        this.mContext = context;
        this.dialogTitle = title;

        this.rootFolderFile = new File((chrootPath != null) ? chrootPath : "/");

        File currentFolder = new File(path);
        this.currentFolderPath = currentFolder.getAbsolutePath();
    }

    /**
     * ディレクトリ選択ダイアログを表示する.
     */
    public void show()
    {
        try
        {
            // View取得
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.list_view_dialog, null);
            this.listView = (ListView) view.findViewById(R.id.list_view);
            this.messageTextView = (TextView) view.findViewById(R.id.message);
            this.messageTextView.setVisibility(TextView.VISIBLE);

            ArrayList<String> currentFolderList = new ArrayList<String>();
            this.currentFolderListAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1,
                    currentFolderList);
            this.listView.setAdapter(this.currentFolderListAdapter);

            this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    // フォルダリスト再生成
                    DirectorySelectDialog.this.currentFolderPath = DirectorySelectDialog.this.currentFolderFileList
                            .get(position).getAbsolutePath();

                    createCurrentFolderList();

                    // 表示位置を先頭に戻す
                    DirectorySelectDialog.this.listView.setSelectionFromTop(0, 0);
                }
            });

            // 初期のフォルダリスト生成
            createCurrentFolderList();

            // ダイアログ表示
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
            alertDialogBuilder.setTitle(this.dialogTitle);
            alertDialogBuilder.setView(view);

            // 自身のContextではgetStringが失敗する
            alertDialogBuilder.setPositiveButton(mContext.getString(R.string.ui_ok),
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Log.d("DirectorySelectDialog", DirectorySelectDialog.this.currentFolderPath);
                            mListenner.onClickFileList(DirectorySelectDialog.this.currentFolderPath);
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

            // 戻るキーによるキャンセルを処理
            alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    mListenner.onClickFileList(null);
                }
            });

            Dialog dialog = alertDialogBuilder.create();

            dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialog)
                {
                    // ダイアログ消去後処理
                    postDismissDialog(mContext);
                }
            });

            // ダイアログ表示前処理
            preShowDialog(mContext);

            dialog.show();
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * フォルダリストを生成する.
     */
    private void createCurrentFolderList()
    {
        if ((this.currentFolderPath == null) || (this.currentFolderPath.length() == 0))
        {
            this.currentFolderPath = "/";
        }

        File currentFolder = new File(this.currentFolderPath);

        File[] mDirectories = currentFolder.listFiles();
        if (mDirectories != null)
        {
            Arrays.sort(mDirectories);
        }
        else
        {
            mDirectories = new File[0];
        }

        // タイトルにパスを表示
        String showTitle = currentFolder.getAbsolutePath();

        if (!this.rootFolderFile.getAbsolutePath().equals("/"))
        {
            showTitle = showTitle.substring(this.rootFolderFile.getAbsolutePath().length());
            if (showTitle.length() == 0)
            {
                showTitle = "/";
            }
        }

        this.messageTextView.setText(showTitle);

        // ディレクトリのリストを作成
        this.currentFolderFileList.clear();
        this.currentFolderListAdapter.clear();

        if (!currentFolder.getPath().equals(this.rootFolderFile.getPath()))
        {
            // 上位フォルダを登録
            this.currentFolderListAdapter.add("..");
            this.currentFolderFileList.add(currentFolder.getParentFile());
        }

        for (File file : mDirectories)
        {
            if (file.isDirectory())
            {
                this.currentFolderListAdapter.add(file.getName() + "/");
                this.currentFolderFileList.add(file);
            }
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
