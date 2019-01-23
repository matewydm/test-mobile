package pl.darenie.dna.model.adapter

import android.content.Context
import android.support.annotation.NonNull
import android.view.LayoutInflater
import de.hdodenhof.circleimageview.CircleImageView
import android.support.annotation.LayoutRes
import android.util.Log
import android.view.View
import pl.darenie.dna.model.json.User
import android.view.ViewGroup
import android.widget.*
import pl.darenie.R
import pl.darenie.dna.callback.InvitationAcceptCallback

class UserListAdapter(@NonNull context: Context,
                      @LayoutRes private val layoutResource: Int,
                      @NonNull private val list : List<User>) :
        ArrayAdapter<User> (context, layoutResource, list){

    private val TAG = "UserListAdapter"
    var selectionList: List<User> = ArrayList()
    lateinit var callback: InvitationAcceptCallback

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val user = getItem(position)
        if (convertView == null) {
            convertView = inflater.inflate(layoutResource, parent, false)
        }

        val userLabel = convertView!!.findViewById<TextView>(R.id.username)
        userLabel.text = user.firstname.plus(" ").plus(user.lastname)
        val image = convertView.findViewById<CircleImageView>(R.id.profileImage)
        when (layoutResource) {
            R.layout.row_friend -> {
                addFriendQuickBillListener(convertView, user)
            }
            R.layout.row_invitation_send -> {
                addInvitationSendListener(convertView, user)
            }
            R.layout.row_invitation_receive -> {
                initInvitationReceiveRow(convertView, user)
            }
            R.layout.row_multiselect -> {
                initMultiSelectRow(convertView, user, userLabel as CheckedTextView)
            }
        }
        return convertView
    }

    private fun initMultiSelectRow(convertView: View, user: User?, checkedTextView: CheckedTextView) {
        checkedTextView.setOnClickListener({
            selectionList = selectionList.plus(user!!)
            checkedTextView.isChecked = !checkedTextView.isChecked
        })
    }

    private fun addFriendQuickBillListener(convertView: View?, user: User) {
        val quickBill = convertView!!.findViewById<ImageView>(R.id.quickBill)
        quickBill.setOnClickListener { Log.d(TAG, user.firstname) }
    }

    private fun addInvitationSendListener(convertView: View?, user: User) {
        val inviteIcon = convertView!!.findViewById<ImageView>(R.id.inviteIcon)
        inviteIcon.setOnClickListener { Log.d(TAG, user.firstname) }
    }

    private fun initInvitationReceiveRow(convertView: View?, user: User) {
        val acceptIcon = convertView!!.findViewById<ImageView>(R.id.acceptIcon)
        val rejectIcon = convertView.findViewById<ImageView>(R.id.rejectIcon)
        acceptIcon.setOnClickListener {
            callback.onItemClick("accept",user.firebaseToken)
        }
        rejectIcon.setOnClickListener {
            callback.onItemClick("reject",user.firebaseToken)
        }
    }

}