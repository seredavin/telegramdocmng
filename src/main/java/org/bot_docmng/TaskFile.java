package org.bot_docmng;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Base64;

public class TaskFile {
    private String name;
    private String uid;
    private String telegramFileId = "";
    private FileInputStream fileInputStream;
    private String path;

    TaskFile(String uid) {
        this.uid = uid;
//        String outFile = new Database().getFile(uid);
//        if (outFile == null) {
//            File file = saveTempFile(getFileByUid(uid));
//            this.path = file.getAbsolutePath();
//            this.name = file.getName();
//            try {
//                this.fileInputStream = new FileInputStream(this.path);
//            } catch (FileNotFoundException e) {
//                BotLogger.error(e.getMessage());
//            }
//        }
//        else {
//            this.name = "";
//            this.telegramFileId = outFile;
//        }
    }

    private static JsonTaskFile getFileByUid(String uid) {
        String json = null;
        try {
            json = Connector.getRequest(null, "file/" + uid);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            BotLogger.error(e.getMessage());
        }
        Gson gson = new Gson();
        return gson.fromJson(json, JsonTaskFile.class);
    }

    String getTelegramFileId() {
        String outFile = new Database().getFile(uid);
        if (outFile != null) {
            this.name = "";
            this.telegramFileId = outFile;
        }
        return telegramFileId;
    }

    String getUid() {
        return uid;
    }

    FileInputStream getFileInputStream() {
        return fileInputStream;
    }

    String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    private File saveTempFile(JsonTaskFile jsonTaskFile) {
        byte[] decodeBytes = Base64.getDecoder().decode(
                jsonTaskFile.file_string.replace("\r\n", ""));
        File file = new File("tmp/", jsonTaskFile.file_name);
        if (!file.exists()) {
            try {
                file.createNewFile();
                Files.write(file.toPath(), decodeBytes);
            } catch (IOException e) {
                BotLogger.error(e.getMessage());
            }
        }
        return file;
    }

    void deleteFile() {
        if (path != null) {
            File file = new File(path);
            file.delete();
        }
    }

    void load() {
        String outFile = new Database().getFile(uid);
        if (outFile == null) {
            File file = saveTempFile(getFileByUid(uid));
            this.path = file.getAbsolutePath();
            this.name = file.getName();
            try {
                this.fileInputStream = new FileInputStream(this.path);
            } catch (FileNotFoundException e) {
                BotLogger.error(e.getMessage());
            }
        }
    }
}
