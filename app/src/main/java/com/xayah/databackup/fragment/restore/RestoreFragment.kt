package com.xayah.databackup.fragment.restore

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.xayah.databackup.R
import com.xayah.databackup.databinding.FragmentRestoreBinding

class RestoreFragment : Fragment() {

    companion object {
        fun newInstance() = RestoreFragment()
    }

    private lateinit var binding: FragmentRestoreBinding

    private lateinit var viewModel: RestoreViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRestoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(RestoreViewModel::class.java)
        binding.viewModel = viewModel
        setHasOptionsMenu(true)

        Snackbar.make(
            requireContext(),
            binding.constraintLayout,
            getString(R.string.wip),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.topbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        when (item.itemId) {
            R.id.menu_console -> {
                navController.navigate(R.id.action_page_restore_to_page_console)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}