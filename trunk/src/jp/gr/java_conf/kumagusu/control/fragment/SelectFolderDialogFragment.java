package jp.gr.java_conf.kumagusu.control.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import jp.gr.java_conf.kumagusu.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Fragment版フォルダ選択ダイアログ.
 *
 * @author tarshi
 */
public final class SelectFolderDialogFragment extends DialogFragment
{
    /**
     * ルートフォルダFile.
     */
    private File rootFolderFile = null;

    /**
     * 現在のフォルダ.
     */
    private String currentFolderPath = null;

    /**
     * 現在のフォルダ内のFileオブジェクトリスト.
     */
    private ArrayList<File> currentFolderFileList = new ArrayList<File>();

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
     * フォルダ選択ダイアログを生成する.
     *
     * @param listenerId リスナ保持データID
     * @param titleId タイトルID
     * @param chrootPath ルートとして扱うフォルダ（nullなら/）
     * @param path 初期フォルダパス
     * @return ディレクトリ選択ダイアログ
     */
    public static SelectFolderDialogFragment newInstance(int listenerId, int titleId, String chrootPath, String path)
    {
        return newInstance(listenerId, 0, titleId, chrootPath, path);
    }

    /**
     * フォルダ選択ダイアログを生成する.
     *
     * @param listenerId リスナ保持データID
     * @param iconId アイコンID
     * @param titleId タイトルID
     * @param chrootPath ルートとして扱うフォルダ（nullなら/）
     * @param path 初期フォルダパス
     * @return ディレクトリ選択ダイアログ
     */
    public static SelectFolderDialogFragment newInstance(int listenerId, int iconId, int titleId, String chrootPath,
            String path)
    {
        SelectFolderDialogFragment frag = new SelectFolderDialogFragment();

        Bundle args = new Bundle();

        args.putInt("listenerId", listenerId);
        args.putInt("iconId", iconId);
        args.putInt("titleId", titleId);
        args.putString("chrootPath", chrootPath);

        File currentFolder = new File(path);
        String currentFolderPath = currentFolder.getAbsolutePath();
        args.putString("currentFolderPath", currentFolderPath);

        frag.setArguments(args);

        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        int listenerId = getArguments().getInt("listenerId");
        int iconId = getArguments().getInt("iconId");
        int titleId = getArguments().getInt("titleId");
        String chrootPath = getArguments().getString("chrootPath");
        this.currentFolderPath = getArguments().getString("currentFolderPath");

        this.rootFolderFile = new File((chrootPath != null) ? chrootPath : "/");

        // アクティビティーからリスナ取得
        Activity activity = getActivity();

        SelectFolderDialogListeners listeners = null;

        if (activity instanceof SelectFolderDialogListenerFolder)
        {
            SelectFolderDialogListenerFolder listenerFolder = (SelectFolderDialogListenerFolder) activity;
            listeners = listenerFolder.getSelectFolderDialogListeners(listenerId);
        }

        final SelectFolderDialogFragment.OnSelectFolderListener okListener;
        final SelectFolderDialogFragment.OnSelectFolderListener cancelListener;

        if (listeners != null)
        {
            okListener = listeners.getOkOnClickListener();
            cancelListener = listeners.getCancelOnClickListener();
        }
        else
        {
            okListener = null;
            cancelListener = null;
        }

        // View取得
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.list_view_dialog, null);
        this.listView = (ListView) view.findViewById(R.id.list_view);
        this.messageTextView = (TextView) view.findViewById(R.id.message);
        this.messageTextView.setVisibility(TextView.VISIBLE);

        ArrayList<String> currentFolderList = new ArrayList<String>();
        this.currentFolderListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
                currentFolderList);
        this.listView.setAdapter(this.currentFolderListAdapter);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // フォルダリスト再生成
                SelectFolderDialogFragment.this.currentFolderPath = SelectFolderDialogFragment.this.currentFolderFileList
                        .get(position).getAbsolutePath();

                createCurrentFolderList();

                // 表示位置を先頭に戻す
                SelectFolderDialogFragment.this.listView.setSelectionFromTop(0, 0);
            }
        });

        // 初期のフォルダリスト生成
        createCurrentFolderList();

        // ダイアログ表示
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        if (iconId > 0)
        {
            alertDialogBuilder.setIcon(iconId);
        }

        alertDialogBuilder.setTitle(getString(titleId));
        alertDialogBuilder.setView(view);

        // 自身のContextではgetStringが失敗する
        alertDialogBuilder.setPositiveButton(getActivity().getString(R.string.ui_ok),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Log.d("DirectorySelectDialog", SelectFolderDialogFragment.this.currentFolderPath);

                        if (okListener != null)
                        {
                            okListener.onSelect(SelectFolderDialogFragment.this.currentFolderPath);
                        }
                    }
                });

        // 自身のContextではgetStringが失敗する
        alertDialogBuilder.setNegativeButton(getActivity().getString(R.string.ui_cancel),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (cancelListener != null)
                        {
                            cancelListener.onSelect(null);
                        }
                    }
                });

        // 戻るキーによるキャンセルを処理
        alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                if (cancelListener != null)
                {
                    cancelListener.onSelect(null);
                }
            }
        });

        Dialog dialog = alertDialogBuilder.create();

        return dialog;
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
     * フォルダ選択イベントのリスナのインタフェース.
     *
     * @author tarshi
     */
    public interface OnSelectFolderListener
    {
        /**
         * フォルダの選択イベントを処理する.
         *
         * @param path 選択ディレクトリのパス（絶対パス）
         */
        void onSelect(String path);
    }
}
