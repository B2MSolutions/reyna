package com.b2msolutions.reyna;

import org.junit.runners.model.InitializationError;
import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;

public class ReynaTestRunner extends RobolectricTestRunner {

    public ReynaTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        String appRoot = "../app/src/main/";
        String manifestPath = appRoot + "AndroidManifest.xml";
        if(!Fs.fileFromPath(manifestPath).exists()){
            String[] names = Fs.currentDirectory().listFileNames();
            throw new ExceptionInInitializerError("AndroidManifest.xml not found at " + manifestPath);
        }

        String resDir = appRoot + "res";
        String assetsDir = appRoot + "assets";
        AndroidManifest manifest = createAppManifest(Fs.fileFromPath(manifestPath),
                Fs.fileFromPath(resDir),
                Fs.fileFromPath(assetsDir));

        manifest.setPackageName("com.my.package.name");
        // Robolectric is already going to look in the  'app' dir ...
        // so no need to add to package name
        return manifest;
    }
}