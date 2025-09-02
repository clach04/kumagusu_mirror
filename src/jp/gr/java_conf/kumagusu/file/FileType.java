package jp.gr.java_conf.kumagusu.file;

/**
 * ファイル種別.
 *
 * @author tarshi
 */
public class FileType {
    /**
     * ファイル種別名称.
     */
    private final String typeName;

    /**
     * ファイル拡張子.
     */
    private final String fileExt;

    /**
     * MIMEタイプ.
     */
    private final String mimeType;

    /**
     * ファイル種別を初期化する.
     *
     * @param tName ファイル種別名称
     * @param ext ファイル拡張子
     * @param mime MIMEタイプ
     */
    public FileType(String tName, String ext, String mime) {
        this.typeName = tName;
        this.fileExt = ext;
        this.mimeType = mime;
    }

    /**
     * ファイル種別名称を返す.
     *
     * @return ファイル種別名称
     */
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * ファイル拡張子を返す.
     *
     * @return ファイル拡張子
     */
    public String getFileExt() {
        return this.fileExt;
    }

    /**
     * MIMEタイプを返す.
     *
     * @return MIMEタイプ
     */
    public String getMimeType() {
        return this.mimeType;
    }
}
