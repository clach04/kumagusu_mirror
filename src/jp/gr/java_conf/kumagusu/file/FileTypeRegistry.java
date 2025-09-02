package jp.gr.java_conf.kumagusu.file;

import java.util.ArrayList;
import java.util.List;

/**
 * ファイル種別レジストリ.
 *
 * @author tarshi
 */
public class FileTypeRegistry {
    /**
     * サポートするファイル種別のリスト.
     */
    private static final List<FileType> supportedFileTypes = new ArrayList<>();

    static {
        // プレーンテキスト
        supportedFileTypes.add(new FileType("Text", "txt", "text/plain"));
        // 内容暗号化
        supportedFileTypes.add(new FileType("Secret1", "chi", "application/octet-stream"));
        // 内容暗号化かつファイル名暗号化
        supportedFileTypes.add(new FileType("Secret2", "chs", "application/octet-stream"));
    }

    /**
     * サポートするファイル種別のリストを返す.
     *
     * @return サポートするファイル種別のリスト
     */
    public static List<FileType> getSupportedFileTypes() {
        return supportedFileTypes;
    }

    /**
     * ファイル拡張子からファイル種別を取得する.
     *
     * @param ext ファイル拡張子
     * @return ファイル種別
     */
    public static FileType getFileTypeByExt(String ext) {
        for (FileType type : supportedFileTypes) {
            if (type.getFileExt().equalsIgnoreCase(ext)) {
                return type;
            }
        }
        return null;
    }
}
