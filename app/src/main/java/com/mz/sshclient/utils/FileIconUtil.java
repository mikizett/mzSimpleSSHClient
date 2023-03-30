package com.mz.sshclient.utils;

import com.mz.sshclient.ssh.sftp.filesystem.FileInfo;
import com.mz.sshclient.ssh.sftp.filesystem.FileType;

import java.util.Locale;
import java.util.regex.Pattern;

public final class FileIconUtil {

    private FileIconUtil() {}

    private static final String PATTERN_FILE_ARCHIVE_EXTENSION = ".*(\\.zip|\\.tar|\\.tgz|\\.gz|\\.bz2|\\.tbz2|\\.tbz|\\.txz|\\.xz)$";
    private static final String PATTERN_FILE_AUDIO_EXTENSION = ".*(\\.mp3|\\.aac|\\.mp2|\\.wav|\\.flac|\\.mpa|\\.m4a)$";
    private static final String PATTERN_FILE_CODE_EXTENSION = ".*(\\.c|\\.js|\\.cpp|\\.java|\\.cs|\\.py|\\.pl|\\.rb|\\.sql|\\.go|\\.ksh|\\.css|\\.scss|\\.html|\\.htm|\\.ts|\\.sh)$";
    private static final String PATTERN_FILE_EXCEL_EXTENSION = ".*(\\.xls|\\.xlsx)$";
    private static final String PATTERN_FILE_IMAGE_EXTENSION = ".*(\\.jpg|\\.jpeg|\\.png|\\.ico|\\.gif|\\.svg)$";
    private static final String PATTERN_FILE_VIDEO_EXTENSION = ".*(\\.mp4|\\.mkv|\\.m4v|\\.avi)$";
    private static final String PATTERN_FILE_PDF_EXTENSION = ".*(\\.pdf)$";
    private static final String PATTERN_FILE_POWER_POINT_EXTENSION = ".*(\\.ppt|\\.pptx)$";
    private static final String PATTERN_FILE_WORD_EXTENSION = ".*(\\.doc|\\.docx)$";

    public enum FontAwesomeEnum {

    }

    public static String getIconForType(FileInfo ent) {
        if (ent.getType() == FileType.Directory || ent.getType() == FileType.DirLink) {
            return AwesomeFontEnum.FA_FOLDER.getAwesomeFontType();
        }
        String name = ent.getName().toLowerCase(Locale.ENGLISH);

        if (Pattern.compile(PATTERN_FILE_ARCHIVE_EXTENSION).matcher(name).find()) {
            return AwesomeFontEnum.FA_FILE_ARCHIVE_O.getAwesomeFontType();
        }

        if (Pattern.compile(PATTERN_FILE_AUDIO_EXTENSION).matcher(name).find()) {
            return AwesomeFontEnum.FA_FILE_AUDIO_O.getAwesomeFontType();
        }

        if (Pattern.compile(PATTERN_FILE_CODE_EXTENSION).matcher(name).find()) {
            return AwesomeFontEnum.FA_FILE_CODE_O.getAwesomeFontType();
        }

        if (Pattern.compile(PATTERN_FILE_EXCEL_EXTENSION).matcher(name).find()) {
            return AwesomeFontEnum.FA_FILE_EXCEL_O.getAwesomeFontType();
        }

        if (Pattern.compile(PATTERN_FILE_IMAGE_EXTENSION).matcher(name).find()) {
            return AwesomeFontEnum.FA_FILE_IMAGE_O.getAwesomeFontType();
        }

        if (Pattern.compile(PATTERN_FILE_VIDEO_EXTENSION).matcher(name).find()) {
            return AwesomeFontEnum.FA_FILE_VIDEO_O.getAwesomeFontType();
        }

        if (Pattern.compile(PATTERN_FILE_PDF_EXTENSION).matcher(name).find()) {
            return AwesomeFontEnum.FA_FILE_PDF_O.getAwesomeFontType();
        }

        if (Pattern.compile(PATTERN_FILE_POWER_POINT_EXTENSION).matcher(name).find()) {
            return AwesomeFontEnum.FA_FILE_POWERPOINT_O.getAwesomeFontType();
        }

        if (Pattern.compile(PATTERN_FILE_WORD_EXTENSION).matcher(name).find()) {
            return AwesomeFontEnum.FA_FILE_WORD_O.getAwesomeFontType();
        }
        return AwesomeFontEnum.FA_FILE.getAwesomeFontType();
    }

}
