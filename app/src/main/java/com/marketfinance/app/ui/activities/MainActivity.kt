package com.marketfinance.app.ui.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.marketfinance.app.R
import com.marketfinance.app.ui.fragments.core.dashboard.DashboardFragment
import com.marketfinance.app.ui.fragments.core.dashboard.UserWatchListData
import com.marketfinance.app.ui.fragments.core.orders.OrdersFragment
import com.marketfinance.app.ui.fragments.core.search.SearchFragment
import com.marketfinance.app.utils.Defaults
import com.marketfinance.app.utils.interfaces.FragmentTransactions
import com.marketfinance.app.utils.security.EncryptedPreference
import com.marketfinance.app.utils.security.Hashing
import com.marketfinance.app.utils.storage.PortfolioData
import me.abhinay.input.CurrencyEditText
import me.abhinay.input.CurrencySymbols
import java.util.*

class MainActivity : AppCompatActivity(), FragmentTransactions, Hashing {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                val fragmentAnimation = FragmentTransactions.FragmentAnimations(
                    R.anim.fragment_fadein,
                    R.anim.fragment_fadeout,
                    R.anim.fragment_fadein,
                    R.anim.fragment_fadeout
                )

                Log.d(
                    TAG,
                    "BottomNavigationItemSelected listener detected input. Switching Fragment to ${item.title}"
                )
                return@OnNavigationItemSelectedListener when (item.itemId) {
                    R.id.menu_bottomNavigationView_dashboard -> {
                        replaceFragment(
                            DashboardFragment(),
                            supportFragmentManager.beginTransaction(),
                            fragmentAnimation,
                            true
                        )
                        true
                    }
                    R.id.menu_bottomNavigationView_search -> {
                        replaceFragment(
                            SearchFragment(),
                            supportFragmentManager.beginTransaction(),
                            fragmentAnimation,
                            true
                        )
                        true
                    }
                    R.id.menu_bottomNavigationView_orders -> {
                        replaceFragment(
                            OrdersFragment(),
                            supportFragmentManager.beginTransaction(),
                            fragmentAnimation,
                            true
                        )
                        true
                    }
                    else -> false
                }
            }
        findViewById<BottomNavigationView>(R.id.main_bottomNavigationView).setOnNavigationItemSelectedListener(
            bottomNavigationItemSelectedListener
        )
    }

    override fun onStart() {
        super.onStart()

        if (intent.getBooleanExtra("promptOnboard", false)) {

            val builder = AlertDialog.Builder(this)
            val dialogLayout = View.inflate(this, R.layout.alert_onbording, null)
            val portfolioNameEditText =
                dialogLayout.findViewById<EditText>(R.id.alertOnboarding_portfolioName_editText)
            val initialBuyingPowerEditText =
                dialogLayout.findViewById<CurrencyEditText>(R.id.alertOnboarding_initialBuyingPower_currencyEditText)
            val eulaCheckBox =
                dialogLayout.findViewById<CheckBox>(R.id.alertOnboarding_eula_checkBox)

            initialBuyingPowerEditText.apply {
                setCurrency(CurrencySymbols.USA)
                setSpacing(true)
            }
            eulaCheckBox.movementMethod = LinkMovementMethod.getInstance()

            val onboardingAlert = builder.apply {
                setView(dialogLayout)
                setCancelable(false)
                setPositiveButton(getString(R.string.Static_Continue)) { dialog, which ->
                    val portfolioName = portfolioNameEditText.text.toString()
                    val portfolioHash = sha256(portfolioName)
                    val initialBuyingPowerCleanDouble = initialBuyingPowerEditText.cleanDoubleValue

                    val portfolioData = PortfolioData(
                        portfolioName,
                        portfolioHash,
                        initialBuyingPowerCleanDouble,
                        initialBuyingPowerCleanDouble,
                        mutableListOf(
                            UserWatchListData(
                                "Default Stocks",
                                Defaults.defaultStocks.toMutableList()
                            )
                        ),
                        mutableListOf()
                    )
                    EncryptedPreference("userData").getPreference(context).edit().apply {
                        putBoolean("completedOnboarding", true)
                        apply()
                    }
                    EncryptedPreference("portfolioData").getPreference(context).apply {
                        val portfolioDataMutableList = gson.fromJson<MutableList<PortfolioData>>(
                            getString("gson", gson.toJson(mutableListOf<PortfolioData>())),
                            object : TypeToken<MutableList<PortfolioData>>() {}.type
                        )
                        portfolioDataMutableList.add(portfolioData)
                        edit().apply {
                            remove("gson")
                            putString("gson", gson.toJson(portfolioDataMutableList))
                            apply()
                            Log.d(TAG, "[DATA] Creating Portfolio: $portfolioData")
                        }
                        replaceFragment(
                            DashboardFragment(),
                            supportFragmentManager.beginTransaction(),
                            null,
                            false
                        )
                    }
                }
            }.create()

            onboardingAlert.show()

            val positiveButton = onboardingAlert.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false

            fun checkRequiredInputs() {
                positiveButton.isEnabled =
                    !portfolioNameEditText.text.isNullOrBlank() && !initialBuyingPowerEditText.text.isNullOrBlank() && eulaCheckBox.isChecked
            }

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    checkRequiredInputs()
                }
            }

            portfolioNameEditText.addTextChangedListener(textWatcher)
            initialBuyingPowerEditText.addTextChangedListener(textWatcher)
            eulaCheckBox.setOnCheckedChangeListener { _, _ -> checkRequiredInputs() }

        } else {
            // Set view on start, otherwise it will be empty
            replaceFragment(
                DashboardFragment(),
                supportFragmentManager.beginTransaction(),
                null,
                false
            )
        }

    }

    override fun onBackPressed() {
        supportFragmentManager.popBackStack()
    }

    companion object {
        private const val TAG = "MainActivity"

        private val gson = Gson()
    }

}