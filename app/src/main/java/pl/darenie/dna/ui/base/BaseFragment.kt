package pl.darenie.dna.ui.base

import android.app.Fragment

abstract class BaseFragment : Fragment(){

    fun replaceFragment(layoutRes: Int, fragment: Fragment, addToBackStack: String?) {
        val fragmentManager = fragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(layoutRes, fragment)
        fragmentTransaction.addToBackStack(addToBackStack)
        fragmentTransaction.commit()
    }

    fun addFragment(layoutRes: Int, fragment: Fragment) {
        val fragmentManager = fragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(layoutRes, fragment)
        fragmentTransaction.commit()
    }
}