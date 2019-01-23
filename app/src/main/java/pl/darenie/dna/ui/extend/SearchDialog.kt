package pl.darenie.dna.ui.extend

import android.app.Dialog
import android.content.Context
import android.view.View
import butterknife.ButterKnife
import butterknife.OnClick
import kotlinx.android.synthetic.main.dialog_search.*
import pl.darenie.R
import pl.darenie.dna.model.adapter.UserListAdapter
import pl.darenie.dna.model.json.User
import java.util.*

class SearchDialog(context: Context,
                   val searchList: List<User>) : Dialog(context) {

    var selectionList: List<User> = ArrayList()

    init {
        setContentView(R.layout.dialog_search)
        ButterKnife.bind(this)
        initList()
    }

    private fun initList() {
        val userAdapter = UserListAdapter(context, R.layout.row_multiselect, searchList)
        dialogListView.adapter = userAdapter
    }

    @OnClick(R.id.acceptSearch)
    fun acceptSearchClickAction(view: View) {
        selectionList = (dialogListView.adapter as UserListAdapter).selectionList
        dismiss()
    }

}