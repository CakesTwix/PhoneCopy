package com.cakestwix.phonecopy

import android.content.pm.PackageManager
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import com.cakestwix.phonecopy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PhoneAdapter
    private var sims = mutableListOf<PhoneModel>()
    private lateinit var manager: TelephonyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PhoneAdapter()

        // Check perms
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 23423)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
            ) {
            requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), 23423)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_NUMBERS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.READ_PHONE_NUMBERS), 23423)
        }


        // Get sims
        manager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        // getActiveSimCards(this)
        // callUssdToGetPhoneNumber("*161#", manager.createForSubscriptionId(1))
        callUssdToGetPhoneNumber("*161#", getActiveSimCards(this))

        binding.swipeUpdate.setOnRefreshListener{
            binding.swipeUpdate.isRefreshing = false
            // Clear list
            sims = mutableListOf()
            adapter.data = sims
            binding.phones.layoutManager = LinearLayoutManager(applicationContext)
            binding.phones.adapter = adapter

            // Update info
            callUssdToGetPhoneNumber("*161#", getActiveSimCards(this))
        }
    }


    @SuppressLint("MissingPermission")
    fun callUssdToGetPhoneNumber(ussdCode: String, listSim: List<SubscriptionInfo>, index: Int = 0) {
        if(listSim.size == 0){
            return
        }
        manager = manager.createForSubscriptionId(listSim[index].subscriptionId)
        // Log.d("CakesTwix-Debug", "Simlist : ${manager.line1Number}")
        if(manager.line1Number.isNotEmpty()){
            sims.add(
                PhoneModel(
                    listSim[index].subscriptionId,
                    listSim[index].displayName.toString(),
                    manager.line1Number
                )
            )

            // Update data
            adapter.data = sims.toSet().toMutableList()
            binding.phones.layoutManager = LinearLayoutManager(applicationContext)
            binding.phones.adapter = adapter

            if(index + 1 != listSim.size){
                callUssdToGetPhoneNumber("*161#", listSim, index + 1)
            }
            return
        }
        manager.sendUssdRequest(ussdCode, object : TelephonyManager.UssdResponseCallback() {
            override fun onReceiveUssdResponse(
                telephonyManager: TelephonyManager,
                request: String,
                response: CharSequence
            ) {
                Toast.makeText(applicationContext, response.toString(), Toast.LENGTH_LONG).show()
                sims.add(
                    PhoneModel(
                        listSim[index].subscriptionId,
                        listSim[index].displayName.toString(),
                        getPhoneNumberFromString(response.toString())
                    )
                )

                // Update data
                adapter.data = sims

                binding.phones.layoutManager = LinearLayoutManager(applicationContext)
                binding.phones.adapter = adapter

                if(index + 1 != listSim.size){
                    callUssdToGetPhoneNumber("*161#", listSim, index + 1)
                }
            }

            override fun onReceiveUssdResponseFailed(
                telephonyManager: TelephonyManager,
                request: String,
                failureCode: Int
            ) {
                when(failureCode) {
                    -1 ->
                        Toast.makeText(applicationContext, "In progress", Toast.LENGTH_LONG).show()
                    else ->
                        Toast.makeText(applicationContext, failureCode.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }, Handler(Looper.getMainLooper()))
    }
    fun getPhoneNumberFromString(input: String): String {
        val phoneNumberPattern = Regex("""\+\d{1,2}\s?(\(\d{1,}\))?\s?\d{1,}[-\s]?\d{1,}[-\s]?\d{1,}\b""")
        val match = phoneNumberPattern.find(input)
        return match?.value.toString()
    }


    fun getActiveSimCards(context: Context): List<SubscriptionInfo> {
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList

        return activeSubscriptionInfoList ?: emptyList()
    }
}