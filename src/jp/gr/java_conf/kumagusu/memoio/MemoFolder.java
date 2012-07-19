package jp.gr.java_conf.kumagusu.memoio;

import java.io.File;

import jp.gr.java_conf.kumagusu.R;

import android.content.Context;

/**
 * メモフォルダ.
 *
 * @author tarshi
 */
public final class MemoFolder extends AbstractMemo
{
    /**
     * 既存フォルダを使用する場合（表示時）のコンストラクタ.
     *
     * @param context コンテキスト
     * @param folderFile メモフォルダのFileオブジェクト
     * @param encodingName エンコーディング名
     * @param titleLinkFg ファイルの一行目とタイトルを連動するか
     * @param type メモ種別
     */
    MemoFolder(Context context, File folderFile, String encodingName, boolean titleLinkFg, MemoType type)
    {
        super(context, folderFile, encodingName, titleLinkFg, type);

        if ((type != MemoType.Folder) && (type != MemoType.ParentFolder))
        {
            throw new IllegalArgumentException("no memo folder type:" + type.toString());
        }
    }

    @Override
    public String getTitle()
    {
        String title = null;

        if (this.memoType == MemoType.Folder)
        {
            title = getName() + "/";
        }
        else if (this.memoType == MemoType.ParentFolder)
        {
            title = "..";
        }

        if ((title == null) || (title.length() == 0))
        {
            title = this.context.getResources().getString(R.string.etc_memo_type_none);
        }

        return title;
    }

    @Override
    public String getName()
    {
        return (this.folderFile != null) ? this.folderFile.getName() : null;
    }

    @Override
    public String getDetails()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(MemoUtilities.type2Name(getMemoType()));

        return sb.toString();
    }

    @Override
    public String getPath()
    {
        return (this.folderFile != null) ? this.folderFile.getAbsolutePath() : null;
    }

    @Override
    public String getParent()
    {
        if (this.folderFile != null)
        {
            return this.folderFile.getParent();
        }
        else
        {
            return null;
        }
    }
}
