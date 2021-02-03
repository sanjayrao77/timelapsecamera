
# Timelapse Camera GPL, Android App

## Purpose
	Timelapse Camera lets you use an old Android phone or tablet as a networked
timelapse camera. It can use the front, rear, or both cameras with specified
resolutions. It can upload photos to Google Drive or via HTTP(S) POST. It can
also save the photos to local storage. You can have it take a photo every
minute, every hour, every day, etc.

As most Android devices have a camera, battery and wifi hardware, it does not
need any wires to act as a timelapse camera. Depending on your device and
time delay between shots, it
could potentially run for several weeks without charging.

As an option, it can delete old photos from Google Drive. You can select
how many old photos to keep and any more than that will be deleted.

There are many options to reduce power use. Wifi can be toggled
automatically and the screen can be turned off on newer versions of Android.

The scheduling for the camera uses Android's internal "alarm" mechanism so
the device can sleep between shots.

## Screenshots
1|2|3|4

![](http://www.survey7.com/cameraupload/shots/Screenshot_2016-07-28-19-22-17.png) | ![](http://www.survey7.com/cameraupload/shots/Screenshot_2016-07-28-19-22-28.png) | ![](http://www.survey7.com/cameraupload/shots/Screenshot_2016-07-28-19-22-49.png) | ![](http://www.survey7.com/cameraupload/shots/Screenshot_2016-07-28-19-22-53.png)

5|6|7
![](http://www.survey7.com/cameraupload/shots/Screenshot_2016-07-28-19-22-58.png) | ![](http://www.survey7.com/cameraupload/shots/Screenshot_2016-07-28-19-23-04.png) | ![](http://www.survey7.com/cameraupload/shots/Screenshot_2016-07-28-19-23-28.png)


## Uses
- Can be used as a primitive security camera
- Can be pointed at plants to watch them grow
- Can be used to monitor pets while away
- Can be pointed at birds' nests to watch their activity
- Can be used to track sunrises/sunsets
- Can be installed on multiple Android devices to capture several camera angles
- Can be pointed at a porch to check if packages have arrived
- Can be installed in a garage to show if a garage door is open
- Can be pointed at a thermostat/thermometer/display to monitor temperature/humidity/etc

## Features
- Open source, no cost, ad free
- Small app size (2M)
- Works with Google Drive
- Easy configuration

## Installation
1. Install the app
1. Configure your camera settings and saving options
1. Use the "Run once and stop" option to take a single shot
1. Press "Start"
1. Depending on the options you chose, you may need to select an account or
authorize the app in other ways. This should only need to be done this once.
1. If everything was successful, you can go back and select a recurring frequency for the timelapse.

## FAQ

### I'm having trouble uninstalling. What's going on?
It's likely you authorized the app as an administrator. This status is necessary for turning off the screen to save power. Your device may require you to de-authorize the administrator status before allowing you to uninstall. You may find this in Settings -> Security -> Device Administrators.

### I'm having a problem. What should I do?
Please email me with a thorough description of the problem and how to reproduce it. There are a lot of hardware and software configurations out there and I can only test so many of them. Hopefully we can figure out an easy fix for you. My email address should be at the top of this page.

### My device wakes up but only shows the lockscreen.
You'll need to either disable your lockscreen (Settings -> Lock screen) or keep the screen on all the time. Consider the "Keep screen on to prevent sleeping" option.

### My device isn't waking up when expected.
Please check to make sure you don't have a "Power Saving Mode" enabled. Also be sure that the proximity sensor (usually
near the front camera) isn't physically blocked and telling the phone it's in a pocket. If these don't fix it, you might
want to keep the screen on all the time.

### What's with all the permissions needed?
Indeed, it requests a lot. These are mainly for saving power.
WAKE\_LOCK is used to keep the device from sleeping while focusing. CAMERA is
needed to take photos. WRITE\_EXTERNAL\_STORAGE is needed to save files locally
(option). INTERNET is needed to upload via HTTP(S) (option). ACCESS\_WIFI\_STATE
is needed to wait for wifi before saving. CHANGE\_WIFI\_STATE is needed to
enable/disable wifi to save power. ACCESS\_NETWORK\_STATE is needed for some
basic reason. WRITE\_SETTINGS is needed to dim the screen to save power
(option).

If you are worried about this, the source code is available to download
here. You can browse it to see exactly what the app does.

### I'm getting a force-close. Should I report it?
If it's frequent (more than once or repeatable), please let me know! If it's
extremely rare, please mention that when you report it.

### How do I configure my web server for HTTP POST saving?
See the *sample.cgi* perl script included with the source code. The
raw JPEG data are saved via POST, unmodified. The camera name is sent via
QUERY\_STRING.

### What kind of password or encryption is used?
If you use HTTP POST, encryption will not be used and your password will be transmitted in plain-text. This should be fine for a private network.

If you are not using a private network, you
should use HTTPS POST instead. If you use HTTPS, everything will be encrypted reasonably well.


### How do I select HTTPS instead of HTTP?
Your server will need to be configured for HTTPS. After that, you simply enter an upload url that starts with *https://* instead of *http://*.

### How do I enter a username and password for HTTP(S) authentication?
Suppose your username is *frozen* and your password is *yOgUrT*. You would enter *frozen:yOgUrT* in the box titled *Basic Authorization.* Note the colon.

### How can I pass more variables with HTTP(S) POST?
The app already sends a *direction* variable to indicate which camera took the picture (front, back, main). You can send additional variables by modifying the URL. For example, entering *https://yourhost.com/path/sample.cgi?location=garage* would send a *location* value with the photo. The app is smart enough to append the *direction* variable to such a URL.


### Can I use this with automation tools (such as Tasker)?
Yes, you'll want to use the "Start automatically on launch" setting.

### I'd like to compile my own version and put my own branding on it. Is that okay? How do I do that?
Yes, that's perfectly fine however your distributed version
must also be licensed under the GPLv3. You'll need the Android SDK and some
build tools. Please read the *Building* section and email me if you need help.

### I have a great idea to improve the program. What should I do?
By all means, tell me about it! You're welcome to download the source and implement it if you're so inclined.

## Building

Android version 2.3 or higher is required. This includes everything after 2011.

Google Play Services must be installed on the device to use the Google Drive features.


### BUILDING WITHOUT GOOGLE DRIVE LIBRARIES

You'll need to remove the "com.google.android.gms.version" line from
AndroidManifest.xml, as the value comes from Google's "gms-version.xml".

After that, you should be able to build with "$ ant debug". If you then try to
save to Google Drive within the app, the app should crash with a trace to
logcat. You could prevent crashes by forcing "uploaddrive=false" in
Globals.java and/or removing the settings from options.xml.


### BUILDING WITH GOOGLE DRIVE SUPPORT

This requires a Google Play Services library. I've used r28 without a problem.
Google Play Services itself may require a support library. With r28,
android-support-v4 from 23.2.1 seems to work. You'll need to get these from
Google and agree to their licenses. The library changed rather drastically
around r30 and would require something more sophisticated.

Example:
```
	cp ~/opt/googleplayservices/r28-google-play-services_lib/res/values/version.xml res/values/gms-version.xml
	cp ~/opt/googleplayservices/r28-google-play-services_lib/libs/google-play-services.jar libs/
	cp ~/opt/googleplayservices/supportlib-23.2.1/android-support-v4.jar libs/
```


To use the Google Drive API, you'll have to register your build key with
Google's developer console. Your debug key could be in
~/.android/debug.keystore with a password of "android".

After that, you should be able to "$ ant debug" to build an APK.
