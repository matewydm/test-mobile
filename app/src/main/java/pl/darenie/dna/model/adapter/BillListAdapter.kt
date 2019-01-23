package pl.darenie.dna.model.adapter

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.annotation.NonNull
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import pl.darenie.R
import pl.darenie.dna.callback.EditBillCallback
import pl.darenie.dna.model.json.Bill
import pl.darenie.dna.model.json.UserDetailCash

class BillListAdapter(@NonNull context: Context,
                          @LayoutRes private val layoutResource: Int,
                          @NonNull val list : List<Bill>) :
        ArrayAdapter<Bill>(context, layoutResource, list){

    private val TAG = "BillListAdapter"
    var bills: List<Bill> = ArrayList()
    lateinit var callback: EditBillCallback

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val bill = getItem(position)
        if (convertView == null) {
            convertView = inflater.inflate(layoutResource, parent, false)
        }

        val billNameLabel = convertView!!.findViewById<TextView>(R.id.billName)
        billNameLabel.text = bill.name

        return convertView
    }
}