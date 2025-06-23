package com.example.dublintest.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.dublintest.R

class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()

    private lateinit var profileChoices: List<View>
    private lateinit var selectedAvatar: ImageView
    private lateinit var selectedLabel: TextView
    private lateinit var selectedAbilities: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val switcherContainer = view.findViewById<ViewGroup>(R.id.profile_switcher_container)
        profileChoices = listOf(
            switcherContainer.getChildAt(0),
            switcherContainer.getChildAt(1),
            switcherContainer.getChildAt(2)
        )

        selectedAvatar = view.findViewById(R.id.selected_avatar_image)
        selectedLabel = view.findViewById(R.id.selected_profile_label)
        selectedAbilities = view.findViewById(R.id.selected_profile_abilities)

        val profiles = listOf(ProfileType.WOLF, ProfileType.FROG, ProfileType.PELICAN)
        val images = listOf(
            R.drawable.avatar_wolf,
            R.drawable.avatar_frog,
            R.drawable.avatar_pelican
        )

        profiles.forEachIndexed { index, profile ->
            val itemView = profileChoices[index]
            val avatar = itemView.findViewById<ImageView>(R.id.choice_avatar)
            val label = itemView.findViewById<TextView>(R.id.choice_label)

            avatar.setImageResource(images[index])
            label.text = profile.label

            itemView.setOnClickListener {
                profileViewModel.setProfile(profile)
            }
        }

        profileViewModel.selectedProfile.observe(viewLifecycleOwner) { selected ->
            profiles.forEachIndexed { index, profile ->
                val itemView = profileChoices[index]
                val avatar = itemView.findViewById<ImageView>(R.id.choice_avatar)

                val isSelected = profile == selected
                val background = if (isSelected)
                    R.drawable.selected_avatar_border
                else
                    R.drawable.profile_circle_bg

                avatar.background = ContextCompat.getDrawable(requireContext(), background)
            }

            // Update the current profile display
            when (selected) {
                ProfileType.WOLF -> selectedAvatar.setImageResource(R.drawable.avatar_wolf)
                ProfileType.FROG -> selectedAvatar.setImageResource(R.drawable.avatar_frog)
                ProfileType.PELICAN -> selectedAvatar.setImageResource(R.drawable.avatar_pelican)
            }

            selectedLabel.text = selected.label.uppercase()
            selectedAbilities.text = selected.abilities
        }

        return view
    }
}
