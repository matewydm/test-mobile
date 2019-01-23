package pl.darenie.dna.model.adapter

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.annotation.NonNull
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import pl.darenie.R
import pl.darenie.dna.callback.SettlementCallback
import pl.darenie.dna.model.enums.SettlementStatus
import pl.darenie.dna.model.json.Settlement
import pl.darenie.dna.model.json.SettlementStatusRequest

class SettlementListAdapter (@NonNull context: Context,
                             @LayoutRes private val layoutResource: Int,
                             @NonNull private val list : List<Settlement>) :
        ArrayAdapter<Settlement>(context, layoutResource, list){

    private val TAG = "SettlementListAdapter"
//    var selectionList: List<Settlement> = ArrayList()
    lateinit var  callback : SettlementCallback

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val settlement = getItem(position)
        if (convertView == null) {
            convertView = inflater.inflate(layoutResource, parent, false)
        }

        val chargeLabel = convertView?.findViewById<TextView>(R.id.chargeLabel)
        chargeLabel?.text = settlement?.charge?.toString()

        val billNumberLabel = convertView!!.findViewById<TextView>(R.id.billNumberLabel)
        billNumberLabel.text = "$".plus(settlement.billId)
        when (layoutResource) {
            R.layout.row_settlement_account -> {
                initAccountRow(convertView, settlement)
            }
            R.layout.row_settlement_pay -> {
                initPayRow(convertView, settlement)
            }
            R.layout.row_settlement_due -> {
                initDueRow(convertView, settlement)
            }
        }
        return convertView
    }

    private fun initDueRow(convertView: View, settlement: Settlement?) {
        val userLabel = convertView.findViewById<TextView>(R.id.usernameLabel)
        userLabel.text = settlement?.charger?.firstname.plus(" ").plus(settlement?.charger?.lastname)
        val actionIcon = convertView.findViewById<ImageView>(R.id.actionIcon)
        actionIcon.setOnClickListener({
            callback.call(SettlementStatusRequest(settlement!!.id, SettlementStatus.PAID))
        })
        val notificationIcon = convertView.findViewById<ImageView>(R.id.notificationIcon)
        notificationIcon.setOnClickListener({
            callback.notify(SettlementStatusRequest(settlement!!.id, SettlementStatus.PAID))
        })
    }

    private fun initPayRow(convertView: View, settlement: Settlement?) {
        val userLabel = convertView.findViewById<TextView>(R.id.usernameLabel)
        userLabel.text = settlement?.payer?.firstname.plus(" ").plus(settlement?.payer?.lastname)
        val actionIcon = convertView.findViewById<ImageView>(R.id.actionIcon)
        actionIcon.setOnClickListener({
            callback.call(SettlementStatusRequest(settlement!!.id, SettlementStatus.WAITING_FOR_CONFIRMATION))
        })
    }

    private fun initAccountRow(convertView: View, settlement: Settlement?) {
        val userLabel = convertView.findViewById<TextView>(R.id.usernameLabel)
        userLabel.text = settlement?.charger?.firstname.plus(" ").plus(settlement?.charger?.lastname)
        val actionIcon = convertView.findViewById<ImageView>(R.id.actionIcon)
        actionIcon.setOnClickListener({
            callback.call(SettlementStatusRequest(settlement!!.id, SettlementStatus.PAID))
        })
    }


}