package jp.gr.java_conf.kumagusu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import jp.gr.java_conf.kumagusu.commons.Timer;
import jp.gr.java_conf.kumagusu.control.fragment.ProgressDialogFragment;
import android.app.Activity;
import android.app.Application;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

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
        MainApplication application = (MainApplication) act.getApplication();

        return application;
    }

    /**
     * 最新のアクティビティ.
     */
    private FragmentActivity currentActivity = null;

    /**
     * 最新のアクティビティを設定する.
     *
     * @param act 最新のアクティビティ
     */
    public void setCurrentActivity(FragmentActivity act)
    {
        this.currentActivity = act;
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
        if (getPasswordList().contains(pass))
        {
            getPasswordList().remove(pass);
        }

        getPasswordList().add(pass);
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
     * パスワードタイマー.
     */
    private Timer passwordTimer = null;

    /**
     * パスワードタイマーを帰す.
     *
     * @return パスワードタイマー
     */
    public Timer getPasswordTimer()
    {
        if (this.passwordTimer == null)
        {
            this.passwordTimer = new Timer(this.currentActivity);
        }

        return this.passwordTimer;
    }

    /**
     * 縦横切り替え表示に対応していないダイアログの表示数.
     */
    private int oldStyleDialogCounter = 0;

    /**
     * 縦横切り替え表示に対応していないダイアログの表示数を増やす.
     */
    public void incrementOldStyleDialogCounter()
    {
        this.oldStyleDialogCounter++;
    }

    /**
     * 縦横切り替え表示に対応していないダイアログの表示数を減らす.
     */
    public void decrementOldStyleDialogCounter()
    {
        this.oldStyleDialogCounter--;

        assert (this.oldStyleDialogCounter >= 0);
    }

    /**
     * 縦横切り替え表示に対応していないダイアログが表示中かを返す.
     *
     * @return 表示中ならtrue
     */
    public boolean isPlusOldStyleDialogCounter()
    {
        return (this.oldStyleDialogCounter > 0);
    }

    /**
     * プログレスダイアログが表示状態（trueなら表示中）.
     */
    private boolean displayingProgressDialog = false;

    /**
     * プログレスダイアログの表示状態を返す.
     *
     * @param dialog 表示中のダイアログ
     * @return プログレスダイアログの表示状態
     */
    public boolean continueDisplayingProgressDialog(ProgressDialogFragment dialog)
    {
        synchronized (getLockObject("ProgressDialog"))
        {
            if (!this.displayingProgressDialog)
            {
                dialog.dismiss();
            }

            return this.displayingProgressDialog;
        }
    }

    /**
     * プログレスダイアログを表示する.
     *
     * @param iconId アイコンID
     * @param titleId タイトルID
     * @param messageId メッセージID
     * @param cancelable キャンセル可否（trueのとき可）
     * @return プログレスダイアログ
     */
    public ProgressDialogFragment showProgressDialog(int iconId, int titleId, int messageId, boolean cancelable)
    {
        synchronized (getLockObject("ProgressDialog"))
        {
            dismissProgressDialog();

            ProgressDialogFragment dialog = null;

            try
            {
                dialog = ProgressDialogFragment.newInstance(iconId, titleId, messageId, cancelable);
                dialog.show(this.currentActivity.getSupportFragmentManager(), "");

                this.displayingProgressDialog = true;
            }
            catch (Exception e)
            {
                Log.d("MainApplication", "Progress dialog show error", e);
            }

            return dialog;
        }
    }

    /**
     * プログレスダイアログを消去する.
     */
    public void dismissProgressDialog()
    {
        dismissProgressDialog(null);
    }

    /**
     * プログレスダイアログを消去する.
     *
     * @param targetDialog 消去対象のプログレスダイアログ
     */
    public void dismissProgressDialog(ProgressDialogFragment targetDialog)
    {
        synchronized (getLockObject("ProgressDialog"))
        {
            ProgressDialogFragment dialog = getProgressDialog();

            if ((dialog != null) && ((targetDialog == null) || (targetDialog.equals(dialog))))
            {
                try
                {
                    dialog.dismiss();
                }
                catch (Exception ex)
                {
                    Log.d("MainApplication", "Dialog dismiss error.", ex);
                }

                this.displayingProgressDialog = false;
            }
        }
    }

    /**
     * 表示中プログレスダイアログ.
     */
    private ProgressDialogFragment progressDialog = null;

    /**
     * 表示中プログレスダイアログを返す.
     *
     * @return 表示中プログレスダイアログ
     */
    public ProgressDialogFragment getProgressDialog()
    {
        synchronized (getLockObject("ProgressDialog"))
        {
            ProgressDialogFragment resultDialog = null;

            if (this.progressDialog != null)
            {
                if ((!this.progressDialog.isDetached()) && (!this.progressDialog.isRemoving()))
                {
                    resultDialog = this.progressDialog;
                }
            }

            return resultDialog;
        }
    }

    /**
     * 表示中プログレスダイアログを設定する.
     *
     * @param dialog 表示中プログレスダイアログ
     */
    public void setProgressDialog(ProgressDialogFragment dialog)
    {
        synchronized (getLockObject("ProgressDialog"))
        {
            // すでに表示中のダイアログがあれば消去
            ProgressDialogFragment oldDialog = getProgressDialog();

            if ((dialog != null) && (oldDialog != null) && (!oldDialog.equals(dialog)))
            {
                dismissProgressDialog();
            }

            // 保存
            this.progressDialog = dialog;
        }
    }

    /**
     * ロックオブジェクト.
     */
    private HashMap<String, Object> lockObjects = new HashMap<String, Object>();

    /**
     * ロックオブジェクトを取得する.
     *
     * @param name ロックオブジェクト名
     * @return ロックオブジェクト
     */
    public Object getLockObject(String name)
    {
        Object obj = this.lockObjects.get(name);

        if (obj == null)
        {
            obj = new Object();
            this.lockObjects.put(name, obj);
        }

        return obj;
    }

    /**
     * エディタでメモを更新しているか?
     */
    private boolean updateMemo = false;

    /**
     * エディタでメモを更新しているかを返す.
     *
     * @return エディタでメモを更新しているときtrue
     */
    public boolean isUpdateMemo()
    {
        return this.updateMemo;
    }

    /**
     * エディタでメモを更新しているかを設定する.
     *
     * @param update エディタでメモを更新しているか?
     */
    public void setUpdateMemo(boolean update)
    {
        this.updateMemo = update;
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
