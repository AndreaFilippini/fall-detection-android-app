package com.example.falldetection.ui.dashboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.falldetection.R
import com.example.falldetection.SharedEmailCredentials
import com.example.falldetection.SharedTimerViewModel
import com.example.falldetection.SharedListViewModel
import com.example.falldetection.SharedPhoneViewModel
import com.example.falldetection.SharedEmailViewModel
import com.example.falldetection.SharedIpViewModel

class DashboardFragment : Fragment() {

    private val timerViewModel: SharedTimerViewModel by activityViewModels()
    private val listItemViewModel: SharedListViewModel by activityViewModels()
    private lateinit var listItemAdapter: ListItemAdapter

    private val phoneViewModel: SharedPhoneViewModel by activityViewModels()
    private lateinit var phoneEditText: EditText

    private val emailViewModel: SharedEmailViewModel by activityViewModels()
    private lateinit var emailEditText: EditText

    private val ipViewModel: SharedIpViewModel by activityViewModels()
    private lateinit var setIpButton: Button

    private lateinit var setTimerButton: Button
    private lateinit var setDelayButton: Button
    private lateinit var setSoundButton: Button
    private lateinit var inputTimerText: TextView
    private lateinit var inputDelayText: TextView
    private lateinit var inputSoundText: TextView

    private lateinit var ipNumber : EditText
    private lateinit var ipNumber2 : EditText
    private lateinit var ipNumber3 : EditText
    private lateinit var ipNumber4 : EditText
    private lateinit var totalIp : Array<EditText>

    private lateinit var emailCredentials : SharedEmailCredentials
    private lateinit var setCredentialsButton: Button
    private lateinit var inputUserSender : EditText
    private lateinit var inputPassSender : EditText

    override fun onCreateView(      inflater: LayoutInflater,
                                    container: ViewGroup?,
                                    savedInstanceState: Bundle?
    ): View {

        // init fragment view UI based on the corresponding XML file
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // variable to store EditText references to enter the web server IP
        ipNumber = view.findViewById(R.id.ip_number_1)
        ipNumber2 = view.findViewById(R.id.ip_number_2)
        ipNumber3 = view.findViewById(R.id.ip_number_3)
        ipNumber4 = view.findViewById(R.id.ip_number_4)
        // put all EditText references in an array
        totalIp = arrayOf(ipNumber, ipNumber2, ipNumber3, ipNumber4)

        // for each EditText of web server IP add a listener, to parse and correct the text automatically
        totalIp.forEach { numberEdit ->
            setIpLimit(numberEdit)
        }
        // get the set IP button reference
        setIpButton = view.findViewById(R.id.setIpButton)

        // set the listener of the button to store the IP
        setIpButton.setOnClickListener {
            val ipArray : Array<Int> = arrayOf(0,0,0,0)
            var validIp = true
            // iterate over each EditText to check if the IP is well formed
            totalIp.forEachIndexed { index, number ->
                // convert the current IP part to a string
                val numberString = number.text.toString().toIntOrNull()
                // if the conversion succeed, put the string the IP array that will store the full IP
                if (numberString != null) {
                    ipArray[index] = numberString
                }else{
                    // otherwise set the flag that indicated the current IP is valid to false
                    validIp = false
                }
            }
            // if the entered IP is valid, then stored it into the memory application
            if(validIp){
                ipViewModel.setIp(ipArray)
            }
        }

        // initialize the shared encrypted credentials object
        emailCredentials = SharedEmailCredentials(requireContext())
        // get the references of the EditText with which to enter the email sender credentials
        inputUserSender = view.findViewById(R.id.userEditText)
        inputPassSender = view.findViewById(R.id.passwordEditText)
        // get the reference of the button with which to store the credentials
        setCredentialsButton = view.findViewById(R.id.addCredentalsButton)
        // add a listener to the previous button to call a the storing function of the shared encrypted model
        setCredentialsButton.setOnClickListener {
            // get the entered credentials from the EditText
            val userSender = inputUserSender.text.toString()
            val passSender = inputPassSender.text.toString()
            // if the credentials are not empty, then store them in the application
            if ((userSender != "") && (passSender != "")){
                emailCredentials.storeCredentials(userSender, passSender)
            }else{
                // otherwise notify the user that the credentials are not valid
                Toast.makeText(context, "Enter valid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        // for each timer in the application, get the reference for the corresponding setting button and EditText
        setTimerButton = view.findViewById(R.id.timerButton)
        setDelayButton = view.findViewById(R.id.delayButton)
        setSoundButton = view.findViewById(R.id.soundButton)
        inputTimerText = view.findViewById(R.id.secondsInput)
        inputDelayText = view.findViewById(R.id.delayInput)
        inputSoundText = view.findViewById(R.id.soundInput)

        // fill all of the EditText with the previous stored data
        fillIpInput()
        fillTimerInput()
        fillDelayInput()
        fillSoundInput()
        fillSenderInput()

        // set a listener to the main timer setting button
        setTimerButton.setOnClickListener {
            // convert the seconds value to a string
            val seconds = inputTimerText.text.toString().toIntOrNull()
            // if the entered seconds value is valid
            if (seconds != null) {
                // set the seconds in the shared view model
                timerViewModel.setSeconds(seconds)
            }
        }

        // set a listener to the delay operations setting button
        setDelayButton.setOnClickListener {
            // convert the seconds value to a string
            val seconds = inputDelayText.text.toString().toIntOrNull()
            // if the entered seconds value is valid
            if (seconds != null) {
                // set the seconds in the shared view model
                timerViewModel.setDelay(seconds)
            }
        }

        // set a listener to the delay sound setting button
        setSoundButton.setOnClickListener {
            // convert the seconds value to a string
            val seconds = inputSoundText.text.toString().toIntOrNull()
            // if the entered seconds value is valid and is greater than zero
            if ((seconds != null) && (seconds > 0)){
                // set the seconds in the shared view model
                timerViewModel.setSoundTimer(seconds)
            }else{
                // otherwise notify the user that the entered amount is not valid
                Toast.makeText(requireContext(), "Enter a valid amount for sound delay", Toast.LENGTH_SHORT).show()
            }
        }

        // get the reference of the recyclerView that will contain the operations list
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        // initialize the adapter with slide-up action
        listItemAdapter = ListItemAdapter { listItem ->
            // call the moveItemUp function in the view model
            listItemViewModel.moveItemUp(listItem)
        }
        // set the layout manager and the adapter to update automatically the list view UI
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = listItemAdapter

        // observe the item list in the view model
        listItemViewModel.itemList.observe(viewLifecycleOwner) { list ->
            // using submitList to update the list in the adapter
            listItemAdapter.submitList(list)
        }

        // get the reference of EditText that will be use to enter a phone number
        phoneEditText = view.findViewById(R.id.phoneEditText)
        // get the reference of the recyclerView that will contain the phone numbers list
        val phoneRecyclerView: RecyclerView = view.findViewById(R.id.phoneRecyclerView)
        // set the layout manager to update automatically the list view UI
        phoneRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // initialize the adapter with delete callback
        val adapter = PhoneAdapter { phoneNumber ->
            phoneViewModel.removePhoneNumber(phoneNumber)
        }
        // set the recycler view with the phone adapter
        phoneRecyclerView.adapter = adapter

        // set an observer to update automatically the stored phone list
        phoneViewModel.phoneNumbers.observe(viewLifecycleOwner) { phoneList ->
            adapter.setPhoneNumbers(phoneList ?: emptyList())
        }

        // add a listener to add a phone number in the list that will store all phone numbers on a button click
        view.findViewById<View>(R.id.addButton).setOnClickListener {
            // convert the phone number number and trim it, to make sure that is not empty or a whitespace
            val phoneNumber = phoneEditText.text.toString().trim()
            // if the number is not empty, add it to the corresponding list and clear the EditText
            if (phoneNumber.isNotEmpty()) {
                phoneViewModel.addPhoneNumber(phoneNumber)
                phoneEditText.text.clear()
            } else {
                // otherwise notify the user that the entered phone number is not valid
                Toast.makeText(context, "Enter a valid phone number", Toast.LENGTH_SHORT).show()
            }
        }

        // get the reference of EditText that will be use to enter an email contact
        emailEditText = view.findViewById(R.id.emailEditText)
        // get the reference of the recyclerView that will contain the email contacts list
        val emailRecyclerView: RecyclerView = view.findViewById(R.id.emailRecyclerView)
        // set the layout manager to update automatically the list view UI
        emailRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // initialize the adapter with delete callback
        val emailAdapter = EmailAdapter { emailString ->
            emailViewModel.removeEmail(emailString)
        }
        // set the recycler view with the email adapter
        emailRecyclerView.adapter = emailAdapter

        // set an observer to update automatically the stored email contacts list
        emailViewModel.emailList.observe(viewLifecycleOwner) { emailList ->
            emailAdapter.setEmail(emailList ?: emptyList())
        }

        // add a listener to add a phone number in the list that will store all email contacts on a button click
        view.findViewById<View>(R.id.addEmailButton).setOnClickListener {
            // if the entered email is not empty, add it to the corresponding list and clear the EditText
            val emailString = emailEditText.text.toString().trim()
            if (emailString.isNotEmpty()) {
                emailViewModel.addEmail(emailString)
                emailEditText.text.clear()
            } else {
                // otherwise notify the user that the entered email is not valid
                Toast.makeText(context, "Enter a valid email contact", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // function to manage the a single part of the IP and automatically restore it to a valid number
    private fun setIpLimit(ipNumber : EditText){
        // add a listener to the EditText to execute a code when the text changes
        ipNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // convert to entered number to a numeric value
                val number = s.toString().toIntOrNull()
                // if the number is not valid or is greater than 255
                if((number != null) && (number > 255)){
                    // set the EditText to the max allowable IP number
                    ipNumber.setText(resources.getString(R.string.MAX_IP_VALUE))
                    ipNumber.setSelection(ipNumber.text.length)
                }
            }
        })
    }

    // function to fill the EditText used to enter the countdown timer seconds
    private fun fillTimerInput(){
        // get the stored seconds of the timer
        val currentSeconds = timerViewModel.getSeconds()
        // if the stored seconds value is valid, then put it in the EditText
        if (currentSeconds != null){
            inputTimerText.text = currentSeconds.toString()
        }
    }

    // function to fill the EditText used to enter the delay operations seconds
    private fun fillDelayInput(){
        // get the stored seconds of the timer
        val currentSeconds = timerViewModel.getDelay()
        // if the stored seconds value is valid, then put it in the EditText
        if (currentSeconds != null){
            inputDelayText.text = currentSeconds.toString()
        }
    }

    // function to fill the EditText used to enter the delay sound seconds
    private fun fillSoundInput(){
        // get the stored seconds of the timer
        val currentSeconds = timerViewModel.getSoundTimer()
        // if the stored seconds value is valid, then put it in the EditText
        if (currentSeconds != null){
            inputSoundText.text = currentSeconds.toString()
        }
    }

    // function to fill the all EditText used to enter the web server IP
    private fun fillIpInput(){
        // get the stored IP array
        val currentIp = ipViewModel.getIp()
        // iterate over each part of the IP and set the corresponding EditText
        currentIp.forEachIndexed { index, number ->
            totalIp[index].setText(number.toString())
        }
    }

    // function to fill the the EditText used to enter email sender credentials
    private fun fillSenderInput(){
        val credentials = emailCredentials.getCredentials()
        if(credentials != null){
            val (userSender, passSender) = credentials
            if(userSender != "" && passSender != "") {
                inputUserSender.setText(userSender)
                inputPassSender.setText(passSender)
            }
        }
    }

    /*override fun onDestroyView() {
        super.onDestroyView()
    }*/

    // function to fill all of the EditText with the previous stored data
    override fun onResume() {
        super.onResume()
        fillIpInput()
        fillTimerInput()
        fillDelayInput()
        fillSoundInput()
        fillSenderInput()
    }
}