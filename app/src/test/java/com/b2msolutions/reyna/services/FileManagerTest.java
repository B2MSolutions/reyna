package com.b2msolutions.reyna.services;

import android.content.Context;

import com.b2msolutions.reyna.ReynaTestRunner;

import junit.framework.Assert;

import org.apache.maven.artifact.ant.shaded.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ReynaTestRunner.class)
public class FileManagerTest {

    protected FileManager fileManager;

    @Before
    public void setup(){
        this.fileManager = new FileManager();
    }

    @Test
    public void whenCallingGetFileShouldReturnCorrectFile(){
        File file = this.fileManager.getFile("/somepath");

        assertNotNull(file);
        assertEquals("/somepath", file.getPath());
    }

    @Test
    public void whenCallingCopyShouldCreateNewCopyOfProvidedFile() throws IOException {
        //data preparation

        File originalFile = null;
        File copiedFile = null;
        try {
            originalFile = createFile("temp.data");

            //test execution
            this.fileManager.copy("temp.data", "copy.data");
            copiedFile = new File("copy.data");

            assertTrue(copiedFile.exists());
            assertEquals(originalFile.length(), copiedFile.length());
        }
        finally {
            //cleanup
            if(originalFile != null) {
                originalFile.delete();
            }
            if(copiedFile != null){
                copiedFile.delete();
            }
        }
    }

    private File createFile(String path) throws IOException {
        File file = new File(path);
        if(!file.exists()) {
            file.createNewFile();
        }

        OutputStream outputStream = new FileOutputStream(file);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        outputStreamWriter.write("Hello, world!" + Calendar.getInstance().getTime());
        outputStreamWriter.close();
        return file;
    }

    @Test
    public void whenCallingDeleteDatabaseShouldRemoveAllRelatedFilesOfProvidedDatabase() throws IOException {
        File databases = new File("databases");
        try {
            if(databases.exists()){
                databases.delete();
            }
            databases.mkdir();

            createDbStructure("reyna.db", "databases");
            createDbStructure("other.db", "databases");

            this.fileManager.deleteDatabase("databases/reyna.db");

            assertEquals(7, databases.list().length);
            assertEquals(0, databases.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.startsWith("reyna.db");
                }
            }).length);
        }
        finally {
            FileUtils.cleanDirectory(databases);
            databases.delete();
        }
    }

    private void createDbStructure(String dbName, String path) throws IOException {
        String[] toCreate = new String[]{"", "-journal", "-shm", "-wal", "-mj0", "-mj", "-mj1"};

        for(String filename : toCreate){
            createFile(path + "/" + dbName + filename);
        }
    }
}
