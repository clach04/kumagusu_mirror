package jp.gr.java_conf.kumagusu.preference;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import jp.gr.java_conf.kumagusu.R;
import jp.gr.java_conf.kumagusu.memoio.MemoUtilities;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 設定のメモフォルダ選択処理.
 *
 * @author tarshi
 *
 */
public final class DirectorySelectDialogPreference extends DialogPreference
{
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
     * 設定のメモフォルダ選択処理を初期化する.
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
        Log.d("DirectorySelectDialogPreference", "*** START onBindView()");

        setSummary(getInitFolderPath());

        super.onBindView(view);
    }

    @Override
    protected View onCreateDialogView()
    {
        Log.d("DirectorySelectDialogPreference", "*** START onCreateDialogView()");

        // 初期フォルダ取得
        this.currentFolderPath = getInitFolderPath();

        // View生成
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.list_view_dialog, null);
        this.listView = (ListView) view.findViewById(R.id.list_view);
        this.messageTextView = (TextView) view.findViewById(R.id.message);
        this.messageTextView.setVisibility(TextView.VISIBLE);

        ArrayList<String> currentFolderList = new ArrayList<String>();
        this.currentFolderListAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,
                currentFolderList);
        this.listView.setAdapter(this.currentFolderListAdapter);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // フォルダリスト再生成
                DirectorySelectDialogPreference.this.currentFolderPath = DirectorySelectDialogPreference.this.currentFolderFileList
                        .get(position).getAbsolutePath();

                createCurrentFolderList();

                // 表示位置を先頭に戻す
                DirectorySelectDialogPreference.this.listView.setSelectionFromTop(0, 0);
            }
        });

        // 初期のフォルダリスト生成
        createCurrentFolderList();

        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        Log.d("DirectorySelectDialogPreference", "*** START onDialogClosed() positiveResult:" + positiveResult);

        if (positiveResult)
        {
            if ((this.currentFolderPath == null) || (this.currentFolderPath.length() == 0))
            {
                this.currentFolderPath = "/";
            }

            SharedPreferences.Editor editor = getEditor();
            editor.putString(getKey(), this.currentFolderPath);
            editor.commit();
            notifyChanged();
        }
    }

    /**
     * 初期フォルダを取得する.
     *
     * @return 初期フォルダ
     */
    private String getInitFolderPath()
    {
        String folderPath = MemoUtilities.getDefaultMemoFolderPath();

        SharedPreferences pref = getSharedPreferences();
        if (null != pref)
        {
            folderPath = pref.getString(getKey(), folderPath);
        }

        return folderPath;
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

        // ルートフォルダ
        File rootFolderFile = new File("/");

        // タイトルにパスを表示
        String showTitle = currentFolder.getAbsolutePath();

        if (!rootFolderFile.getAbsolutePath().equals("/"))
        {
            showTitle = showTitle.substring(rootFolderFile.getAbsolutePath().length());
            if (showTitle.length() == 0)
            {
                showTitle = "/";
            }
        }

        this.messageTextView.setText(showTitle);

        // ディレクトリのリストを作成
        this.currentFolderFileList.clear();
        this.currentFolderListAdapter.clear();

        if (!currentFolder.getPath().equals(rootFolderFile.getPath()))
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
}
