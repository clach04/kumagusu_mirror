package jp.gr.java_conf.kumagusu.memoio;

/**
 * メモのインタフェース.
 *
 * @author tarshi
 *
 */
public interface IMemo
{
    /**
     * メモのタイトルまたはフォルダ名を取得する。メモ／フォルダ以外の場合は、 “（不明）”を返す.
     *
     * @return タイトル
     */
    String getTitle();

    /**
     * メモ／フォルダのパスを取得する。メモ／フォルダ以外でもパスを返す.
     *
     * @return ファイル名
     */
    String getPath();

    /**
     * メモ／フォルダのファイル名を取得する。メモ／フォルダ以外でもファイル名を返す.
     *
     * @return メモ／フォルダのファイル名
     */
    String getName();

    /**
     * メモ種別を取得する.
     *
     * @return メモ種別
     */
    MemoType getMemoType();

    /**
     * メモの詳細を取得する.
     *
     * @return メモ詳細
     */
    String getDetails();

    /**
     * 親フォルダを取得する.
     *
     * @return 親フォルダ
     */
    String getParent();

    /**
     * 更新日時を取得する.
     *
     * @return 更新日時
     */
    long lastModified();

    /**
     * サイズを返す.
     *
     * @return サイズ
     */
    long length();
}
