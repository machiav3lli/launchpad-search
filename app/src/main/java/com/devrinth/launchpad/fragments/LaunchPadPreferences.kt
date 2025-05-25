package com.devrinth.launchpad.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.devrinth.launchpad.BuildConfig
import com.devrinth.launchpad.R
import com.devrinth.launchpad.utils.IntentUtils

class LaunchPadPreferences : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey)


        val bmcPreference: Preference? = findPreference("setting_about_bmc")
        val aboutPreference: Preference? = findPreference("setting_about_app_version")
        val webPreference: Preference? = findPreference("setting_about_website")
        val privacyPreference: Preference? = findPreference("setting_about_privacy")
        val mailPreference: Preference? = findPreference("setting_about_mail")

        aboutPreference?.summary = BuildConfig.VERSION_NAME

        bmcPreference?.setOnPreferenceClickListener {
            startActivity(IntentUtils.getLinkIntent(context?.resources!!.getString(R.string.link_kofi)))
            true
        }
        webPreference?.setOnPreferenceClickListener {
            startActivity(IntentUtils.getLinkIntent(context?.resources!!.getString(R.string.link_website)))
            true
        }
        privacyPreference?.setOnPreferenceClickListener {
            startActivity(IntentUtils.getLinkIntent(context?.resources!!.getString(R.string.link_privacy)))
            true
        }
        mailPreference?.setOnPreferenceClickListener {

            val selectorIntent = Intent(Intent.ACTION_SENDTO).apply{
                data = Uri.parse("mailto:")
            }
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(context?.resources?.getString(R.string.link_mail)))
                putExtra(Intent.EXTRA_SUBJECT, context?.resources?.getString(R.string.contact_mail_template_subject))
                putExtra(Intent.EXTRA_TEXT, context?.resources?.getString(R.string.contact_mail_template_body)?.format(BuildConfig.VERSION_NAME))
                selector = selectorIntent
            }
            if (emailIntent.resolveActivity(context?.packageManager!!) != null) {
                startActivity(emailIntent)
            }

            true
        }
    }

}