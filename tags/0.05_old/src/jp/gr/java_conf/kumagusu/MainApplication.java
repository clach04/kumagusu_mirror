package jp.gr.java_conf.kumagusu;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.app.Application;

/**
 * Applicationクラス.
 *
 * @author tarshi
 */
public final class MainApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    /**
     * MainApplicationのインスタンスを取得する.
     *
     * @param act Activity
     * @return MainApplicationインスタンス
     */
    public static MainApplication getInstance(Activity act)
    {
        return (MainApplication) act.getApplication();
    }

    /**
     * 現在のフォルダ（絶対パス）.
     */
    private String currentMemoFolder = null;

    /**
     * 現在のフォルダ（絶対パス）を返す.
     *
     * @return 現在のフォルダ（絶対パス）
     */
    public String getCurrentMemoFolder()
    {
        return currentMemoFolder;
    }

    /**
     * 現在のフォルダ（絶対パス）を設定する.
     *
     * @param folder 現在のフォルダ（絶対パス）
     */
    public void setCurrentMemoFolder(String folder)
    {
        this.currentMemoFolder = folder;
    }

    /**
     * 入力済みのパスワードのリスト.
     */
    private List<String> passwordList = new ArrayList<String>();

    /**
     * 入力済みのパスワードのリストをかえす.
     *
     * @return 入力済みのパスワードのリスト
     */
    public List<String> getPasswordList()
    {
        return passwordList;
    }

    /**
     * パスワードを設定する.
     *
     * @param pass パスワード
     */
    public void addPassword(String pass)
    {
        if (!getPasswordList().contains(pass))
        {
            getPasswordList().add(pass);
        }
    }

    /**
     * 最後の正しいパスワード.
     */
    private String lastCorrectPassword = null;

    /**
     * 最後の正しいパスワードを返す.
     *
     * @return 最後の正しいパスワード
     */
    public String getLastCorrectPassword()
    {
        return this.lastCorrectPassword;
    }

    /**
     * 最後の正しいパスワードを設定する.
     *
     * @param lcPassword 最後の正しいパスワード
     */
    public void setLastCorrectPassword(String lcPassword)
    {
        this.lastCorrectPassword = lcPassword;
    }

    /**
     * 保存パスワードをクリアする.
     */
    public void clearPasswordList()
    {
        this.passwordList.clear();
        this.lastCorrectPassword = null;
    }

    /**
     * メモリスト状態.
     */
    private Stack<MemoListViewStatus> memoListStatusStack = new Stack<MainApplication.MemoListViewStatus>();

    /**
     * メモリスト状態を保存.
     *
     * @param status メモリスト状態
     */
    public void pushMemoListStatusStack(MemoListViewStatus status)
    {
        while (true)
        {
            // スタックが空ならpush
            if (this.memoListStatusStack.empty())
            {
                this.memoListStatusStack.push(status);
                break;
            }

            MemoListViewStatus popStatus = memoListStatusStack.peek();
            String popLastFolder = popStatus.getLastFolder();

            // すでに同じフォルダの情報であれば、入れ替え
            if (popLastFolder.equals(status.getLastFolder()))
            {
                this.memoListStatusStack.pop(); // 削除
                this.memoListStatusStack.push(status);
                break;
            }

            // 下へ移動した場合、スタックに追加
            if (status.getLastFolder().startsWith(popLastFolder))
            {
                this.memoListStatusStack.push(status);
                break;
            }
            else
            {
                this.memoListStatusStack.pop(); // 削除
            }
        }
    }

    /**
     * 指定フォルダのメモリスト状態を返す.
     *
     * @param currentFolder 現在のフォルダ
     * @return メモリスト状態
     */
    public MemoListViewStatus popMemoListViewStatus(String currentFolder)
    {
        MemoListViewStatus result = null;

        for (int i = 0; i < this.memoListStatusStack.size(); i++)
        {
            MemoListViewStatus status = this.memoListStatusStack.get(i);

            if (currentFolder.equals(status.getLastFolder()))
            {
                result = status;
                break;
            }
        }

        return result;
    }

    /**
     * メモリスト状態（検索用）.
     */
    private MemoListViewStatus memoListStatus4Search = null;

    /**
     * メモリスト状態（検索用）を返す.
     *
     * @return メモリスト状態（検索用）
     */
    public MemoListViewStatus getMemoListStatus4Search()
    {
        return memoListStatus4Search;
    }

    /**
     * メモリスト状態（検索用）を設定する.
     *
     * @param status メモリスト状態（検索用）
     */
    public void setMemoListStatus4Search(MemoListViewStatus status)
    {
        this.memoListStatus4Search = status;
    }

    /**
     * メモリストの状態.
     *
     * @author tarshi
     *
     */
    public static final class MemoListViewStatus
    {
        /**
         * 最後の表示フォルダ.
         */
        private String lastFolder = null;

        /**
         * 最後のリスト表示位置.
         */
        private int lastTopPosition = 0;

        /**
         * 最後のリスト表示位置.
         */
        private int lastTopPositionY = 0;

        /**
         * 最後の表示フォルダを返す.
         *
         * @return 最後の表示フォルダ
         */
        public String getLastFolder()
        {
            return this.lastFolder;
        }

        /**
         * 最後の表示フォルダを設定する.
         *
         * @param folder 最後の表示フォルダ
         */
        public void setLastFolder(String folder)
        {
            this.lastFolder = folder;
        }

        /**
         * 最後のリスト表示位置を返す.
         *
         * @return 最後のリスト表示位置
         */
        public int getLastTopPosition()
        {
            return lastTopPosition;
        }

        /**
         * 最後のリスト表示位置を設定する.
         *
         * @param pos 最後のリスト表示位置
         */
        public void setLastTopPosition(int pos)
        {
            this.lastTopPosition = pos;
        }

        /**
         * 最後のリスト表示位置を返す.
         *
         * @return 最後のリスト表示位置
         */
        public int getLastTopPositionY()
        {
            return lastTopPositionY;
        }

        /**
         * 最後のリスト表示位置を設定する.
         *
         * @param posY 最後のリスト表示位置
         */
        public void setLastTopPositionY(int posY)
        {
            this.lastTopPositionY = posY;
        }
    }
}
