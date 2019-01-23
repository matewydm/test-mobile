package pl.darenie.dna.ui.bill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import butterknife.ButterKnife
import kotlinx.android.synthetic.main.layout_list_bill.*
import pl.darenie.R
import pl.darenie.dna.configuration.App
import pl.darenie.dna.model.adapter.BillListAdapter
import pl.darenie.dna.model.json.Bill
import pl.darenie.dna.ui.base.BaseFragment

class BillListFragment : BaseFragment() {

    private var bills : List<Bill> = ArrayList()
    private var editable : Boolean = false

    companion object {
        fun newInstance(bills: ArrayList<Bill>, editable : Boolean): BillListFragment {
            val fragment = BillListFragment()
            val bundle = Bundle()
            bundle.putSerializable("BILLS", bills)
            bundle.putSerializable("EDITABLE", editable)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity.application as App).restComponent!!.inject(this)
        bills = arguments.getSerializable("BILLS") as ArrayList<Bill>
        editable = arguments.getSerializable("EDITABLE") as Boolean
        val view = inflater?.inflate(R.layout.layout_list_bill, container, false)
        view?.let {
            ButterKnife.bind(this, it)
        }
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        if (editable) {
            billListHeader.text = resources.getString(R.string.ownBills_label)
        } else {
            billListHeader.text = resources.getString(R.string.historyBills_label)
        }
        billListView.adapter = BillListAdapter(activity.applicationContext, R.layout.row_bill, bills)
        billListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            var bill = (billListView.adapter as BillListAdapter).getItem(position)
            replaceFragment(R.id.container,BillFragment.newInstance(bill, editable),null)

        }
        billListView.emptyView = emptyView
    }

}
