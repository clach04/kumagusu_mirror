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
    protected File folderFile;

    /**
     * エンコーディング名.
     */
    protected String encodingName;

    /**
     * メモ内容とタイトルをリンクするか？.
     */
    protected boolean titleLinkFg;

    /**
     * メモのFileオブジェクト.
     */
    protected File memoFile;

    /**
     * メモ種別.
     */
    protected MemoType memoType;

    /**
     * コンテキスト.
     */
    protected Context context;

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
            this.folderFile = new File(this.memoFile.getParent());
        }
        else if (mFileOrDir.isDirectory()) // フォルダ
        {
            this.memoFile = null;
            this.folderFile = new File(mFileOrDir.getAbsolutePath());
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
}
