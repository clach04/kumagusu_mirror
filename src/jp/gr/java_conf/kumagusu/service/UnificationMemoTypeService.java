package jp.gr.java_conf.kumagusu.service;

import java.io.File;
import java.io.FileNotFoundException;

import jp.gr.java_conf.kumagusu.MainApplication;
import jp.gr.java_conf.kumagusu.memoio.IMemo;
import jp.gr.java_conf.kumagusu.memoio.MemoBuilder;
import jp.gr.java_conf.kumagusu.memoio.MemoFile;
import jp.gr.java_conf.kumagusu.memoio.MemoType;
import jp.gr.java_conf.kumagusu.memoio.MemoUtilities;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * メモ種別・パスワード統一処理サービス.
 *
 * @author tarshi
 *
 */
public class UnificationMemoTypeService extends IntentService
{
    /**
     * メモビルダ.
     */
    private MemoBuilder memoBuilder;

    /**
     * 入力済みの旧パスワード.
     */
    private String[] oldPasswords;

    /**
     * 新しい統一パスワード.
     */
    private String newPassword;

    /**
     * 新しいメモ種別.
     */
    private MemoType memoType;

    /**
     * コンストラクタ（自動生成されたもの）.
     *
     * @param name サービス名
     */
    public UnificationMemoTypeService(String name)
    {
        super(name);
    }

    /**
     * コンストラクタ(Activityから呼び出す).
     */
    public UnificationMemoTypeService()
    {
        this("UnificationMemoTypeService");
    }

    @Override
    protected final void onHandleIntent(Intent intent)
    {
        Log.d("UnificationMemoTypeService", "*** Start onHandleIntent()");

        // 開始通知
        sendBCast(UnificationMemoTypeResponseReceiver.ACTION_STATUS_START, null, true);
        MainApplication.getInstance(getApplication()).setUnificationMemoTypeServiceExecute(true);

        boolean finishResult = false;

        try
        {
            // パラメータ取得
            String currentFolder = intent.getStringExtra("currentFolder");
            this.oldPasswords = intent.getStringArrayExtra("oldPasswords");
            if (this.oldPasswords == null)
            {
                this.oldPasswords = new String[0];
            }
            this.newPassword = intent.getStringExtra("newPassword");
            String encodeName = intent.getStringExtra("encodeName");
            boolean memoCrypto = intent.getBooleanExtra("memoCrypto", true);
            boolean memoTitleLink = intent.getBooleanExtra("memoTitleLink", true);
            boolean memoRandamName = intent.getBooleanExtra("memoRandamName", false);

            // メモ種別決定
            if (memoCrypto)
            {
                this.memoType = (memoRandamName) ? MemoType.Secret2 : MemoType.Secret1;
            }
            else
            {
                this.memoType = MemoType.Text;
            }

            // 暗号化指定でパスワードが空
            if ((memoCrypto) && ((newPassword == null) || (newPassword.length() == 0)))
            {
                sendBCast(UnificationMemoTypeResponseReceiver.ACTION_STATUS_FINISH, null, false);
                return;
            }

            // メモファイル生成ビルダ
            this.memoBuilder = new MemoBuilder(this.getBaseContext(), encodeName, memoTitleLink);

            // メモファイル変換
            findMemoFile(new File(currentFolder), 0);

            finishResult = true;
        }
        finally
        {
            // 終了通知
            MainApplication.getInstance(getApplication()).setUnificationMemoTypeServiceExecute(false);
            sendBCast(UnificationMemoTypeResponseReceiver.ACTION_STATUS_FINISH, null, finishResult);
        }

    }

    /**
     * フォルダ階層最大値.
     */
    private static final int FOLDER_LEVEL_MAX = 50;

    /**
     * フォルダ内のメモファイルを検索し、指定種別・パスワードを設定する.
     *
     * @param targetFolderFile メモファイルを取得するフォルダ
     * @param level 階層(0～)
     */
    private void findMemoFile(File targetFolderFile, int level)
    {
        Log.d("UnificationMemoTypeService", "*** Start findMemoFile() level:" + level);

        if (level >= FOLDER_LEVEL_MAX)
        {
            Log.w("UnificationMemoTypeService", "folder level overflow (level > 50)");
            return;
        }

        // フォルダが存在しない場合終了
        if ((!targetFolderFile.exists()) || (!targetFolderFile.isDirectory()))
        {
            return;
        }

        // カレントフォルダ内のメモを変換
        File[] files = targetFolderFile.listFiles();

        for (File file : files)
        {
            if (file.isDirectory())
            {
                // 下位フォルダ検索
                findMemoFile(file.getAbsoluteFile(), level + 1);
            }
            else
            {
                IMemo memo;
                try
                {
                    memo = this.memoBuilder.buildFromFile(file.getAbsolutePath(), this.oldPasswords);
                }
                catch (Exception ex)
                {
                    memo = null;
                }

                if ((memo == null) || (memo.getMemoType() == MemoType.None) || (!(memo instanceof MemoFile)))
                {
                    Log.d("UnificationMemoTypeService", "Not decrypt file:" + memo.getPath());
                    continue;
                }

                // 更新通知
                sendBCast(UnificationMemoTypeResponseReceiver.ACTION_STATUS_UPDATE, memo.getName(), true);

                // メモタイプ変更
                changeMemoType((MemoFile) memo);
            }
        }
    }

    /**
     * メモのメモ種別を変更する.
     *
     * @param srcMemoFile 変更するメモ
     */
    private void changeMemoType(MemoFile srcMemoFile)
    {
        // 元ファイル読み込み
        String srcMemoData = srcMemoFile.getText();

        // メモを出力（更新日時は変更しない）
        MemoFile dstMemoFile;
        try
        {
            dstMemoFile = (MemoFile) this.memoBuilder.build(srcMemoFile.getParent(), this.memoType, this.oldPasswords);

            if (dstMemoFile.setText(this.newPassword, srcMemoData, srcMemoFile.lastModified()))
            {
                // メモ種別の変更に成功した場合、元のファイルを削除
                if (!srcMemoFile.getPath().equals(dstMemoFile.getPath()))
                {
                    MemoUtilities.deleteFile(srcMemoFile.getPath());
                }
            }
        }
        catch (FileNotFoundException ex)
        {
            // 新しい種別のメモファイルの作成に失敗
            Log.w("UnificationMemoTypeService", " New type memo creating failed", ex);
        }
    }

    /**
     * サービス状態を通知する.
     *
     * @param status 通知状態
     * @param memoFileName メモファイル名（更新通知のみ）
     * @param result 結果（終了通知のみ）
     */
    private void sendBCast(int status, String memoFileName, boolean result)
    {
        Log.d("UnificationMemoTypeService", "*** Start sendBCast() status:" + status);

        Intent intent = new Intent();
        intent.setAction(UnificationMemoTypeResponseReceiver.ACTION_RESPONSE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("status", status);
        intent.putExtra("memoFileName", memoFileName);
        intent.putExtra("finishResult", result);

        sendBroadcast(intent);
    }
}
