package jp.gr.java_conf.kumagusu.memoio;

import java.io.File;

import android.content.Context;

/**
 * メモ.
 *
 * @author tarshi
 */
abstract class AbstractMemo implements IMemo
{
    /**
     * メモの保存フォルダオブジェクト.
     */
    private File folderFile;

    /**
     * エンコーディング名.
     */
    private String encodingName;

    /**
     * メモ内容とタイトルをリンクするか？.
     */
    private boolean titleLinkFg;

    /**
     * メモのFileオブジェクト.
     */
    private File memoFile;

    /**
     * メモ種別.
     */
    private MemoType memoType;

    /**
     * コンテキスト.
     */
    private Context context;

    /**
     * 既存ファイルを使用する場合（表示時）のコンストラクタ.
     *
     * @param con コンテキスト
     * @param mFileOrDir メモまたはメモフォルダのFileオブジェクト
     * @param encName エンコーディング名
     * @param tlkFg ファイルの一行目とタイトルを連動するか
     * @param type メモ種別
     */
    AbstractMemo(Context con, File mFileOrDir, String encName, boolean tlkFg, MemoType type)
    {
        this.context = con;
        this.encodingName = encName;
        this.titleLinkFg = tlkFg;
        this.memoType = type;

        if (mFileOrDir.isFile()) // メモ
        {
            this.memoFile = mFileOrDir;
            this.folderFile = this.memoFile.getParentFile();
        }
        else if (mFileOrDir.isDirectory()) // フォルダ
        {
            this.memoFile = null;
            this.folderFile = mFileOrDir;
        }
        else
        {
            throw new IllegalArgumentException("not file or directory.");
        }
    }

    @Override
    public MemoType getMemoType()
    {
        return this.memoType;
    }

    /**
     * メモの保存フォルダオブジェクトを返す.
     *
     * @return メモの保存フォルダオブジェクト
     */
    protected File getFolderFile()
    {
        return folderFile;
    }

    /**
     * エンコーディング名を返す.
     *
     * @return エンコーディング名
     */
    protected String getEncodingName()
    {
        return encodingName;
    }

    /**
     * メモ内容とタイトルをリンクするか?
     *
     * @return trueのときメモ内容とタイトルをリンクする
     */
    protected boolean isTitleLinkFg()
    {
        return titleLinkFg;
    }

    /**
     * メモのFileオブジェクトを返す.
     *
     * @return メモのFileオブジェクト
     */
    protected File getMemoFile()
    {
        return memoFile;
    }

    /**
     * メモのFileオブジェクトを設定する.
     *
     * @param file メモのFileオブジェクト
     */
    protected void setMemoFile(File file)
    {
        this.memoFile = file;
    }

    /**
     * コンテキストを返す.
     *
     * @return コンテキスト
     */
    protected Context getContext()
    {
        return context;
    }
}
