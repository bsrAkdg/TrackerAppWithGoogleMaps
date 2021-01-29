package com.bsrakdg.trackerappwithgooglemaps.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bsrakdg.trackerappwithgooglemaps.R
import com.bsrakdg.trackerappwithgooglemaps.databinding.FragmentSettingsBinding
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.KEY_NAME
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private fun loadFieldsFromSharedPref() {
        val name = sharedPreferences.getString(KEY_NAME, "")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 80F)
        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())
    }

    private fun applyChangesToSharedPref(): Boolean {
        val nameText = binding.etName.text.toString()
        val weightText = binding.etWeight.text.toString()

        if (nameText.isEmpty() || weightText.isEmpty()) {
            return false;
        }

        sharedPreferences.edit()
            .putString(KEY_NAME, nameText)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .apply()
        val toolbarText = "Let's go $nameText"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle)?.text = toolbarText
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        loadFieldsFromSharedPref()

        binding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if (success) {
                Snackbar.make(view, getString(R.string.saved_changes), Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, getString(R.string.fill_out_fields), Snackbar.LENGTH_LONG).show()
            }
        }
    }
}