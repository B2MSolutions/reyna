package com.b2msolutions.reyna;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.b2msolutions.reyna.services.FileManager;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositoryTest {

    private Repository repository;

    @Mock Context context;
    @Mock ReynaSqlHelper sqlHelper;
    @Mock SharedPreferences sharedPreferences;
    @Mock Preferences preferences;
    @Mock FileManager fileManager;

    protected Message message1;
    protected Message message2;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        Repository.reynaSqlHelper = this.sqlHelper;
        this.repository = new Repository(context);
        this.repository.fileManager = this.fileManager;
        this.repository.preferences = this.preferences;

        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(db.getPath()).thenReturn("/data/data/packageName/databases/reyna.db");
        when(this.sqlHelper.getReadableDatabase()).thenReturn(db);

        when(this.context.getApplicationContext()).thenReturn(this.context);
        when(this.context.getSharedPreferences(anyString(), anyInt())).thenReturn(this.sharedPreferences);
        when(this.sharedPreferences.getString(eq("DB_FILE"), anyString())).thenReturn("reyna.db");

        this.message1 = createMessage();
        this.message2 = createMessage();
    }

    @Test
    public void whenGettingSqlHelperAndStaticSqlHelperIsNullShouldCreateNewOne() {
        Repository.reynaSqlHelper = null;
        assertNotNull(Repository.getSqlHelper(context));
    }

    @Test
    public void whenCallingInsertWithTwoRepositoriesShouldUseTheSameReynaSqlHelper() {
        Repository repository1 = new Repository(this.context);
        Repository repository2 = new Repository(this.context);

        repository1.insert(this.message1);
        repository2.insert(this.message2);

        verify(this.sqlHelper).insert(this.message1);
        verify(this.sqlHelper).insert(this.message2);
    }

    @Test
    public void whenCallingDeleteWithTwoRepositoriesShouldUseTheSameReynaSqlHelper() {
        Repository repository1 = new Repository(this.context);
        Repository repository2 = new Repository(this.context);

        repository1.delete(this.message1);
        repository2.delete(this.message2);

        verify(this.sqlHelper).delete(this.message1);
        verify(this.sqlHelper).delete(this.message2);
    }

    @Test
    public void whenCallingGetNextWithTwoRepositoriesShouldUseTheSameReynaSqlHelper() throws URISyntaxException {
        when(this.sqlHelper.getNext()).thenReturn(this.message1).thenReturn(this.message2);

        Repository repository1 = new Repository(this.context);
        Repository repository2 = new Repository(this.context);

        Message receivedMessage1 = repository1.getNext();
        Message receivedMessage2 = repository2.getNext();

        verify(this.sqlHelper, times(2)).getNext();
        assertEquals(this.message1, receivedMessage1);
        assertEquals(this.message2, receivedMessage2);
    }

    @Test
    public void WhenCallingMoveDatabaseShouldMoveDbFileToNewLocation() throws Exception{

        this.repository.moveDatabase("/sdcard/mounted/0/reyna.db");

        verify(this.fileManager).copy("/data/data/packageName/databases/reyna.db", "/sdcard/mounted/0/reyna.db");
    }

    @Test
    public void WhenCallingMoveDatabaseShouldSetNewLocationInPreferences() throws Exception{
        this.repository.moveDatabase("/sdcard/mounted/0/reyna.db");

        verify(this.preferences).saveDbFile("/sdcard/mounted/0/reyna.db");
    }

    @Test
    public void WhenCallingMoveDatabaseShouldSetSqlHelperToNull() throws Exception{
        this.repository.moveDatabase("/sdcard/mounted/0/reyna.db");

        assertNull(Repository.reynaSqlHelper);
    }

    @Test
    public void WhenCallingMoveDatabaseShouldDeletePreviousDatabase() throws Exception{
        File file = mock(File.class);
        when(this.fileManager.getFile("/data/data/packageName/databases/reyna.db")).thenReturn(file);

        this.repository.moveDatabase("/sdcard/mounted/0/reyna.db");
        verify(this.fileManager).deleteDatabase("/data/data/packageName/databases/reyna.db");
    }

    @Test
    public void WhenCallingMoveDatabaseAndNewLocationIsSameAsPreviousLocationShouldNotCopy() throws Exception{

        this.repository.moveDatabase("/data/data/packageName/databases/reyna.db");

        verify(this.fileManager, never()).copy(anyString(), anyString());
        verify(this.preferences, never()).saveDbFile(anyString());
    }

    @Test
    public void WhenCreatingShouldInitialiseAllRequiredMembers() {
        Repository repository = new Repository(this.context);

        assertNotNull(repository.fileManager);
        assertNotNull(repository.preferences);
        assertEquals(repository.context, this.context);
    }

    private Message createMessage(){
        try {
            return new Message(new URI("http://uri"), "message", new Header[0]);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
