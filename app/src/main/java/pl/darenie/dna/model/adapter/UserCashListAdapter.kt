package pl.darenie.dna.model.adapter

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.annotation.NonNull
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import de.hdodenhof.circleimageview.CircleImageView
import pl.darenie.R
import pl.darenie.dna.model.json.User
import pl.darenie.dna.model.json.UserCash
import pl.darenie.dna.model.json.UserDetailCash

class UserCashListAdapter(@NonNull context: Context,
                          @LayoutRes private val layoutResource: Int,
                          @NonNull val list : List<UserDetailCash>) :
        ArrayAdapter<UserDetailCash>(context, layoutResource, list){

    private val TAG = "UserCashListAdapter"
    var userCashList: List<UserCash> = ArrayList()

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val userCash = getItem(position)
        if (convertView == null) {
            convertView = inflater.inflate(layoutResource, parent, false)
        }

        val userLabel = convertView!!.findViewById<TextView>(R.id.username)
        userLabel.text = userCash.firstname.plus(" ").plus(userCash.lastname)
        val image = convertView.findViewById<CircleImageView>(R.id.profileImage)

        initUserCashRow(convertView, userCash);

        return convertView
    }

    private fun initUserCashRow(convertView: View, userCash: UserDetailCash) {

        val cashEditText = convertView.findViewById<EditText>(R.id.cashValue)
        cashEditText.setText(userCash.cash.toString())

        cashEditText.onFocusChangeListener = View.OnFocusChangeListener { view: View, hasFocus: Boolean ->
            if(!hasFocus) {
                val index = list.indexOf(userCash)
                userCash.cash = if (cashEditText.text?.toString().isNullOrEmpty()) {
                    0.00
                } else {
                    cashEditText.text.toString().toDouble()
                }
                list.toMutableList().apply {
                    this[index] = userCash
                }
            }
        }

//        cashEditText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            }
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            }
//
//            override fun afterTextChanged(editable: Editable?) {
//                val index = list.indexOf(userCash)
//                userCash.cash = if (cashEditText.text?.toString().isNullOrEmpty()) {
//                    0.00
//                } else {
//                    cashEditText.text.toString().toDouble()
//                }
//                list.toMutableList().apply {
//                    this[index] = userCash
//                }
//            }
//        })


    }


}