package cn.fusionfish.sakuraplayer.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 文件实用类
 * @author JeremyHu
 */
public class FileUtil {

    /**
     * 获取文件夹目录下所有文件
     * @param dir 文件夹
     * @return 所有文件
     */
    public static List<File> getFiles(File dir){
        if(!dir.isDirectory() || !dir.exists()){
            return null;
        }

        return new ArrayList<>(Arrays.asList(Objects.requireNonNull(dir.listFiles())));
    }

    /**
     * 从文件读取Json
     * @param file 本地文件
     * @return Json对象
     */
    public static JsonObject getJson(File file) {
        if (!file.exists()){
            return null;
        }
        JsonObject json = new JsonObject();
        try {
            json = new JsonParser().parse(new JsonReader(new FileReader(file))).getAsJsonObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return json;
    }
}
