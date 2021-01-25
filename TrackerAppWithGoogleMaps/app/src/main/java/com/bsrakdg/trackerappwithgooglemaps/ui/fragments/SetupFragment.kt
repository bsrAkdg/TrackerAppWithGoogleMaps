package com.bsrakdg.trackerappwithgooglemaps.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bsrakdg.trackerappwithgooglemaps.R
import com.bsrakdg.trackerappwithgooglemaps.databinding.FragmentSetupBinding
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.KEY_NAME
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    private lateinit var binding: FragmentSetupBinding

    @Inject
    lateinit var sharedPref: SharedPreferences

    @Inject
    lateinit var name: String

    @set:Inject
    var isFirstAppOpen = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstAppOpen) {
            navigateRunFragment(savedInstanceState)
        }

        binding = FragmentSetupBinding.bind(view).apply {
            tvContinue.setOnClickListener {
                if (writePersonalDataToSharedPref()) {
                    navigateRunFragment()
                } else {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.enter_all_fields),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun navigateRunFragment(savedInstanceState: Bundle? = null) {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(
                R.id.setupFragment,
                true
            ) // true : clear setup fragment, false : hold setup fragment on back stack
            .build()

        findNavController().navigate(
            R.id.action_setupFragment_to_runFragment,
            savedInstanceState,
            navOptions
        )
    }

    private fun writePersonalDataToSharedPref(): Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply() // async (commit is sync)
        val toolbarText = "Let's go, $name"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle)?.text = toolbarText
        return true
    }
}