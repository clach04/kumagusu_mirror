package jp.gr.java_conf.kumagusu.memoio;

import java.io.File;
import java.util.Date;

import jp.gr.java_conf.kumagusu.R;

import android.content.Context;
import android.content.res.Configuration;

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

        if (this.getMemoType() == MemoType.Folder)
        {
            title = getName();
        }
        else if (this.getMemoType() == MemoType.ParentFolder)
        {
            title = "..";
        }

        if ((title == null) || (title.length() == 0))
        {
            title = this.getContext().getResources().getString(R.string.etc_memo_file_untitled_4_title);
        }

        return title;
    }

    @Override
    public String getName()
    {
        return (this.getFolderFile() != null) ? this.getFolderFile().getName() : null;
    }

    @Override
    public String getDetails()
    {
        StringBuilder sb = new StringBuilder();

        long modifyTime = lastModified();

        if (modifyTime != 0)
        {
            Date lastModifyDate = new Date(modifyTime);

            sb.append(MemoUtilities.formatDateTime(getContext(), lastModifyDate, true));
        }

        Configuration conf = getContext().getResources().getConfiguration();

        if (conf != null)
        {
            if (conf.orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                if (sb.length() > 0)
                {
                    sb.append(", ");
                }

                sb.append(MemoUtilities.type2Name(getContext(), getMemoType()));
            }
        }

        return sb.toString();
    }

    @Override
    public String getPath()
    {
        return (this.getFolderFile() != null) ? this.getFolderFile().getAbsolutePath() : null;
    }

    @Override
    public String getParent()
    {
        if (this.getFolderFile() != null)
        {
            return this.getFolderFile().getParent();
        }
        else
        {
            return null;
        }
    }

    @Override
    public long lastModified()
    {
        return (this.getFolderFile() != null) ? this.getFolderFile().lastModified() : 0;
    }

    @Override
    public long length()
    {
        return 0;
    }
}
