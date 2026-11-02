package com.vpn.gen4

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var selectedCountry = "Optimal Server (Gaming)"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val btnSelectCountry = findViewById<Button>(R.id.btnSelectCountry)

        // दुनिया भर के 190 देशों की पूरी ग्लोबल लिस्ट
        val countries = arrayOf(
            "⚡ Optimal Server (Gaming)",
            "🇦🇫 Afghanistan", "🇦🇱 Albania", "🇩🇿 Algeria", "🇦🇩 Andorra", "🇦🇴 Angola",
            "🇦🇬 Antigua and Barbuda", "🇦🇷 Argentina", "🇦🇲 Armenia", "🇦🇺 Australia", "🇦🇹 Austria",
            "🇦🇿 Azerbaijan", "🇧🇸 Bahamas", "🇧🇭 Bahrain", "🇧🇩 Bangladesh", "🇧🇧 Barbados",
            "🇧🇾 Belarus", "🇧🇪 Belgium", "🇧🇿 Belize", "🇧🇯 Benin", "🇧🇹 Bhutan",
            "🇧🇴 Bolivia", "🇧🇦 Bosnia and Herzegovina", "🇧🇼 Botswana", "🇧🇷 Brazil", "🇧🇳 Brunei",
            "🇧🇬 Bulgaria", "🇧🇫 Burkina Faso", "🇧🇮 Burundi", "🇨🇻 Cabor Verde", "🇰🇭 Cambodia",
            "🇨🇲 Cameroon", "🇨🇦 Canada", "🇨🇫 Central African Republic", "🇹🇩 Chad", "🇨🇱 Chile",
            "🇨🇳 China", "🇨🇴 Colombia", "🇰🇲 Comoros", "🇨🇬 Congo", "🇨🇷 Costa Rica",
            "🇭🇷 Croatia", "🇨🇺 Cuba", "🇨🇾 Cyprus", "🇨🇿 Czech Republic", "🇩🇰 Denmark",
            "🇩🇯 Djibouti", "🇩🇲 Dominica", "🇩🇴 Dominican Republic", "🇪🇨 Ecuador", "🇪🇬 Egypt",
            "🇸🇻 El Salvador", "🇬🇶 Equatorial Guinea", "🇪🇷 Eritrea", "🇪🇪 Estonia", "🇪🇹 Ethiopia",
            "🇫🇯 Fiji", "🇫🇮 Finland", "🇫🇷 France", "🇬🇦 Gabon", "🇬🇲 Gambia",
            "🇬🇪 Georgia", "🇩🇪 Germany", "🇬🇭 Ghana", "🇬🇷 Greece", "🇬🇩 Grenada",
            "🇬🇹 Guatemala", "🇬🇳 Guinea", "🇬🇼 Guinea-Bissau", "🇬🇾 Guyana", "🇭🇹 Haiti",
            "🇭🇳 Honduras", "🇭🇰 Hong Kong", "🇭🇺 Hungary", "🇮🇸 Iceland", "🇮🇳 India",
            "🇮🇩 Indonesia", "🇮🇷 Iran", "🇮🇶 Iraq", "🇮🇪 Ireland", "🇮🇱 Israel",
            "🇮🇹 Italy", "🇯🇲 Jamaica", "🇯🇵 Japan", "🇯🇴 Jordan", "🇰🇿 Kazakhstan",
            "🇰🇪 Kenya", "🇰🇮 Kiribati", "🇰🇵 North Korea", "🇰🇷 South Korea", "🇰🇼 Kuwait",
            "🇰🇬 Kyrgyzstan", "🇱🇦 Laos", "🇱🇻 Latvia", "🇱🇧 Lebanon", "🇱🇸 Lesotho",
            "🇱🇷 Liberia", "🇱🇾 Libya", "🇱🇮 Liechtenstein", "🇱🇹 Lithuania", "🇱🇺 Luxembourg",
            "🇲🇴 Macau", "🇲🇬 Madagascar", "🇲🇼 Malawi", "🇲🇾 Malaysia", "🇲🇻 Maldives",
            "🇲🇱 Mali", "🇲🇹 Malta", "🇲🇭 Marshall Islands", "🇲🇷 Mauritania", "🇲🇺 Mauritius",
            "🇲🇽 Mexico", "🇫🇲 Micronesia", "🇲🇩 Moldova", "🇲🇨 Monaco", "🇲🇳 Mongolia",
            "🇲🇪 Montenegro", "🇲🇦 Morocco", "🇲🇿 Mozambique", "🇲🇲 Myanmar", "🇳🇦 Namibia",
            "🇳🇷 Nauru", "🇳🇵 Nepal", "🇳🇱 Netherlands", "🇳🇿 New Zealand", "🇳🇮 Nicaragua",
            "🇳🇪 Niger", "🇳🇬 Nigeria", "🇲🇰 North Macedonia", "🇳🇴 Norway", "🇴🇲 Oman",
            "🇵🇰 Pakistan", "🇵🇼 Palau", "🇵🇸 Palestine", "🇵🇦 Panama", "🇵🇬 Papua New Guinea",
            "🇵🇾 Paraguay", "🇵🇪 Peru", "🇵🇭 Philippines", "🇵🇱 Poland", "🇵🇹 Portugal",
            "🇶🇦 Qatar", "🇷🇴 Romania", "🇷🇺 Russia", "🇷🇼 Rwanda", "🇰🇳 Saint Kitts and Nevis",
            "🇱🇨 Saint Lucia", "🇻🇨 Saint Vincent and the Grenadines", "🇼🇸 Samoa", "🇸🇲 San Marino", "🇸🇹 Sao Tome and Principe",
            "🇸🇦 Saudi Arabia", "🇸🇳 Senegal", "🇸🇷 Serbia", "🇸🇨 Seychelles", "🇸🇱 Sierra Leone",
            "🇸🇬 Singapore", "🇸🇰 Slovakia", "🇸🇮 Slovenia", "🇸🇧 Solomon Islands", "索 Somalia",
            "🇿🇦 South Africa", "🇪🇸 Spain", "🇱🇰 Sri Lanka", "🇸🇩 Sudan", "🇸🇷 Suriname",
            "🇸🇪 Sweden", "🇨🇭 Switzerland", "🇸🇾 Syria", "🇹🇼 Taiwan", "🇹🇯 Tajikistan",
            "🇹🇿 Tanzania", "🇹🇭 Thailand", "🇹🇱 Timor-Leste", "🇹🇬 Togo", "🇹🇴 Tonga",
            "🇹🇹 Trinidad and Tobago", "🇹🇳 Tunisia", "🇹🇷 Turkey", "🇹🇲 Turkmenistan", "🇹🇻 Tuvalu",
            "🇺🇬 Uganda", "🇺🇦 Ukraine", "🇦🇪 United Arab Emirates", "🇬🇧 United Kingdom", "🇺🇸 United States",
            "🇺🇾 Uruguay", "🇺🇿 Uzbekistan", "🇻🇺 Vanuatu", "🇻🇦 Vatican City", "🇻🇪 Venezuela",
            "🇻🇳 Vietnam", "🇾🇪 Yemen", "🇿🇲 Zambia", "🇿🇼 Zimbabwe"
        )

        btnSelectCountry.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select from 190+ Global Countries")
            builder.setItems(countries) { _, which ->
                selectedCountry = countries[which]
                btnSelectCountry.text = selectedCountry
                Toast.makeText(this, "Selected: $selectedCountry", Toast.LENGTH_SHORT).show()
            }
            builder.show()
        }

        btnConnect.setOnClickListener {
            val intent = Intent(this, MyVpnService::class.java).apply {
                putExtra("SELECTED_COUNTRY", selectedCountry)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "Connecting to $selectedCountry...", Toast.LENGTH_SHORT).show()
        }
    }
}
