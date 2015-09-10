package jp.gr.java_conf.kumagusu.memoio;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.Context;

/**
 * メモの生成処理.
 *
 * @author tarshi
 *
 */
public final class MemoBuilder
{
    /**
     * コンテキスト.
     */
    private Context context;

    /**
     * エンコーディング名.
     */
    private String encodingName;

    /**
     * メモタイトルとファイル名を連動するか?
     */
    private boolean titleLinkFg;

    /**
     * メモの生成処理を初期化する.
     *
     * @param con コンテキスト
     * @param encName エンコーディング名
     * @param tlFg メモタイトルとファイル名を連動するか?
     */
    public MemoBuilder(Context con, String encName, boolean tlFg)
    {
        this.context = con;
        this.encodingName = encName;
        this.titleLinkFg = tlFg;
    }

    /**
     * 既存ファイルまたは既存フォルダからメモを生成する.
     *
     * @param filePath メモまたはメモフォルダの絶対パス
     * @return メモ
     * @throws FileNotFoundException メモまたはメモフォルダが存在しない
     */
    public IMemo buildFromFile(String filePath) throws FileNotFoundException
    {
        return buildFromFile(filePath, null);
    }

    /**
     * 既存ファイルまたは既存フォルダからメモを生成する.
     *
     * @param filePath メモまたはメモフォルダの絶対パス
     * @param passwds 入力済みパスワード
     * @return メモ
     * @throws FileNotFoundException メモまたはメモフォルダが存在しない
     */
    public IMemo buildFromFile(String filePath, String[] passwds) throws FileNotFoundException
    {
        File memoFile = new File(filePath);
        MemoType type;

        if (memoFile.exists())
        {
            type = MemoUtilities.getMemoType(memoFile);
        }
        else
        {
            throw new FileNotFoundException();
        }

        IMemo memo = build(filePath, type, passwds);

        return memo;
    }

    /**
     * メモを生成する.
     *
     * @param fileOrFolderPath メモまたはメモフォルダの絶対パス
     * @param type メモまたはメモフォルダのタイプ
     * @return メモ
     * @throws FileNotFoundException
     */
    public IMemo build(String fileOrFolderPath, MemoType type) throws FileNotFoundException
    {
        return build(fileOrFolderPath, type, null);
    }

    /**
     * メモを生成する.
     *
     * @param fileOrFolderPath メモまたはメモフォルダの絶対パス
     * @param type メモまたはメモフォルダのタイプ
     * @param passwds 入力済みパスワード
     * @return メモ
     * @throws FileNotFoundException
     */
    public IMemo build(String fileOrFolderPath, MemoType type, String[] passwds) throws FileNotFoundException
    {
        File memoFileOrFolder = new File(fileOrFolderPath);

        IMemo memo;

        switch (type)
        {
        case Folder:
        case ParentFolder:
            memo = new MemoFolder(this.context, memoFileOrFolder, this.encodingName, this.titleLinkFg, type);
            break;

        default:
            if (passwds == null)
            {
                memo = new MemoFile(this.context, memoFileOrFolder, this.encodingName, this.titleLinkFg, type);
            }
            else
            {
                memo = new MemoFile(this.context, memoFileOrFolder, this.encodingName, this.titleLinkFg, type, passwds);
            }
            break;
        }

        return memo;
    }
}
