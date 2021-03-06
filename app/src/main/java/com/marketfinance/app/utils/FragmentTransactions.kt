package com.marketfinance.app.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.marketfinance.app.R

interface FragmentTransactions {

    data class FragmentAnimations(
        val enterID: Int,
        val exitID: Int,
        val popStackEnterID: Int,
        val popStackExitID: Int
    )

    fun attachJSONtoFragment(fragment: Fragment, json: String): Fragment {
        val bundle = Bundle()
        bundle.putString("json", json)
        fragment.arguments = bundle
        return fragment
    }

    fun replaceFragment(
        fragment: Fragment,
        fragmentTransaction: FragmentTransaction,
        fragmentAnimations: FragmentAnimations?,
        addToBackStack: Boolean
    ) {
        if (fragmentAnimations != null) {
            fragmentTransaction.setCustomAnimations(
                fragmentAnimations.enterID, fragmentAnimations.exitID,
                fragmentAnimations.popStackEnterID, fragmentAnimations.popStackExitID
            )
        }
        fragmentTransaction.replace(R.id.main_fragment, fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()
    }
}