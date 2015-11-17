reyna : Icelandic for "try"
=====
An android store and forward library for http post requests.
Reyna will store your requests and post them when there is a valid connection.

## Installation
Reyna is a standard android library and can be referenced as a jar in your project.

If you don't want to build your own, you can [download](https://github.com/B2MSolutions/reyna/blob/master/artifacts/reyna-2.10.0.47.jar?raw=true) the latest version from the [artifacts directory](https://github.com/B2MSolutions/reyna/tree/master/artifacts).

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
		new Header("myheader", "header content"),

		// gzip content when posting
		new Header("Content-Encoding", "gzip")
	};


	// Create the message to send
	Message message = new Message(
		new URI("http://server.tosendmessageto.com"),
		"body of post, probably JSON",
		headers);

	// Send the message to Reyna
	StoreService.start(context, message);


	// set Reyna logging level, same constant values as android.util.log (ERROR, WARN, INFO, DEBUG, VERBOSE)
	StoreService.setLogLevel(level);
```

## Building jar

From the Reyna root folder

 * run `./hooks/pre-commit
 * inside `reyna/bin` you will find classes.jar, rename it according to semver

## Contributors
Pair programmed by [Roy Lines](http://roylines.co.uk) and [James Bloomer](https://github.com/jamesbloomer).
[Ivan Bokii](https://github.com/ivanbokii) and [Youhana Hana](https://github.com/youhana-hana).
