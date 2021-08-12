package com.example.mpgievents

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultListener
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PaymentActivity : AppCompatActivity(), PaymentResultWithDataListener {

    private lateinit var btnRegister:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        Checkout.preload(applicationContext);

        btnRegister=findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            startPayment()
        }
    }

    private fun startPayment() {
        val activity: Activity = this
        val co = Checkout()
        co.setImage(R.drawable.splash_logo)

        try {
            val options = JSONObject()
            options.put("name","Faizan Khan")
            options.put("description","Demoing Charges")
            //You can omit the image option to fetch the image from dashboard
            //options.put("image","https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("theme.color", "#3399cc")
            options.put("currency","INR")
            options.put("amount","50000")//pass amount in currency subunits

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            val prefill = JSONObject()
            prefill.put("email","enormefaizankhan24@gmail.com")
            prefill.put("contact","7078508094")

            options.put("prefill",prefill)
            co.open(activity,options)
        }catch (e: Exception){
            Toast.makeText(activity,"Error in payment: "+ e.message,Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Toast.makeText(this,"Payment Failed : $p2, Error code : $p1",Toast.LENGTH_LONG).show()
    }

    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        Toast.makeText(this,"Payment Successful : $p0, Error code : $p1",Toast.LENGTH_LONG).show()
    }

}
