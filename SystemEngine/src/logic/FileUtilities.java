package logic;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtilities {
    // check 3.1
    public static boolean isFileXMLAndExists(String i_FullPath) {
        boolean isFileXMLAndExists = false;

        if (i_FullPath.endsWith(".xml")) {
            File file = new File(i_FullPath);

            if (file.exists()) {
                isFileXMLAndExists = true;
            }
        }

        return isFileXMLAndExists;
    }

    public static boolean isFileXML(String i_FullPath){
        return i_FullPath.endsWith(".xml");
    }

    public static void zip(String i_FileToZipName, String i_Content, String i_ZipPath) throws IOException {

        File f = new File(i_ZipPath);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        ZipEntry e = new ZipEntry(i_FileToZipName);
        out.putNextEntry(e);

        byte[] data = i_Content.getBytes(Charset.forName("UTF-8"));
        out.write(data, 0, data.length);
        out.closeEntry();

        out.close();
    }

    public static String unZip(String i_ZipPath) throws IOException {
        FileInputStream fis = new FileInputStream(i_ZipPath);
        ZipInputStream zis = new ZipInputStream(fis);
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];

        while (zis.getNextEntry() != null) {
            zis.read(buffer, 0, buffer.length);
            sb.append(new String(buffer));
        }

        fis.close();
        zis.close();

        return sb.toString();
    }

    public static void writeToFile(String i_FileName, String i_Content) throws IOException{
        try (Writer out1 = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(i_FileName), "UTF-8")))
        {
            out1.write(i_Content);
        }
        catch (IOException e) {
            throw e;
        }
    }

    public static List<String> getFilenamesOfAllFilesInAFolder(String i_FolderFullPath) {
        List<String> result = new ArrayList<String>();

        File[] files = new File(i_FolderFullPath).listFiles();

        for (File file : files) {
            if (file.isFile()) {
                result.add(file.getName());
            }
        }

        return result;
    }

    public static List<String> getFilesAndFoldersNamesOfAllFilesInAFolder(String i_FolderFullPath) {
        List<String> result = new ArrayList<String>();

        File[] files = new File(i_FolderFullPath).listFiles();

        for (File file : files) {
            result.add(file.getName());
        }

        return result;
    }

    public static String readFileAsString(String i_FilePaths)throws IOException {
       return new String(Files.readAllBytes(Paths.get(i_FilePaths)));
    }

    public static boolean isFileFolderAndExists(String i_FullPath){
        boolean isFileFolderAndExists = false;
        File file = new File(i_FullPath);

        if (file.exists()) {
            if (file.isDirectory()) {
                isFileFolderAndExists = true;
            }
        }

        return isFileFolderAndExists;
    }

    public static boolean isFileExists(String i_FullPath){
        File file = new File(i_FullPath);

        return  file.exists();
    }

    public static boolean isRepositoryFileAlreadyExists(String i_RepositoryFullPath){
        File file = new File(i_RepositoryFullPath + "//.magit");

        return file.exists();
    }

    public static  void createParentsFoldersByPath(String path) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        file.createNewFile();
    }

    public static void createFoldersByPathAndWriteContent(String i_Content, String i_Path) throws IOException {
        createParentsFoldersByPath(i_Path);
        writeToFile(i_Path, i_Content);
    }

    public static void copyDirectory(File i_Source, File i_Target) throws IOException {
        Files.copy(Paths.get(i_Source.getPath()), Paths.get(i_Target.getPath()));
        if (i_Source.isDirectory()) {
            File[] innerFiles = i_Source.listFiles();
            if (innerFiles != null) {
                for (File innerFile : innerFiles) {
                    copyDirectory(innerFile, new File(i_Target + "\\" + innerFile.getName()));
                }
            }
        }
    }

    public static void deleteDir(String i_Path) throws IOException {
        Path rootPath = Paths.get(i_Path);
        Files.walk(rootPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}

