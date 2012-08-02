package jp.gr.java_conf.kumagusu;

import java.util.Comparator;

import android.content.Context;

import jp.gr.java_conf.kumagusu.memoio.IMemo;
import jp.gr.java_conf.kumagusu.memoio.MemoType;
import jp.gr.java_conf.kumagusu.preference.MainPreferenceActivity;

/**
 * メモリストの並び替え処理.
 *
 * @author tarshi
 *
 */
public final class MemoListComparator implements Comparator<IMemo>
{
    /**
     * コンテキスト.
     */
    private Context context;

    /**
     * コンストラクタ.
     *
     * @param con コンテキスト
     */
    public MemoListComparator(Context con)
    {
        this.context = con;
    }

    /**
     * ソート項目を比較する.
     *
     * @param src 比較ベース
     * @param target 比較対象
     * @return 比較結果
     */
    public int compare(IMemo src, IMemo target)
    {
        int diff;

        if (src.getMemoType() == MemoType.ParentFolder)
        {
            diff = -1;
        }
        else if (target.getMemoType() == MemoType.ParentFolder)
        {
            diff = 1;
        }
        else if ((src.getMemoType() == MemoType.Folder) && (target.getMemoType() != MemoType.Folder))
        {
            diff = -1;
        }
        else if ((src.getMemoType() != MemoType.Folder) && (target.getMemoType() == MemoType.Folder))
        {
            diff = 1;
        }
        else
        {
            int method = MainPreferenceActivity.getMemoSortMethod(this.context);

            switch (method)
            {
            case MainPreferenceActivity.MEMO_SORT_METHOD_TITLE_ASC:
                diff = compareByTitle(src, target, false);
                break;

            case MainPreferenceActivity.MEMO_SORT_METHOD_TITLE_DESC:
                diff = compareByTitle(src, target, true);
                break;

            case MainPreferenceActivity.MEMO_SORT_METHOD_LAST_MODIFIED_ASC:
                diff = compareByLastModified(src, target, false);
                break;

            case MainPreferenceActivity.MEMO_SORT_METHOD_LAST_MODIFIED_DESC:
                diff = compareByLastModified(src, target, true);
                break;

            case MainPreferenceActivity.MEMO_SORT_METHOD_SIZE_ASC:
                diff = compareBySize(src, target, false);
                break;

            case MainPreferenceActivity.MEMO_SORT_METHOD_SIZE_DESC:
                diff = compareBySize(src, target, true);
                break;

            default:
                diff = compareByTitle(src, target, false);
            }

        }

        return diff;
    }

    /**
     * タイトルの比較値を返す.
     *
     * @param src 比較対象１
     * @param target 比較対象２
     * @param desc 結果を反転する場合true
     * @return 比較値
     */
    private int compareByTitle(IMemo src, IMemo target, boolean desc)
    {
        int diff = src.getTitle().compareToIgnoreCase(target.getTitle());

        if (desc)
        {
            diff *= -1;
        }

        return diff;
    }

    /**
     * 更新日の比較値を返す.
     *
     * @param src 比較対象１
     * @param target 比較対象２
     * @param desc 結果を反転する場合true
     * @return 比較値
     */
    private int compareByLastModified(IMemo src, IMemo target, boolean desc)
    {
        long lastModifiedDiff = src.lastModified() - target.lastModified();

        int diff;
        if (lastModifiedDiff < 0)
        {
            diff = -1;
        }
        else if (lastModifiedDiff > 0)
        {
            diff = 1;
        }
        else
        {
            diff = 0;
        }

        if (desc)
        {
            diff *= -1;
        }

        return diff;
    }

    /**
     * サイズの比較値を返す.
     *
     * @param src 比較対象１
     * @param target 比較対象２
     * @param desc 結果を反転する場合true
     * @return 比較値
     */
    private int compareBySize(IMemo src, IMemo target, boolean desc)
    {
        long lengthDiff = src.length() - target.length();

        int diff;
        if (lengthDiff < 0)
        {
            diff = -1;
        }
        else if (lengthDiff > 0)
        {
            diff = 1;
        }
        else
        {
            diff = 0;
        }

        if (desc)
        {
            diff *= -1;
        }

        return diff;
    }
}
