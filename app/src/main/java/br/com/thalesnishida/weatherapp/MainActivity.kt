package br.com.thalesnishida.weatherapp

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    lateinit var API: String
    lateinit var CITY: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        API = getString(R.string.open_weather_api_key)
        CITY = findViewById(R.id.city_name)

        val btnSearch: Button = findViewById(R.id.btnSearch)

        btnSearch.setOnClickListener {
            searchWeather()
        }
        CITY.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchWeather()
                true
            } else {
                false
            }
        }


    }

    private fun searchWeather() {
        val cityName = CITY.text.toString().trim()

        if (cityName.isNotEmpty()) {
            weatherTask().execute(cityName)
        } else {
            Toast.makeText(
                this@MainActivity,
                "Por favor, insira um nome de cidade válido",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    inner class weatherTask() : AsyncTask<String, Void, String>()
    {
        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errortext).visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try {
                val cityName = params[0] // Pegar o nome da cidade passado como parâmetro
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$cityName&units=metric&appid=$API")
                    .readText(Charsets.UTF_8)
            } catch (e: Exception) {
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind  = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                val updateAt:Long = jsonObj.getLong("dt")
                val updateAtText = "Update at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updateAt*10000))
                val temp = main.getString("temp")+"°C"
                val tempMin = "Min temp: "+main.getString("temp_min")+"°C"
                val tempMax = "Max temp: "+main.getString("temp_max")+"°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")
                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")
                val address = jsonObj.getString("name")+", "+sys.getString("country")

                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.update_at).text = updateAtText
                findViewById<TextView>(R.id.status).text = weatherDescription
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
                findViewById<TextView>(R.id.wind).text = windSpeed

                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
            }
            catch (e: Exception) {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errortext).visibility = View.VISIBLE
            }
        }
    }
}