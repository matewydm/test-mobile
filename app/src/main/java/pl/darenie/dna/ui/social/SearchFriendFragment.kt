package pl.darenie.dna.ui.social

import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import pl.darenie.R
import pl.darenie.dna.configuration.App
import pl.darenie.dna.model.adapter.UserListAdapter
import pl.darenie.dna.model.json.FriendInvitationRequest
import pl.darenie.dna.model.json.User
import pl.darenie.dna.rest.RestProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class SearchFriendFragment : Fragment() {

    private val TAG : String = "SearchFriendActivity"

    @Inject
    lateinit var retrofit : Retrofit

    @BindView(R.id.friendSearchListView)
    lateinit var listView: ListView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity.application as App).restComponent!!.inject(this)
        val view = inflater?.inflate(R.layout.fragment_friend_search, container, false)
        view?.let { ButterKnife.bind(this, it) }
        return view
    }

    @OnClick(R.id.ivBackArrow)
    fun backArrowClick(view: View) {
        activity.onBackPressed()

    }

    @OnClick(R.id.searchLoop)
    fun searchForFriends(view: View) {
        val call : Call<List<User>> = retrofit.create(RestProvider::class.java).getUserForeigners()
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call : Call<List<User>>, response: Response<List<User>>) {
                val friends : List<User> = response.body()!!
                val userAdapter = UserListAdapter(activity.applicationContext, R.layout.row_multiselect, friends)
                listView.adapter = userAdapter
                listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
//                listView.setItemChecked(1, true)
            }
            override fun onFailure(call : Call<List<User>>, t: Throwable) {
                Log.d(TAG, t.message)
                val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

    @OnClick(R.id.searchFriendButton)
    fun searchFriendButtonAction(view: View) {
        var userIds = (listView.adapter as UserListAdapter).selectionList.map { it.firebaseToken }
        val invitationRequest = FriendInvitationRequest(userIds)
        val call : Call<Void> = retrofit.create(RestProvider::class.java).inviteUsers(invitationRequest)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call : Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val toast = Toast.makeText(activity.applicationContext, "Invitation sent successfully", Toast.LENGTH_SHORT)
                    toast.show()
                    activity.onBackPressed()
                } else {
                    val toast = Toast.makeText(activity.applicationContext, "Upss...", Toast.LENGTH_SHORT)
                    toast.show()
                }
            }
            override fun onFailure(call : Call<Void>, t: Throwable) {
                Log.d(TAG, t.message)
                val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

}