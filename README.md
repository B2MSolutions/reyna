reyna : Icelandic for "try"
=====
An android store and forward library for http post requests.
Reyna will store your requests and post them when there is a valid connection.

## Installation
Reyna is a standard android library and can be referenced as a jar in your project.

If you don't want to build your own, you can [download](https://github.com/B2MSolutions/reyna/blob/master/artifacts/reyna.jar?raw=true) the latest version from the [artifacts directory](https://github.com/B2MSolutions/reyna/tree/master/artifacts).

## Android Manifest
You will need to add the following entries into your AndroidManifest.xml in order for reyna to have the correct permissions, services and receivers.

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.package.my"
    android:versionCode="1"
    android:versionName="1.0.0.0">

    <uses-sdk android:minSdkVersion="8" />

    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />

		<application>
        <service android:name="com.b2msolutions.reyna.services.StoreService" />
        <service android:name="com.b2msolutions.reyna.services.ForwardService" />
        <receiver android:name="com.b2msolutions.reyna.receivers.ForwardServiceReceiver">
            <intent-filter>
                <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
            </intent-filter>
        </receiver>
    </application>
 </manifest>
```

## Usage


```java
	import com.b2msolutions.reyna.services.StoreService;

	// Add any headers if required
	Header[] headers = new Header[] {
		new Header("Content-Type", "application/json"),
		new Header("myheader", "header content")
	};

	// Create the message to send
	Message message = new Message(
		new URI("http://server.tosendmessageto.com"),
		"body of post, probably JSON",
		headers);

	// Send the message to Reyna
	StoreService.start(context, message);
```
## Latest version is 2.4

## Contributors
Pair programmed by [Roy Lines](http://roylines.co.uk) and [James Bloomer](https://github.com/jamesbloomer).
