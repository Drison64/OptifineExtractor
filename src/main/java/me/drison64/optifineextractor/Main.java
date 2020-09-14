package me.drison64.optifineextractor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Arrays;
import java.util.Iterator;

public class Main {

    public static void main(String[] args ) {
        new Main().init();
    }

    public void init() {
        System.out.println(Main.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        File mcDir = getFile("/minecraft");

        String token = null;
        try {
            URLConnection url = new URL("https://optifine.net/adloadx?f=OptiFine_1.16.2_HD_U_G3.jar").openConnection();
            url.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.getInputStream()));
            String line;

            while ((line = br.readLine()) != null) {

                if (line.contains("onclick='onDownload()'")) {

                    System.out.println(line);

                    System.out.println(line.split("'")[1].split("=")[2]);

                    token = line.split("'")[1].split("=")[2];

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Gson gson = new Gson();

        JsonObject versionsObject = null;
        try {
            URL url = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());

            versionsObject = gson.fromJson(inputStreamReader, JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        JsonObject version = getVersionObject(versionsObject, "1.16.2");

        JsonObject versionObject = null;
        try {
            URL url = new URL(version.get("url").getAsString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());

            versionObject = gson.fromJson(inputStreamReader, JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        //TODO GETS MINECRAFT CLIENT URL
        //TODO NOW JUST DOWNLOAD IT
        //TODO AND DONE IG HELP

        try {
            URL url = new URL(versionObject.get("downloads").getAsJsonObject().get("client").getAsJsonObject().get("url").getAsString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
            System.out.println("Length: " + connection.getContentLength());
            InputStream in = connection.getInputStream();
            System.out.println(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            File file = getFile("/minecraft/versions/1.16.2/1.16.2.jar");
            file.getParentFile().mkdirs();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] dataBuffer = new byte[16384];
            int bytesRead;
            int bytesProgress = 0;
            while ((bytesRead = in.read(dataBuffer)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                bytesProgress = bytesProgress + bytesRead;
                System.out.println(bytesProgress + "/" + connection.getContentLength());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        File optifine = getFile("/minecraft/OptiFine_1.16.2_HD_U_G3.jar");

        try {
            URL url = new URL("https://optifine.net/downloadx?f=OptiFine_1.16.2_HD_U_G3.jar&x=" + token);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
            System.out.println("Length: " + connection.getContentLength());
            InputStream in = connection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(optifine);
            byte[] dataBuffer = new byte[4096];
            int bytesRead;
            int bytesProgress = 0;
            while ((bytesRead = in.read(dataBuffer)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                bytesProgress = bytesProgress + bytesRead;
                System.out.println(bytesProgress + "/" + connection.getContentLength());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        URLClassLoader classLoader;
        try {
            classLoader = URLClassLoader.newInstance(new URL[] {optifine.toURI().toURL()});
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        Class<?> installerClass = null, utilsClass = null;
        try {
            installerClass = Class.forName("optifine.Installer", true, classLoader);
            utilsClass = Class.forName("optifine.Utils", true, classLoader);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        File dirMcLib = new File(mcDir, "libraries");
        File dirMcVers = new File(mcDir, "versions");

        Method getOFVersion = null, getOFEdition = null, tokenize = null, installOFlibrary = null;
        try {
            getOFVersion = installerClass.getMethod("getOptiFineVersion");
            getOFEdition = installerClass.getMethod("getOptiFineEdition", String[].class);
            tokenize = utilsClass.getMethod("tokenize", String.class, String.class);
            System.out.println(Arrays.toString(installerClass.getMethods()));
            installOFlibrary = installerClass.getDeclaredMethod("installOptiFineLibrary", String.class, String.class, File.class, boolean.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }
        getOFVersion.setAccessible(true);
        String ofVer, ofEd, mcVer, mcVerOf = null;
        String[] ofVers;
        try {
            ofVer = (String) getOFVersion.invoke(classLoader);
            ofVers = (String[]) tokenize.invoke(classLoader, ofVer, "_");
            ofEd = (String) getOFEdition.invoke(classLoader, (Object) ofVers);
            mcVer = ofVers[1];
            mcVerOf = mcVer + "-OptiFine_" + ofEd;
            installOFlibrary.setAccessible(true);
            installOFlibrary.invoke(classLoader, mcVer, ofEd, dirMcLib, false);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


    }

    private File getFile(String path) {
        File folder = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
        return new File(folder.getAbsolutePath() + path);
    }

    private JsonObject getVersionObject(JsonObject object, String version) {
        JsonObject versionObject = null;

        Iterator<JsonElement> versions = object.get("versions").getAsJsonArray().iterator();

        while (versions.hasNext()) {
            JsonObject version_ = versions.next().getAsJsonObject();

            if (version_.get("id").getAsString().equalsIgnoreCase(version)) {
                versionObject = version_;

                break;
            }
        }

        return versionObject;
    }

}
