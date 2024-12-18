package com.example.falldetection.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager

import android.widget.Button
import android.widget.ProgressBar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.example.falldetection.R
import com.example.falldetection.ui.notifications.Notification
import com.example.falldetection.SharedNotificationModel
import com.example.falldetection.SharedTimerViewModel
import com.example.falldetection.SharedPhoneViewModel
import com.example.falldetection.SharedIpViewModel
import com.example.falldetection.SharedEmailViewModel
import java.text.DecimalFormat
import androidx.fragment.app.activityViewModels
import com.example.falldetection.SharedListViewModel
import com.example.falldetection.ApiService
import com.example.falldetection.MessageRequest
import com.example.falldetection.MessageResponse
import com.example.falldetection.SharedEmailCredentials

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.Authenticator
import kotlin.math.sqrt

class HomeFragment : Fragment() , SensorEventListener {
    // flag to determine whether there was a fall
    private var fallDetected = false

    // url of the web server to send messages to
    private lateinit var BASE_URL : String

    // init variable to store the GPS service retriever
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // set the properties for the notification display
    private val CHANNEL_ID = "notification_channel"
    private val NOTIFICATION_ID = 1

    // code for permission requests
    private val REQUEST_CODE_LOCATION_PERMISSION = 1001
    private val REQUEST_CODE_POST_NOTIFICATIONS = 1002
    private val REQUEST_CODE_SEND_SMS = 1003
    private val REQUEST_CODE_CALLING = 1004

    // Shared viewModel to share information between fragments
    private val notificationViewModel: SharedNotificationModel by activityViewModels()
    private val timerViewModel: SharedTimerViewModel by activityViewModels()
    private val listItemViewModel: SharedListViewModel by activityViewModels()
    private val phoneNumbersViewModel: SharedPhoneViewModel by activityViewModels()
    private val emailViewModel: SharedEmailViewModel by activityViewModels()
    private val ipViewModel: SharedIpViewModel by activityViewModels()

    // shared viewModel to share encrypted email credentials
    private lateinit var emailCredentials : SharedEmailCredentials

    // format sensor values
    private var dec = DecimalFormat("##.####")

    // define sensor manager and sensor instance variables
    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null

    // variable to store elements on screen of the accelerometer
    private lateinit var accContainer: LinearLayout
    private lateinit var accValueText: TextView
    private lateinit var xValueText: TextView
    private lateinit var yValueText: TextView
    private lateinit var zValueText: TextView

    // variable to store elements on screen of the gyroscope
    private lateinit var gyroContainer: LinearLayout
    private lateinit var gyroValueText: TextView
    private lateinit var xGyroValueText: TextView
    private lateinit var yGyroValueText: TextView
    private lateinit var zGyroValueText: TextView

    // variable to store the element of the banner on which to print the current status
    private lateinit var topBanner: LinearLayout
    private lateinit var topBannerText: TextView

    // variable to store countdown elements
    private lateinit var timerText: TextView
    private lateinit var progressBar: ProgressBar

    // variable to store button references
    private lateinit var emergencyButton: Button
    private lateinit var cancelButton: Button

    // threshold values to detect fall
    private var HORIZONTAL_THRESHOLD = 0.3f
    private var FALL_THRESHOLD = 10.0f

    // default values and variable for fall countdown
    private var DEFAULT_SECONDS = 10
    private var countdownTimer: CountDownTimer? = null
    private var totalTimeInSeconds = DEFAULT_SECONDS
    private var totalTimeInMillis = 0L
    private var isTimerRunning = false

    // default values and variable for operations delay
    private var DEFAULT_DELAY = 5
    private var countdownDelay: CountDownTimer? = null
    private var totalDelayInSeconds = DEFAULT_DELAY
    private var singleDelayInSeconds = DEFAULT_DELAY
    private var totalDelayInMillis = 0L
    private var isDelayRunning = false

    // default values and variable for emergency sound delay
    private var DEFAULT_SOUND_SECONDS = 2
    private var totalSoundInSeconds = DEFAULT_SOUND_SECONDS
    private var totalSoundInMillis = 0L
    //private var isSoundRunning = false

    // functions array to call for the emergency
    private val emergencyOperations: Array<() -> Unit> = arrayOf(::handleSMS, ::handleEmails, ::handleCalling)
    // array to store function indexes to call it based on the order in the dashboard fragment
    private val emergencyOpIndex: Array<Pair<Int, String>?> = arrayOfNulls(emergencyOperations.size)

    // list of phoneNumber and emailContacts to iterate on during the emergency phase
    private var phoneNumbersList : List<String> = emptyList()
    private var emailContactsList: List<String> = emptyList()

    // flag to indicate if the emergency sound is playing
    private var isPlaying: Boolean = false
    // handler to play the emergency sound on repeat
    private val handler = Handler(Looper.getMainLooper())
    // variable that will be store the uri of the notification sound
    private var notificationUri: Uri? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
        ): View {

        // init fragment view UI based on the corresponding XML file
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // initialize the shared encrypted credentials object
        emailCredentials = SharedEmailCredentials(requireContext())

        // get the ip from the shared model to create the server url
        //"http://10.0.2.2:5000" for android emulator
        BASE_URL = ipViewModel.getStringIp()
        // if the ip string is not empty, create the url
        if(BASE_URL != ""){
            BASE_URL = "http://$BASE_URL:5000"
        }

        // initialize SensorManager to manage accelerometer and gyroscope
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // initialize accelerometer sensor
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        // initialize TextViews to print accelerometer values
        xValueText = view.findViewById(R.id.x_value)
        yValueText = view.findViewById(R.id.y_value)
        zValueText = view.findViewById(R.id.z_value)
        accValueText = view.findViewById(R.id.acc_text)
        accContainer = view.findViewById(R.id.accelerometerContainer)

        // if the accelerometer is not available, remove the X,Y,Z labels and notify the user
        if (accelerometerSensor == null) {
            xValueText.visibility = View.GONE
            yValueText.visibility = View.GONE
            zValueText.visibility = View.GONE
            accValueText.text = "Accelerometer not available"
        }

        // initialize gyroscope sensor
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // initialize TextViews to print gyroscope values
        xGyroValueText = view.findViewById(R.id.gyro_x_value)
        yGyroValueText = view.findViewById(R.id.gyro_y_value)
        zGyroValueText = view.findViewById(R.id.gyro_z_value)
        gyroValueText = view.findViewById(R.id.gyro_text)
        gyroContainer = view.findViewById(R.id.gyroscopeContainer)

        // if the gyroscope is not available, remove the X,Y,Z labels and notify the user
        if (gyroscopeSensor == null) {
            xGyroValueText.visibility = View.GONE
            yGyroValueText.visibility = View.GONE
            zGyroValueText.visibility = View.GONE
            gyroValueText.text = "Gyroscope not available"
        }

        // get the reference for top banner elements
        topBanner = view.findViewById(R.id.top_banner)
        topBannerText = view.findViewById(R.id.banner_text)

        // get the references for the elements that are part of the countdown and the buttons
        timerText = view.findViewById(R.id.timerText)
        progressBar = view.findViewById(R.id.circularTimer)
        emergencyButton = view.findViewById(R.id.emergencyButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        // get the current value for the countdown from the view model
        val currentSeconds = timerViewModel.getSeconds()
        // if the value is not null, assign it to the variable that will be used as a countdown
        // otherwise the variable will use a default value specified at the beginning
        if (currentSeconds != null){
            totalTimeInSeconds = currentSeconds
        }
        // calculate the milliseconds from the previous extracted value
        totalTimeInMillis = (totalTimeInSeconds * 1000).toLong()

        // get the current value for operations delay from the view model
        val currentDelay = timerViewModel.getDelay()
        // if the value is not null, assign it to the variable that will be used as delay
        // otherwise the variable will use a default value specified at the beginning
        if (currentDelay != null){
            // variable to store the value of the delay
            singleDelayInSeconds = currentDelay
            // total delay defined as the multiplication between the number of emergency function and the delay value
            totalDelayInSeconds = currentDelay * emergencyOperations.size
        }
        // calculate the milliseconds from the previous extracted value
        totalDelayInMillis = (totalDelayInSeconds * 1000).toLong()

        // get the current value for sound delay from the view model
        val currentSoundTimer = timerViewModel.getSoundTimer()
        // if the value is not null, assign it to the variable that will be used as delay between acoustic signals
        // otherwise the variable will use a default value specified at the beginning
        if (currentSoundTimer != null){
            totalSoundInSeconds = currentSoundTimer
        }
        // calculate the milliseconds from the previous extracted value
        totalSoundInMillis = (totalSoundInSeconds * 1000).toLong()

        // Set up button click listeners for the emergency and cancel operations
        emergencyButton.setOnClickListener {
            // if the countdown timer is activate, start the emergency routine
            if (isTimerRunning) startEmergency()
        }
        cancelButton.setOnClickListener {
            // if the countdown timer is activate or the operations are executing, stop the countdown and back to default state
            if ((isTimerRunning) or (isDelayRunning)) stopCountdown()
        }
        // set the visibility of the UI elements on the screen
        // in the default state, the two buttons are invisible, while information about data sensors are visible
        setButtonVisibility(View.GONE)
        setDataContainersVisibility(View.VISIBLE)

        // set the countdown Timer, defining the operation for each tick and when it will be finished
        countdownTimer = object : CountDownTimer(totalTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // set the flag that indicates the timer is running to true
                isTimerRunning = true
                // calculate the seconds left before the ends of the timer and print them on screen
                val secondsLeft = (millisUntilFinished + 1000) / 1000
                timerText.text = "$secondsLeft s"

                // calculate progress percentage and update the progress bar
                val progress = (millisUntilFinished * 100 / totalTimeInMillis).toInt()
                progressBar.progress = (progress + 1)
            }

            // if the countdown timer ends without any user cancel operations, call the emergency routine
            override fun onFinish() {
                startEmergency()
            }
        }

        // set the delay timer, defining the operation for each tick and when it will be finished
        countdownDelay = object : CountDownTimer(totalDelayInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // set the flag that indicates the timer is running to true
                isDelayRunning = true
                // calculate the seconds left before the ends of the timer
                val secondsLeft = millisUntilFinished / 1000
                // get the operation index based on the subtraction between the number of functions and the rounding of seconds left
                val operationIndex : Int = ((emergencyOperations.size - (secondsLeft / singleDelayInSeconds)) - 1).toInt()
                // get the current seconds left for the current operation
                val delayLeft = ((secondsLeft % singleDelayInSeconds) + 1)
                // get the current index of the function to call based on the order set in the dashboard fragment
                // the index is the first element of a pair of <index, function name>
                val functionIndex = emergencyOpIndex[operationIndex]?.first
                // if the modulo operation between the seconds left and the single delay value slot is zero,
                // it means the slot for the current operation is terminated, so the next function must be called
                if((delayLeft % singleDelayInSeconds).toInt() == 0) {
                    // if the function index is not null, use it to access to the correct function reference and call it
                    if (functionIndex != null) {
                        emergencyOperations[functionIndex]()
                    }
                }
                // update the top banner with the name of the function and the seconds left for this operation
                topBannerText.text = emergencyOpIndex[operationIndex]?.second.toString() + "... [" + delayLeft.toString() + " s]"
            }

            override fun onFinish() {
                // if the timer is set to zero, the operations must be executed immediately
                if(singleDelayInSeconds == 0) {
                    // for each index stored in the array, get the index as the first element element of the pair
                    // and call the corresponding function based on the order set in the dashboard fragment
                    emergencyOpIndex.forEach { item ->
                        if (item != null) {
                            emergencyOperations[item.first]()
                        }
                    }
                }
                // at the end of the execution of the emergency routine, set a default message
                // and restore the countdown progress bar timer on the top
                topBannerText.text = "Emergency sound..."
                resetTimer()
            }
        }

        // use an observer on view model functions to update automatically the corresponding list
        listItemViewModel.itemList.observe(viewLifecycleOwner) { list ->
            list.forEachIndexed { index, item ->
                emergencyOpIndex[index] = Pair(item.index, item.text)
            }
        }

        // use an observer on view model relative to phone numbers to update automatically the corresponding list
        phoneNumbersViewModel.phoneNumbers.observe(viewLifecycleOwner) { phoneNumbers ->
            phoneNumbersList = phoneNumbers
        }

        // use an observer on view model relative to email contacts to update automatically the corresponding list
        emailViewModel.emailList.observe(viewLifecycleOwner) { emailList ->
            emailContactsList = emailList
        }

        // init and create a notification channel to show a notification
        createNotificationChannel()

        // initialize the GPS service provider from the context
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // get the default notification sound URI
        notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        return view
    }

    // general boolean function that check, providing the corresponding permission and the request code,
    // if the user is allowed to use that specific service
    private fun checkPermission(permission : String, requestCode : Int): Boolean {
        // if the user doesn't have the permissions of that service
        if (ActivityCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            // request service permission if not granted
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), requestCode)
            return false
        }
        return true
    }

    // function to set the sound playing flag to true and submit the task to play the sound to the handler
    private fun startSound() {
        isPlaying = true
        handler.post(playSoundTask)
    }

    // function to set the sound playing flag to false and remove the sound task from the handler callbacks list
    private fun stopSound() {
        isPlaying = false
        handler.removeCallbacks(playSoundTask)
    }

    // task to submit to the handler that play the notification sound
    private val playSoundTask = object : Runnable {
        override fun run() {
            // if the task for playing the emergency sound is still active
            if (isPlaying) {
                // call the function to play the ringtone
                playNotificationSound()
                // reschedule the current handler to wait for the amount of seconds specified in the dashboard
                handler.postDelayed(this, totalSoundInMillis)
            }
        }
    }

    // function to play the notification sound, getting the uri of the ringtone and then play it
    private fun playNotificationSound() {
        // check if the uri notification is valid
        if (notificationUri != null) {
            // getting the uri from the context
            val ringtone = RingtoneManager.getRingtone(requireContext(), notificationUri)
            // play the ringtone emergency sound, setting the usage as an alarm to be audible even if the phone is mute
            ringtone.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone.play()
        }
    }

    // function use to display a notification if a fall is detected
    private fun sendNotification() {
        // if the current version of the sdk is older than the Android 8.0 OREO APIs
        // this prevents the execution of a code that will crash
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return

        // Ensure that the app has permission to send a notification
        if(!checkPermission(Manifest.permission.POST_NOTIFICATIONS, REQUEST_CODE_POST_NOTIFICATIONS))
            return

        // Ensure that the app has permission to access to the gps location
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_CODE_LOCATION_PERMISSION)) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // Build the notification
                val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentTitle("FALL DETECTED")
                    .setContentText("Location: " + parseLocation(location))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Send the notification
                with(NotificationManagerCompat.from(requireContext())) {
                    notify(NOTIFICATION_ID, builder.build())
                }
            }
        }
    }

    // function to create a notification channel and register it to the notification manager
    private fun createNotificationChannel() {
        // Create a notification channel for Android OREO and above,
        // to prevent the call of not existing function in older version of Android APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Channel Name"
            val descriptionText = "Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // register the channel with the system with the notification manager
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // function to make the phone vibrate during the fall notification
    private fun vibratePhone() {
        // get the vibration service from the context
        val vibrator = requireContext().getSystemService(Vibrator::class.java)
        // check if the vibrator obj is not null and if the modern Android API that support VibrationEffect
        if ((vibrator?.hasVibrator() == true) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
            // make the phone vibrate
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    // function to start the emergency routine
    private fun startEmergency() {
        countdownTimer?.cancel()
        resetTimer()
        handleFallDetected()
    }

    // function to reset the emergency state, resetting all the elements to go back to the default one
    private fun stopCountdown() {
        countdownTimer?.cancel()
        countdownDelay?.cancel()
        resetTimer()
        resetNoFallElements()
        stopSound()
    }

    // function to init handle the emergency phase
    @SuppressLint("ResourceType")
    private fun handleFallDetected(){
        // make the top banner green to display the current handling functions name
        topBanner.setBackgroundColor(Color.parseColor(resources.getString(R.color.light_green)))
        // remove the emergency button
        removeEmergency()
        // add the notification of a fall in the history
        addHistoryNotification()
        // send the fall detected to the web server
        sendMessageToServer()
        // start the timer to execute a functions every each seconds amount set in the dashboard
        countdownDelay?.start()
        // play an emergency sound every each seconds amount set in the dashboard
        startSound()
    }

    // function to handle the sms sending
    private fun handleSMS() {
        // check the permission for sending an SMS are available
        if(!checkPermission(Manifest.permission.SEND_SMS, REQUEST_CODE_SEND_SMS))
            return

        // Iterate over the phone numbers list and send SMS to each number
        if(phoneNumbersList.isNotEmpty()) {
            // fetch last known location from the gps service
            if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_CODE_LOCATION_PERMISSION)) {
                // add a listener to retrieve the location
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    // create the message to send
                    val message = createMessageFromLoc(location)
                    // send an SMS containing the previous message to each stored phone number
                    for (phoneNumber in phoneNumbersList) {
                        sendSms(phoneNumber, message)
                    }
                }
            }
        }else{
            // if the no number are stored in the application, notify the user
            Toast.makeText(requireContext(), "No saved number to contact with SMS", Toast.LENGTH_SHORT).show()
        }
    }

    // function to send a sms to a phone number
    private fun sendSms(phoneNumber: String, message: String) {
        try {
            // get the sms manager obj
            val smsManager = requireContext().getSystemService(SmsManager::class.java)
            // send the sms to the phone number specified in the first argument
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            // notify the user that the messages was sent successfully
            Toast.makeText(requireContext(), "Message sent to $phoneNumber", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // notify the user if sms sending failed
            Toast.makeText(requireContext(), "Failed to send message to $phoneNumber", Toast.LENGTH_SHORT).show()
        }
    }

    // function to handle the sending of emails
    private fun handleEmails() {
        // if no email contacts are stored in the application, then notify the user and return
        if (emailContactsList.isEmpty()){
            Toast.makeText(requireContext(), "No saved contacts to send an email to", Toast.LENGTH_LONG).show()
            return
        }

        // retrieve the email sender encrypted credentials from the shared view Model
        val credentials = emailCredentials.getCredentials()
        // if the credentials are not available, notify the user and return
        if (credentials == null){
            Toast.makeText(requireContext(), "No sender credentials saved", Toast.LENGTH_LONG).show()
            return
        }

        // check if application has the permission to access to the GPS location
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_CODE_LOCATION_PERMISSION)) {
            // title of the email
            val subject = "Fall Detected by Android Application"

            // add a listener to retrieve the location
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // use a coroutine with the dispatcher for I/O to start the thread that will manage the email sending
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // get the credentials from the credentials obj
                        val (email, password) = credentials
                        // create an email session, specifying the service provider properties with which to send the email
                        val session = createEmailSession(email, password)

                        // define the email properties
                        val emailMessage = MimeMessage(session).apply {
                            // set the email of the sender
                            setFrom(InternetAddress(email))
                            // set the recipients from the list stored in the email shared model
                            setRecipients(
                                Message.RecipientType.TO,
                                emailContactsList.joinToString(",") { InternetAddress(it).toString() }
                            )
                            // set the title of the email and the body
                            setSubject(subject)
                            setText(createMessageFromLoc(location))
                        }
                        // send the email and notify the user
                        Transport.send(emailMessage)
                        showToast("Emails sent successfully")
                    } catch (e: Exception) {
                        // if something failed during the sending email process, notify the user
                        showToast("Failed to send emails")
                    }
                }
            }
        }
    }

    // function to create the mail session
    private fun createEmailSession(email: String, password: String): Session {
        // define the properties of the email service provider
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.ssl.trust", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        // create the authentication instance with the email sender credentials
        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(email, password)
            }
        })
    }

    // function to create and manage a notification with coroutine and the main dispatcher
    private fun showToast(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }


    // function the create a message from a location
    private fun createMessageFromLoc(location : Location?) : String{
        // define the base of the message
        var message = "FALL DETECTED\n"
        // if the location is not null, add the latitude and longitude data to the message
        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude
            message += "Latitude: $latitude\nLongitude: $longitude"
        } else {
            // if the location is null, add "location unknown" to the message
            message += "Location Unknown"
        }
        return message
    }

    // function to parse the location, returning a string that contains location data
    private fun parseLocation(location : Location?) : String{
        // define the location string to return as "unknown"
        var locationString = "Unknown"
        // if the location is not null, overwrite the string with latitude and longitude data
        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude
            locationString = "$latitude, $longitude"
        }
        return locationString
    }

    // function to handle the call of a phone number
    private fun handleCalling() {
        // check if the application has the permission to make a call
        if(!checkPermission(Manifest.permission.CALL_PHONE, REQUEST_CODE_CALLING))
            return

        try {
            // check if the phone number list is not empty
            if(phoneNumbersList.isNotEmpty()){
                // retrieve the first phone number from the list
                val phoneNumber = phoneNumbersList[0]
                // define an intent with call as the action to execute
                val intent = Intent(Intent.ACTION_CALL)
                // set the data of the intent, putting the phone number to call
                intent.data = Uri.parse("tel:$phoneNumber")
                // make the call
                startActivity(intent)
            }else{
                // the no number are available to call, notify the user
                Toast.makeText(requireContext(), "No saved numbers to call", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // if it's not possible to make the call, notify the user
            Toast.makeText(requireContext(), "No app available to make the call", Toast.LENGTH_SHORT).show()
        }
    }

    // function to set the visibility of emergency and cancel button elements
    private fun setButtonVisibility(visibility : Int) {
        emergencyButton.visibility = visibility
        cancelButton.visibility = visibility
    }

    // function to make the emergency button invisible
    private fun removeEmergency() {
        emergencyButton.visibility = View.GONE
    }

    // function to set the visibility of sensors information
    private fun setDataContainersVisibility(visibility : Int){
        accContainer.visibility = visibility
        gyroContainer.visibility = visibility
    }

    // reset the countdown values and progress to the default state
    private fun resetTimer(){
        isTimerRunning = false
        progressBar.progress = 100
        timerText.text = getString(R.string.pause_string)
    }

    // function to add a notification to the history
    private fun addHistoryNotification(){
        // check if application has the permission to access to the GPS location
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_CODE_LOCATION_PERMISSION)) {
            // add a listener to retrieve the location
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->

                // create new notification
                val notification = Notification(
                    id = (notificationViewModel.notifications.value?.size ?: 0) + 1,
                    timestamp = Date().time,
                    location = parseLocation(location)
                )
                // add notification to view model
                notificationViewModel.addNotification(notification)

            }
        }
    }

    // function to reset the state of banner and the buttons, sensor information to the default state
    @SuppressLint("ResourceType")
    private fun resetNoFallElements(){
        topBannerText.text = getString(R.string.banner_string)
        topBanner.setBackgroundColor(Color.parseColor(resources.getString(R.color.light_gray)))
        setButtonVisibility(View.GONE)
        setDataContainersVisibility(View.VISIBLE)
    }

    // function to interact with the sensors and register them to specific handling functions
    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        // associates each sensor with a management function
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> handleAccelerometerData(event.values)
            Sensor.TYPE_GYROSCOPE -> handleGyroscopeData(event.values)
        }
    }

    // function to update accelerometer information on the screen and check for a fall
    private fun handleAccelerometerData(values: FloatArray) {
        // get X,Y,Z values
        val x = values[0]
        val y = values[1]
        val z = values[2]

        // update the X,Y,Z labels with the current data of the accelerometer
        xValueText.text = getString(R.string.x_value,  dec.format(x))
        yValueText.text = getString(R.string.y_value, dec.format(y))
        zValueText.text = getString(R.string.z_value, dec.format(z))

        // if the Z value is greater than the threshold, then a fall is detected
        // if (values[2] > FALL_THRESHOLD) {
        if (sqrt(x * x + y * y + z * z) > FALL_THRESHOLD) {
            // set the falling flag to true
            fallDetected = true
        }
    }

    // function to update gyroscope information on the screen and check for a fall
    @SuppressLint("ResourceType")
    private fun handleGyroscopeData(values: FloatArray) {
        // update the X,Y,Z labels with the current data of the gyroscope
        xGyroValueText.text = getString(R.string.x_value,  dec.format(values[1]))
        yGyroValueText.text = getString(R.string.y_value, dec.format(values[1]))
        zGyroValueText.text = getString(R.string.z_value, dec.format(values[2]))

        // if a fall is detected from the accelerometer
        if (fallDetected) {
            // use the pith and the roll from the gyroscope to check if the phone is parallel to the floor to filter false positive
            val pitch = values[1]
            val roll = values[2]
            if (pitch < HORIZONTAL_THRESHOLD && roll < HORIZONTAL_THRESHOLD) {
                // the emergency and cancel button visible
                setButtonVisibility(View.VISIBLE)
                // hide the information about sensor, since the emergency phase is now active
                setDataContainersVisibility(View.GONE)
                // print that a fall was detected in the top banner and set his background to red
                topBannerText.text = getString(R.string.fall_string)
                topBanner.setBackgroundColor(Color.parseColor(resources.getString(R.color.light_red)))
                // set the countdown progress to the start default value and start the timer
                progressBar.max = 100
                countdownTimer?.start()
                // send a notification that a fall was detected and make the phone vibrate
                sendNotification()
                vibratePhone()
            }
            // reset fall detection to false
            fallDetected = false
        }
    }

    // function to send a message to a web server
    private fun sendMessageToServer(){
        // if the current web server url is empty, notify the user and return
        if(BASE_URL == ""){
            Toast.makeText(activity, "Invalid Server IP", Toast.LENGTH_LONG).show()
            return
        }

        // check if application has the permission to access to the GPS location
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_CODE_LOCATION_PERMISSION)) {
            // add a listener to retrieve the location
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // use the retrofit class to make http-request to web server
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                // create from the retrofit obj the api service obj with which to make the http calls
                val apiService = retrofit.create(ApiService::class.java)

                // create the message request containing the message with the current user location
                val messageRequest = MessageRequest(createMessageFromLoc(location))

                // send the message using the previous api obj
                apiService.sendMessage(messageRequest).enqueue(object : Callback<MessageResponse> {
                    // function of the api obj to manage the responses from the web server
                    override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                        // if the communication with the web server doesn't failed and the message is not null
                        if (response.isSuccessful && response.body() != null) {
                            // notify the user with message received from the web server,
                            // otherwise show a default message to notify the user that no messages were sent from the server
                            Toast.makeText(activity, response.body()?.message ?: "No response message from the server", Toast.LENGTH_LONG).show()
                        } else {
                            // notify the user if wasn't possible to send the message with location information to the web server
                            Toast.makeText(activity, "Failed to send message to the server", Toast.LENGTH_LONG).show()
                        }
                    }
                    // function of the api obj to manager the communication if something went wrong
                    override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                        // notify the user if the communication with the web server failed
                        Toast.makeText(activity, "Server communication error", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // no need to handle accuracy changes
    }

    // when the view is destroyed, delete the timers and stop the emergency sound
    override fun onDestroyView() {
        super.onDestroyView()
        countdownTimer?.cancel()
        countdownDelay?.cancel()
        stopSound()
    }

    // register the sensor listeners when the app is resume
    override fun onResume() {
        super.onResume()
        resetTimer()
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    // remove the sensor listeners to save the phone battery
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this, accelerometerSensor)
        sensorManager.unregisterListener(this, gyroscopeSensor)
    }

}
