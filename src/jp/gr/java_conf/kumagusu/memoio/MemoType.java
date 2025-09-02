package jp.gr.java_conf.kumagusu.memoio;

import jp.gr.java_conf.kumagusu.file.FileType;
import jp.gr.java_conf.kumagusu.file.FileTypeRegistry;

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
    Text("Text", 1, FileTypeRegistry.getFileTypeByExt("txt")),

    /**
     * (chi)内容暗号化.
     */
    Secret1("Secret1", 2, FileTypeRegistry.getFileTypeByExt("chi")),

    /**
     * (chs)内容暗号化かつファイル名暗号化.
     */
    Secret2("Secret2", 3, FileTypeRegistry.getFileTypeByExt("chs")),

    /**
     * 親フォルダ.
     */
    ParentFolder("ParentFolder", 4, null),

    /**
     * 下位フォルダ.
     */
    Folder("Folder", 5, null),

    /**
     * 不明.
     */
    None("None", 9, null);

    /**
     * メモ種別名称.
     */
    private final String typeName;

    /**
     * メモ種別ID.
     */
    private final int typeId;

    /**
     * ファイル種別.
     */
    private final FileType fileType;

    /**
     * メモ種別を初期化する.
     *
     * @param tName メモ種別名称
     * @param tNumber メモ種別ID
     * @param fType ファイル種別
     */
    private MemoType(String tName, int tNumber, FileType fType)
    {
        this.typeName = tName;
        this.typeId = tNumber;
        this.fileType = fType;
    }

    /**
     * メモ種別名称を返す.
     *
     * @return メモ種別名称
     */
    public String getTypeName()
    {
        return this.typeName;
    }

    /**
     * メモ種別IDを返す.
     *
     * @return メモ種別ID
     */
    public int getTypeId()
    {
        return this.typeId;
    }

    /**
     * メモ種別IDからメモ種別（Enum値）を取得する.
     *
     * @param id メモ種別ID
     * @return メモ種別
     */
    public static MemoType getMemoType(int id)
    {
        for (MemoType type : values())
        {
            if (type.getTypeId() == id)
            {
                return type;
            }
        }

        return MemoType.None;
    }

    /**
     * ファイル種別を返す.
     *
     * @return ファイル種別
     */
    public FileType getFileType()
    {
        return this.fileType;
    }

    /**
     * メモ拡張子を返す.
     *
     * @return メモ拡張子
     */
    public String getFileExt()
    {
        if (this.fileType == null) {
            return "";
        }
        return this.fileType.getFileExt();
    }
}
