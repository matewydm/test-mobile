package pl.darenie.dna.ui.social

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx
import kotlinx.android.synthetic.main.fragment_social.*
import pl.darenie.R
import pl.darenie.dna.callback.InvitationAcceptCallback
import pl.darenie.dna.configuration.App
import pl.darenie.dna.model.adapter.UserListAdapter
import pl.darenie.dna.model.json.Friend
import pl.darenie.dna.model.json.User
import pl.darenie.dna.model.json.UserIdDTO
import pl.darenie.dna.rest.RestProvider
import pl.darenie.dna.ui.base.BaseFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class SocialFragment : BaseFragment() {

    val TAG = "SocialFragment"
    lateinit var fragmentView: View
    @Inject
    lateinit var retrofit: Retrofit
    lateinit var topView : BottomNavigationViewEx
    @BindView(R.id.listView)
    lateinit var listView: ListView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity.application as App).restComponent!!.inject(this)
        fragmentView = inflater?.inflate(R.layout.fragment_social, container, false)!!
        fragmentView.let { ButterKnife.bind(this, it) }
        setupTopSocialNavigationView()
        return fragmentView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateFriendListView()
    }

    private fun setupTopSocialNavigationView() {
         topView = fragmentView.findViewById(R.id.topSocialNavigationView)
        topView.enableAnimation(false)
        topView.enableShiftingMode(false)
        topView.enableItemShiftingMode(false)
        topView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId){
                R.id.top_social_friends -> {
                    updateFriendListView()
                    item.isChecked = true
                }

                R.id.top_social_invitiation ->{
                    updateInvitationListView()
                    item.isChecked = true
                }

                else ->
                    Log.d(TAG,"activity without TopSocialNavigationView")
            }
            false
        }
    }

    private fun updateFriendListView() {
        Log.d(TAG, "Friends")
        val call : Call<List<User>> = retrofit.create(RestProvider::class.java).getUserFriends()
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call : Call<List<User>>, response: Response<List<User>>) =
                    if (response.isSuccessful) {
                        val friends: List<User> = response.body()!!
                        val userAdapter = UserListAdapter(activity.applicationContext, R.layout.row_friend, friends)
                        listView.adapter = userAdapter
                        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, positon, id ->
                            showProfileView(listView.adapter.getItem(positon) as User?)
                        }
                    } else {
                        Log.d(TAG, response.code().toString())
                        val toast = Toast.makeText(activity.applicationContext, "Upss...", Toast.LENGTH_SHORT)
                        toast.show()
                    }
            override fun onFailure(call : Call<List<User>>, t: Throwable) {
                val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

    private fun showProfileView(user: User?) {
        val call : Call<Friend> = retrofit.create(RestProvider::class.java).getFriend(user!!.firebaseToken)
        call.enqueue(object : Callback<Friend> {
            override fun onResponse(call : Call<Friend>, response: Response<Friend>) =
                    if (response.isSuccessful) {
                        val friend: Friend = response.body()!!
                        replaceFragment(R.id.container, FriendProfileFragment.newInstance(friend),null)
                    } else {
                        Log.d(TAG, response.code().toString())
                        val toast = Toast.makeText(activity.applicationContext, "Upss...", Toast.LENGTH_SHORT)
                        toast.show()
                    }
            override fun onFailure(call : Call<Friend>, t: Throwable) {
                val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

    private fun updateInvitationListView() {
        Log.d(TAG, "Invitation")
        val call : Call<List<User>> = retrofit.create(RestProvider::class.java).getInvitations()
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call : Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val friends : List<User> = response.body()!!
                    val userAdapter = UserListAdapter(activity.applicationContext, R.layout.row_invitation_receive, friends)
                    userAdapter.callback = (object : InvitationAcceptCallback {
                        override fun onItemClick(s: String, userId: String) {
                            sendFriendInvitationAnswer(s,userId)
                        }
                    })
                    listView.adapter = userAdapter
                    listView.emptyView = emptyView
                } else {
                    Log.d(TAG, response.code().toString())
                    val toast = Toast.makeText(activity.applicationContext, "Upss...", Toast.LENGTH_SHORT)
                    toast.show()
                }
            }
            override fun onFailure(call : Call<List<User>>, t: Throwable) {
                val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

    @OnClick(R.id.searchFriend)
    fun searchFriendClick(view: View) {
        replaceFragment(R.id.container,SearchFriendFragment(),null)
    }

    fun sendFriendInvitationAnswer(answer: String, userId: String) {
        val call: Call<Void>
        if (answer.equals("accept")) {
            call = retrofit.create(RestProvider::class.java).acceptInvitation(UserIdDTO(userId))
        } else {
            call = retrofit.create(RestProvider::class.java).rejectInvitation(UserIdDTO(userId))
        }
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call : Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(TAG, response.code().toString())
                    val toast = Toast.makeText(activity.applicationContext, "Invitation request sent successfully", Toast.LENGTH_SHORT)
                    toast.show()
                    updateFriendListView()
                    topView.selectedItemId = 0
                } else {
                    Log.d(TAG, response.code().toString())
                    val toast = Toast.makeText(activity.applicationContext, "Upss...", Toast.LENGTH_SHORT)
                    toast.show()
                }
            }
            override fun onFailure(call : Call<Void>, t: Throwable) {
                val toast = Toast.makeText(activity.applicationContext, t.message, Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

}