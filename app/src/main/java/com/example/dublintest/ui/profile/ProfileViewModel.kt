package com.example.dublintest.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    private val _selectedProfile = MutableLiveData(ProfileType.WOLF) // default is wolf
    val selectedProfile: LiveData<ProfileType> = _selectedProfile

    fun setProfile(profile: ProfileType) {
        _selectedProfile.value = profile
    }
}
