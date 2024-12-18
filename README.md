# Fall Detection Android App
Android application used to detect a person's fall, using the accelerometer and gyroscope inside the smartphone and notifying one or more emergency contacts of the fall with a message contains the gps location.

The application provides multiple ways to automatically handle the emergency:
* Send a SMS to one or more contacts stored in the application
* Send Email to one or more contacts
* Call automatically a phone number stored in the application
* Sending a message to a web server, which will take care of forwarding it to a telegram bot that will contact one or more users

The emergency phase is accompanied by an intermittent beep to signal the location of the phone to people nearby.

# Dependencies
[Android Studio](https://developer.android.com/)

# Fall Detection Algorithm
Initially, the algorithm involves the use of the accelerometer sensor in order to compare the Signal Magnitude Vector (SMV) with a fixed threshold value.
To filter out any false positives, in case the threshold is exceeded, the gyroscope sensor is used to verify the phone's position relative to the ground.
The idea is that the phone, in the event that person has felt ill and is lying down, is parallel to the ground, while the device, normally stowed in a pocket, has an angle perpendicular to the ground.

# Result
<img src="https://github.com/AndreaFilippini/fall-detection-android-app/blob/main/images/home.png" width="200" height="480"><img src="https://github.com/AndreaFilippini/fall-detection-android-app/blob/main/images/dashboard.gif" width="200" height="480"><img src="https://github.com/AndreaFilippini/fall-detection-android-app/blob/main/images/notification.png" width="200" height="480"><img src="https://github.com/AndreaFilippini/fall-detection-android-app/blob/main/images/web_server.png" width="305" height="240">
