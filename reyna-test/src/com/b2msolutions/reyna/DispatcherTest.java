package com.b2msolutions.reyna;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.b2msolutions.reyna.Dispatcher.Result;
import com.b2msolutions.reyna.blackout.Time;
import com.b2msolutions.reyna.blackout.TimeRange;
import com.b2msolutions.reyna.http.HttpPost;
import com.b2msolutions.reyna.shadows.ShadowAndroidHttpClient;
import com.b2msolutions.reyna.system.Message;
import com.b2msolutions.reyna.system.Preferences;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowApplication;
import com.xtremelabs.robolectric.shadows.ShadowConnectivityManager;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.zip.GZIPOutputStream;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class DispatcherTest {

    private Context context;
    private ShadowApplication shadowApplication;
    private Intent batteryStatus;
    @Mock NetworkInfo networkInfo;
    @Mock Date now;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        shadowApplication = Robolectric.getShadowApplication();
        context = shadowApplication.getApplicationContext();
        Robolectric.bindShadowClass(ShadowAndroidHttpClient.class);
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        ShadowConnectivityManager shadowConnectivityManager = Robolectric.shadowOf_(connectivityManager);
        shadowConnectivityManager.setActiveNetworkInfo(networkInfo);
        when(networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_WIFI);
        when(networkInfo.isConnectedOrConnecting()).thenReturn(true);
    }

    @Test
    public void sendMessageHappyPathShouldSetExecuteCorrectHttpPostAndReturnOK() throws URISyntaxException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        Message message = RepositoryTest.getMessageWithHeaders();

        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(200);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);

        HttpPost httpPost = mock(HttpPost.class);
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.execute(httpPost)).thenReturn(httpResponse);

        assertEquals(Result.OK, new Dispatcher().sendMessage(message, httpPost, httpClient, this.context));

        this.verifyHttpPost(message, httpPost);

        ArgumentCaptor<StringEntity> stringEntityCaptor = ArgumentCaptor.forClass(StringEntity.class);
        verify(httpPost).setEntity(stringEntityCaptor.capture());
        StringEntity stringEntity = stringEntityCaptor.getValue();
        assertEquals(stringEntity.getContentType().getValue(), "text/plain; charset=UTF-8");
        assertEquals(EntityUtils.toString(stringEntity), "body");
    }

    @Test
    public void sendMessageHappyPathWithChineseCharactersShouldSetExecuteCorrectHttpPostAndReturnOK() throws URISyntaxException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        Message message = RepositoryTest.getMessageWithHeaders("谷歌拼音输入法");

        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(200);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);

        HttpPost httpPost = mock(HttpPost.class);
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.execute(httpPost)).thenReturn(httpResponse);

        assertEquals(Result.OK, new Dispatcher().sendMessage(message, httpPost, httpClient, this.context));

        this.verifyHttpPost(message, httpPost);

        ArgumentCaptor<StringEntity> stringEntityCaptor = ArgumentCaptor.forClass(StringEntity.class);
        verify(httpPost).setEntity(stringEntityCaptor.capture());
        StringEntity stringEntity = stringEntityCaptor.getValue();
        assertEquals(stringEntity.getContentType().getValue(), "text/plain; charset=UTF-8");
        assertEquals(EntityUtils.toString(stringEntity), "谷歌拼音输入法");
    }

    @Test
    public void sendMessageHappyPathWithPortShouldSetPort() throws URISyntaxException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        Message message = new Message(new URI("https://www.google.com:9008/a/b"), "body", null);

        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(200);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);

        HttpPost httpPost = mock(HttpPost.class);
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.execute(httpPost)).thenReturn(httpResponse);

        assertEquals(Result.OK, new Dispatcher().sendMessage(message, httpPost, httpClient, this.context));

        ArgumentCaptor<StringEntity> stringEntityCaptor = ArgumentCaptor.forClass(StringEntity.class);
        verify(httpPost).setEntity(stringEntityCaptor.capture());
        StringEntity stringEntity = stringEntityCaptor.getValue();
        assertEquals(stringEntity.getContentType().getValue(), "text/plain; charset=UTF-8");
        assertEquals(EntityUtils.toString(stringEntity), "body");
    }

    @Test
    public void sendMessageShouldReturnBlackoutWhenInBlackout() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);

        Calendar now = Calendar.getInstance();
        int hourOfDay = now.get(Calendar.HOUR_OF_DAY);
        TimeRange range = new TimeRange(new Time(hourOfDay - 1, 0), new Time(hourOfDay + 1, 0));
        new Preferences(this.context).saveCellularDataBlackout(range);

        assertEquals(Result.BLACKOUT, new Dispatcher().sendMessage(null, null, null, this.context));
    }

    @Test
    public void sendMessageShouldReturnNotConnectedWhenNotConnected() {
        when(this.networkInfo.isConnectedOrConnecting()).thenReturn(false);
        assertEquals(Result.NOTCONNECTED, new Dispatcher().sendMessage(null, null, null, this.context));
    }

    @Test
    public void whenExecuteThrowsReturnTemporaryError() throws URISyntaxException, IOException {
        Message message = RepositoryTest.getMessageWithHeaders();

        HttpPost httpPost = mock(HttpPost.class);
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.execute(httpPost)).thenThrow(new RuntimeException(""));

        assertEquals(Result.TEMPORARY_ERROR, new Dispatcher().sendMessage(message, httpPost, httpClient, this.context));

        this.verifyHttpPost(message, httpPost);
    }

    @Test
    public void getResultShouldReturnExpected() {
        assertEquals(Result.PERMANENT_ERROR, Dispatcher.getResult(100));
        assertEquals(Result.OK, Dispatcher.getResult(200));
        assertEquals(Result.PERMANENT_ERROR, Dispatcher.getResult(300));
        assertEquals(Result.PERMANENT_ERROR, Dispatcher.getResult(400));
        assertEquals(Result.TEMPORARY_ERROR, Dispatcher.getResult(500));
        assertEquals(Result.PERMANENT_ERROR, Dispatcher.getResult(600));
    }

    @Test
    public void sendMessageWithGzipAndContentIsLessThanMinGzipLengthShouldRemoveGzipHeaderAndSendMessageAsString() throws Exception {
        Message message = RepositoryTest.getMessageWithGzipHeaders("body");

        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(200);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);

        HttpPost httpPost = mock(HttpPost.class);
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.execute(httpPost)).thenReturn(httpResponse);
        when(httpClient.execute(httpPost)).thenReturn(httpResponse);

        assertEquals(Result.OK, new Dispatcher().sendMessage(message, httpPost, httpClient, this.context));

        this.verifyHttpPost(message, httpPost);

        ArgumentCaptor<ByteArrayEntity> byteArrayEntityCaptor = ArgumentCaptor.forClass(ByteArrayEntity.class);
        verify(httpPost).setEntity(byteArrayEntityCaptor.capture());
        ByteArrayEntity entity = byteArrayEntityCaptor.getValue();
        assertNull(entity.getContentEncoding());
        assertEquals(EntityUtils.toString(entity, "utf-8"), "body");
    }

    @Test
    public void sendMessageWithGzipHeaderShouldCompressContentAndReturnOK() throws URISyntaxException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        Message message = RepositoryTest.getMessageWithGzipHeaders("this any message body more than 10 bytes length");
        byte[] data = "this any message body more than 10 bytes length".getBytes("utf-8");

        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(200);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);

        HttpPost httpPost = mock(HttpPost.class);
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.execute(httpPost)).thenReturn(httpResponse);

        Result actual = new Dispatcher().sendMessage(message, httpPost, httpClient, this.context);

        assertEquals(Result.OK, actual);

        this.verifyHttpPost(message, httpPost);

        ArgumentCaptor<ByteArrayEntity> entityCaptor = ArgumentCaptor.forClass(ByteArrayEntity.class);
        verify(httpPost).setEntity(entityCaptor.capture());
        AbstractHttpEntity byteArrayEntity = entityCaptor.getValue();
        assertEquals(byteArrayEntity.getContentEncoding().getValue(), "gzip");

        byte[] expected = gzip(data);
        assertArrayEquals(EntityUtils.toByteArray(byteArrayEntity), expected);
    }

    @Test
    public void canSendShouldReturnNOTCONNECTEDIfNoActiveNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        ShadowConnectivityManager shadowConnectivityManager = Robolectric.shadowOf_(connectivityManager);
        shadowConnectivityManager.setActiveNetworkInfo(null);
        assertEquals(Result.NOTCONNECTED, Dispatcher.canSend(this.context));
    }

    @Test
    public void canSendShouldReturnOKIfNonCellularAndConnected() {
        canSendShouldReturnOKIfNonCellularAndConnected(7); // BLUETOOTH
        canSendShouldReturnOKIfNonCellularAndConnected(ConnectivityManager.TYPE_WIFI);
        canSendShouldReturnOKIfNonCellularAndConnected(8); // DUMMY
        canSendShouldReturnOKIfNonCellularAndConnected(9); // ETHERNET
    }

    @Test
    public void canSendShouldReturnOKIfMobileAndHasNoPreferences() {
        canSendShouldReturnOKIfCellularAndHasNoPreferences(ConnectivityManager.TYPE_MOBILE);
        canSendShouldReturnOKIfCellularAndHasNoPreferences(ConnectivityManager.TYPE_MOBILE_DUN);
        canSendShouldReturnOKIfCellularAndHasNoPreferences(ConnectivityManager.TYPE_MOBILE_HIPRI);
        canSendShouldReturnOKIfCellularAndHasNoPreferences(ConnectivityManager.TYPE_MOBILE_MMS);
        canSendShouldReturnOKIfCellularAndHasNoPreferences(ConnectivityManager.TYPE_MOBILE_SUPL);
        canSendShouldReturnOKIfCellularAndHasNoPreferences(ConnectivityManager.TYPE_WIMAX);
    }

    @Test
    public void canSendShouldReturnBlackoutIfMobileAndInBlackout() {
        canSendShouldReturnBlackoutIfCellularAndInBlackout(ConnectivityManager.TYPE_MOBILE);
        canSendShouldReturnBlackoutIfCellularAndInBlackout(ConnectivityManager.TYPE_MOBILE_DUN);
        canSendShouldReturnBlackoutIfCellularAndInBlackout(ConnectivityManager.TYPE_MOBILE_HIPRI);
        canSendShouldReturnBlackoutIfCellularAndInBlackout(ConnectivityManager.TYPE_MOBILE_MMS);
        canSendShouldReturnBlackoutIfCellularAndInBlackout(ConnectivityManager.TYPE_MOBILE_SUPL);
        canSendShouldReturnBlackoutIfCellularAndInBlackout(ConnectivityManager.TYPE_WIMAX);
    }

    @Test
    public void canSendShouldReturnOKIfMobileAndOutsideOfBlackout() {
        canSendShouldReturnOKIfMobileAndOutsideOfBlackout(ConnectivityManager.TYPE_MOBILE);
        canSendShouldReturnOKIfMobileAndOutsideOfBlackout(ConnectivityManager.TYPE_MOBILE_DUN);
        canSendShouldReturnOKIfMobileAndOutsideOfBlackout(ConnectivityManager.TYPE_MOBILE_HIPRI);
        canSendShouldReturnOKIfMobileAndOutsideOfBlackout(ConnectivityManager.TYPE_MOBILE_MMS);
        canSendShouldReturnOKIfMobileAndOutsideOfBlackout(ConnectivityManager.TYPE_MOBILE_SUPL);
        canSendShouldReturnOKIfMobileAndOutsideOfBlackout(ConnectivityManager.TYPE_WIMAX);
    }

    @Test
    public void canSendShouldReturnNoConnectionIfActiveConnectionIsNotConnectedOrConnecting() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        ShadowConnectivityManager shadowConnectivityManager = Robolectric.shadowOf_(connectivityManager);
        when(this.networkInfo.isConnectedOrConnecting()).thenReturn(false);
        shadowConnectivityManager.setActiveNetworkInfo(this.networkInfo);

        assertEquals(Result.NOTCONNECTED, Dispatcher.canSend(this.context));

    }

    private void canSendShouldReturnOKIfMobileAndOutsideOfBlackout(int type) {
        when(this.networkInfo.getType()).thenReturn(type);

        Calendar now = Calendar.getInstance();
        int hourOfDay = now.get(Calendar.HOUR_OF_DAY);

        TimeRange range = new TimeRange(new Time(hourOfDay - 2, 0), new Time(hourOfDay - 1, 0));
        new Preferences(this.context).saveCellularDataBlackout(range);

        assertEquals(Result.OK, Dispatcher.canSend(this.context));
    }

    private void canSendShouldReturnOKIfCellularAndHasNoPreferences(int type) {
        when(this.networkInfo.getType()).thenReturn(type);
        SharedPreferences sp = this.context.getSharedPreferences(Preferences.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
        assertEquals(Result.OK, Dispatcher.canSend(this.context));
    }

    private void canSendShouldReturnBlackoutIfCellularAndInBlackout(int type) {
        when(this.networkInfo.getType()).thenReturn(type);

        Calendar now = Calendar.getInstance();
        int hourOfDay = now.get(Calendar.HOUR_OF_DAY);

        TimeRange range = new TimeRange(new Time(hourOfDay - 1, 0), new Time(hourOfDay + 1, 0));
        new Preferences(this.context).saveCellularDataBlackout(range);

        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));
    }

    private void canSendShouldReturnOKIfNonCellularAndConnected(int type) {
        when(this.networkInfo.getType()).thenReturn(type);

        Calendar now = Calendar.getInstance();
        int hourOfDay = now.get(Calendar.HOUR_OF_DAY);
        TimeRange range = new TimeRange(new Time(hourOfDay - 1, 0), new Time(hourOfDay + 1, 0));
        new Preferences(this.context).saveCellularDataBlackout(range);

        assertEquals(Result.OK, Dispatcher.canSend(this.context));
    }

    private void verifyHttpPost(Message message, HttpPost httpPost) {
        ArgumentCaptor<URI> argument = ArgumentCaptor.forClass(URI.class);
        verify(httpPost).setURI(argument.capture());
        assertEquals(message.getUrl(), argument.getValue().toString());

        verify(httpPost).setHeader(message.getHeaders()[0].getKey(), message.getHeaders()[0].getValue());
        verify(httpPost).setHeader(message.getHeaders()[1].getKey(), message.getHeaders()[1].getValue());
        verify(httpPost, times(2)).setHeader(anyString(), anyString());
    }

    private byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        OutputStream zipper = new GZIPOutputStream(arr);
        zipper.write(data);
        zipper.close();
        return arr.toByteArray();
    }

    @Test
    // device wlan oncharge
    // configuration
    //   wlan true
    public void shouldSendBlackoutTimeScenarioA() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_WIFI);
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, android.os.BatteryManager.BATTERY_PLUGGED_AC);

        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWlanBlackout("02:00-02:01");
        preferences.saveOnChargeBlackout(true);
        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));

        preferences.saveOnChargeBlackout(false);
        assertEquals(Result.OK, Dispatcher.canSend(this.context));
    }

    @Test
    // device wlan oncharge
    // configuration
    //   wlan false
    public void shouldNotSendBlackoutTimeScenarioB() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_WIFI);
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, android.os.BatteryManager.BATTERY_PLUGGED_AC);

        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWlanBlackout("00:00-23:59");
        preferences.saveOnChargeBlackout(true);
        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));

        preferences.saveOnChargeBlackout(false);
        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));
    }

    @Test
    // device wlan offcharge
    // configuration
    //   wlan true
    public void blackoutTimeScenarioC() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_WIFI);
        initBatteryChanged();

        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWlanBlackout("02:00-02:01");
        preferences.saveOffChargeBlackout(true);

        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));

        preferences.saveOffChargeBlackout(false);
        assertEquals(Result.OK, Dispatcher.canSend(this.context));
    }

    @Test
    // device wlan offcharge
    // configuration
    //   wlan false
    public void blackoutTimeScenarioD() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_WIFI);
        initBatteryChanged();

        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWlanBlackout("00:00-23:59");
        preferences.saveOffChargeBlackout(true);

        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));

        preferences.saveOffChargeBlackout(false);
        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));
    }

    private void testBlackoutMobileScenario(int type) {
        when(this.networkInfo.getType()).thenReturn(type);
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, android.os.BatteryManager.BATTERY_PLUGGED_AC);

        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWwanBlackout("02:00-02:01");
        preferences.saveOnChargeBlackout(true);

        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));

        preferences.saveOnChargeBlackout(false);
        assertEquals(Result.OK, Dispatcher.canSend(this.context));
    }

    @Test
    // device wwan oncharge
    // configuration
    //   wwan true
    public void blackoutTimeScenarioE() {
        testBlackoutMobileScenario(ConnectivityManager.TYPE_MOBILE);
        testBlackoutMobileScenario(ConnectivityManager.TYPE_MOBILE_DUN);
        testBlackoutMobileScenario(ConnectivityManager.TYPE_MOBILE_HIPRI);
        testBlackoutMobileScenario(ConnectivityManager.TYPE_MOBILE_MMS);
        testBlackoutMobileScenario(ConnectivityManager.TYPE_MOBILE_SUPL);
        testBlackoutMobileScenario(ConnectivityManager.TYPE_WIMAX);
    }

    @Test
    // device wwan oncharge
    // configuration
    //   wwan false
    public void blackoutTimeScenarioF() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, android.os.BatteryManager.BATTERY_PLUGGED_AC);

        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWwanBlackout("00:00-23:59");
        preferences.saveOnChargeBlackout(true);

        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));

        preferences.saveOnChargeBlackout(false);
        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));
    }

    @Test
    // device wwan offcharge
    // configuration
    //   wwan true
    public void blackoutTimeScenarioG() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        initBatteryChanged();

        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWwanBlackout("02:00-02:01");
        preferences.saveOffChargeBlackout(true);

        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));

        preferences.saveOffChargeBlackout(false);
        assertEquals(Result.OK, Dispatcher.canSend(this.context));
    }

    @Test
    // device wwan offcharge
    // configuration
    //   wwan false
    public void blackoutTimeScenarioH() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        initBatteryChanged();

        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveWwanBlackout("00:00-23:59");
        preferences.saveOffChargeBlackout(true);

        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));

        preferences.saveOffChargeBlackout(false);
        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));
    }

    @SuppressLint("NewApi")
    @Test
    // device roaming oncharge
    // configuration
    //   roaming false
    public void blackoutTimeScenarioI() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        when(this.networkInfo.isRoaming()).thenReturn(true);
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, android.os.BatteryManager.BATTERY_PLUGGED_AC);

        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveOnChargeBlackout(true);
        preferences.saveWwanRoamingBlackout(false);

        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));

        preferences.saveOnChargeBlackout(false);
        assertEquals(Result.OK, Dispatcher.canSend(this.context));
    }

    @SuppressLint("NewApi")
    @Test
    // device roaming oncharge
    // configuration
    //   roaming true
    public void blackoutTimeScenarioJ() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        when(this.networkInfo.isRoaming()).thenReturn(true);
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, android.os.BatteryManager.BATTERY_PLUGGED_AC);

        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveOnChargeBlackout(true);
        preferences.saveWwanRoamingBlackout(true);

        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));

        preferences.saveOnChargeBlackout(false);
        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));
    }

    @SuppressLint("NewApi")
    @Test
    // device roaming offcharge
    // configuration
    //   roaming true
    public void blackoutTimeScenarioK() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        when(this.networkInfo.isRoaming()).thenReturn(true);
        initBatteryChanged();

        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveOffChargeBlackout(true);
        preferences.saveWwanRoamingBlackout(true);

        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));

        preferences.saveOffChargeBlackout(false);
        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));
    }

    @SuppressLint("NewApi")
    @Test
    // device roaming offcharge
    // configuration
    //   roaming false
    public void blackoutTimeScenarioL() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        when(this.networkInfo.isRoaming()).thenReturn(true);
        initBatteryChanged();

        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveOffChargeBlackout(true);
        preferences.saveWwanRoamingBlackout(false);

        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));

        preferences.saveOffChargeBlackout(false);
        assertEquals(Result.OK, Dispatcher.canSend(this.context));
    }

    @SuppressLint("NewApi")
    @Test
    // device wwan and wlan oncharge
    // configuration
    //   wwan true
    public void blackoutTimeScenarioM() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        when(this.networkInfo.isRoaming()).thenReturn(true);
        initBatteryChanged();

        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveOffChargeBlackout(true);
        preferences.saveWwanRoamingBlackout(false);

        assertEquals(Result.BLACKOUT, Dispatcher.canSend(this.context));

        preferences.saveOffChargeBlackout(false);
        assertEquals(Result.OK, Dispatcher.canSend(this.context));
    }

    @Test
    public void whenCallingIsChargingAndCouldNotGetBatteryStatusShouldReturnFalse() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        shadowApplication.sendStickyBroadcast(intent);
        assertFalse(Dispatcher.isBatteryCharging(context));
    }

    private void initBatteryChanged() {
        batteryStatus = new Intent();
        batteryStatus.setAction(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_LEVEL, 3);
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_SCALE, 7);
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_STATUS, 4);
        shadowApplication.sendStickyBroadcast(batteryStatus);
    }

    @Test
    public void whenCallingIsChargingShouldReturnExpected() {
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, android.os.BatteryManager.BATTERY_PLUGGED_AC);
        assertTrue(Dispatcher.isBatteryCharging(context));
    }

    @Test
    public void whenCallingIsChargingUsbShouldReturnExpected() {
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, android.os.BatteryManager.BATTERY_PLUGGED_USB);
        assertTrue(Dispatcher.isBatteryCharging(context));
    }

    @Test
    public void whenCallingIsChargingWirelessShouldReturnExpected() {
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, 4);
        assertTrue(Dispatcher.isBatteryCharging(context));
    }

    @Test
    public void whenCallingIsChargingUnknownShouldReturnExpected() {
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, 3);
        assertTrue(Dispatcher.isBatteryCharging(context));
    }

    @Test
    public void whenCallingGetBatteryChargingOnAndNotChargingShouldReturnExpected() {
        initBatteryChanged();
        batteryStatus.putExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1);
        assertFalse(Dispatcher.isBatteryCharging(context));
    }

    @Test
    public void whenOldAndNewConfigurationPresentPreferNewConfiguration() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        Calendar now = new GregorianCalendar();
        Time oldTo = new Time(now.get(Calendar.HOUR_OF_DAY) + 1, now.get(Calendar.MINUTE));
        preferences.saveCellularDataBlackout(new TimeRange(new Time(0,0), oldTo));

        String newTo = (String.format("%02d", now.get(Calendar.HOUR_OF_DAY) - 1)) + ":" + String.format("%02d", now.get(Calendar.MINUTE));
        preferences.saveWwanBlackout("00:00-" + newTo);

        assertEquals(Result.OK, Dispatcher.canSend(context));
    }

    @Test
    public void whenOldConfigurationPresentAndNewNotPresentDontUseNewConfDefaultValue() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        Calendar now = new GregorianCalendar();
        Time oldTo = new Time(now.get(Calendar.HOUR_OF_DAY) + 1, now.get(Calendar.MINUTE));
        preferences.saveCellularDataBlackout(new TimeRange(new Time(0,0), oldTo));

        assertEquals(Result.BLACKOUT, Dispatcher.canSend(context));
    }

    @Test
    public void whenOldConfigurationIsFromZeroAndToZeroAllowSending() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveCellularDataBlackout(new TimeRange(new Time(0, 0), new Time(0, 0)));

        assertEquals(Result.OK, Dispatcher.canSend(context));
    }

    @Test
    public void whenCallingCanSendAndCurrentTimeIsBetweenNonRecurringWwanBlackoutStartTimeAndEndTimeShouldBlackout() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveNonRecurringWwanBlackoutStartTime(42L);
        preferences.saveNonRecurringWwanBlackoutEndTime(84L);

        GregorianCalendar now = mock(GregorianCalendar.class);
        when(now.getTimeInMillis()).thenReturn(63L);

        assertEquals(Result.BLACKOUT, Dispatcher.canSend(context, now));
    }

    @Test
    public void whenCallingCanSendAndCurrentTimeIsBeforeNonRecurringWwanBlackoutStartTimeAndEndTimeShouldBeOk() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveNonRecurringWwanBlackoutStartTime(70L);
        preferences.saveNonRecurringWwanBlackoutEndTime(84L);

        GregorianCalendar now = mock(GregorianCalendar.class);
        when(now.getTimeInMillis()).thenReturn(63L);

        assertEquals(Result.OK, Dispatcher.canSend(context, now));
    }

    @Test
    public void whenCallingCanSendAndCurrentTimeIsAfterNonRecurringWwanBlackoutStartTimeAndEndTimeShouldBeOk() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.saveNonRecurringWwanBlackoutStartTime(70L);
        preferences.saveNonRecurringWwanBlackoutEndTime(84L);

        GregorianCalendar now = mock(GregorianCalendar.class);
        when(now.getTimeInMillis()).thenReturn(90L);

        assertEquals(Result.OK, Dispatcher.canSend(context, now));
    }

    @Test
    public void whenCallingCanSendAndNonRecurringWwanBlackoutStartTimeOrEndTimeIsNotSetShouldBeOk() {
        when(this.networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        Preferences preferences = new Preferences(Robolectric.getShadowApplication().getApplicationContext());
        preferences.resetNonRecurringWwanBlackout();

        GregorianCalendar now = mock(GregorianCalendar.class);
        when(now.getTimeInMillis()).thenReturn(90L);

        assertEquals(Result.OK, Dispatcher.canSend(context, now));
    }
}
