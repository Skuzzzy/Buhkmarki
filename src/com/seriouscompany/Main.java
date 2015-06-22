package com.seriouscompany;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Main {
    public static String bookmarks = "C:\\Users\\Daniel\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Bookmarks";
    public static String targetDirectory = "C:\\Youtube\\";
    public static String youtubedllocation = "C:\\Youtube\\youtube-dl.exe";

    public static Runtime rt = Runtime.getRuntime();

    public static void main(String[] args) {
        createDirectory(targetDirectory);
        readBookmarks();
    }

    public static void createDirectory(String directoryName)
    {
        File theDir = new File(directoryName);
        theDir.mkdir();
    }

    public static void processLink(String link, String fileLocation)
    {
        String trimmedLink = trimJSONString(link);
        if(trimmedLink.startsWith("https://www.youtube.com/watch")) // Matches youtube links
        {
            //processYoutubeLink();
            System.out.println(trimmedLink);
            String command =  youtubedllocation + " " + link + " -o \"" + fileLocation + "%(title)s-%(id)s.%(ext)s\"";

            try
            {
                System.out.println(command);
                Process pr = rt.exec(command);
                pr.waitFor(10, TimeUnit.SECONDS);
                Thread.sleep(500);
                //System.out.println("wait");
                //pr.waitFor();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }

    public static void processFolder(JsonObject folder, String path)
    {
        for(JsonElement innerElement : folder.getAsJsonArray("children")) {

            JsonObject elementObj = innerElement.getAsJsonObject();
            String type = trimJSONString(elementObj.getAsJsonPrimitive("type").toString());
            String name = trimJSONString(elementObj.getAsJsonPrimitive("name").toString());

            if(type.equals("url")) {
                // is a link
                System.out.println(path);
                processLink(innerElement.getAsJsonObject().getAsJsonPrimitive("url").toString(), path);

            } else if(type.equals("folder")) {
                // Is a folder

                System.out.println(path+name);
                processFolder(elementObj, path+name+"\\");
                //System.out.println(innerElement.getAsJsonObject().getAsJsonArray("children"));
            }
        }
    }

    public static void readBookmarks()
    {
        JsonReader file;
        try
        {
            file = new JsonReader(new InputStreamReader(new FileInputStream(bookmarks), "UTF-8"));
        }
        catch (Exception e)
        {
            System.out.println("EXCEPTION: IOException in readBookmarks");
            e.printStackTrace();
            return;
        }

        JsonParser jsonParser = new JsonParser();
        JsonObject result = jsonParser.parse(file).getAsJsonObject().getAsJsonObject("roots");
        Set<Map.Entry<String,JsonElement>> firstEntrySet = result.entrySet();
        for(Map.Entry<String,JsonElement> test : firstEntrySet) {

            JsonObject bars = result.getAsJsonObject(test.getKey());
            System.out.println(test.getKey());
            processFolder(bars, targetDirectory+test.getKey()+"\\");
        }
    }

    public static String trimJSONString(String jsonString)
    {
        return jsonString.substring(1, jsonString.length()-1);
    }
}
