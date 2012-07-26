package jp.gr.java_conf.kumagusu.memoio;

/**
 * メモ種別.
 *
 * @author tarshi
 */
public enum MemoType
{
    /**
     * (txt)プレーンテキスト.
     */
    Text("Text", 1, "txt"),

    /**
     * (chi)内容暗号化.
     */
    Secret1("Secret1", 2, "chi"),

    /**
     * (chs)内容暗号化かつファイル名暗号化.
     */
    Secret2("Secret2", 3, "chs"),

    /**
     * 親フォルダ.
     */
    ParentFolder("ParentFolder", 4, ""),

    /**
     * 下位フォルダ.
     */
    Folder("Folder", 5, ""),

    /**
     * 不明.
     */
    None("None", 9, "");

    /**
     * メモ種別名称.
     */
    private final String typeName;

    /**
     * メモ種別ID.
     */
    private final int typeId;

    /**
     * メモ拡張子.
     */
    private final String fileExt;

    /**
     * メモ種別を初期化する.
     * @param tName メモ種別名称
     * @param tNumber メモ種別ID
     * @param ext メモ拡張子
     */
    private MemoType(String tName, int tNumber, String ext)
    {
        this.typeName = tName;
        this.typeId = tNumber;
        this.fileExt = ext;
    }

    /**
     * メモ種別名称を返す.
     * @return メモ種別名称
     */
    public String getTypeName()
    {
        return this.typeName;
    }

    /**
     * メモ種別IDを返す.
     * @return メモ種別ID
     */
    public int getTypeId()
    {
        return this.typeId;
    }

    /**
     * メモ拡張子を返す.
     * @return メモ拡張子
     */
    public String getFileExt()
    {
        return this.fileExt;
    }
}
