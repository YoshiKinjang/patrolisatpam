package com.example.patrolisatpam

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.patrolisatpam.`object`.Round
import kotlinx.android.synthetic.main.activity_peta.*
import kotlinx.android.synthetic.main.layout_gridview_main.view.*


//---------------------------bagian/class ini tidak terpakai------------------------
class PetaActivity : AppCompatActivity() {
    var adapter: PetaActivity.MenuAdapter? = null
    var menusList = ArrayList<Round>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peta)

        val angkaRound = intent.getStringExtra("round")
        val tvPos = findViewById(R.id.tvRound) as TextView;

        tvPos.setText("Round "+angkaRound);

        for (x in 1..18){
//            menusList.add(Round(x, 0))
        }
        adapter = PetaActivity.MenuAdapter(this, menusList)
        Mapgridview.adapter = adapter
    }

    class MenuAdapter : BaseAdapter {
        var menusList = ArrayList<Round>()
        var context: Context? = null
        constructor(context: Context, menusList: ArrayList<Round>) : super() {
            this.context = context
            this.menusList = menusList
        }
        override fun getCount(): Int {
            return menusList.size
        }
        override fun getItem(position: Int): Any {
            return menusList[position]
        }
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val menu = this.menusList[position]
            var inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var menuView = inflator.inflate(R.layout.layout_gridview_main, parent, false)
            menuView.tvAngka.text = menu.angka!!.toString()

            menuView.cvGrid.setOnClickListener {
                val inten = Intent(context, PosActivity::class.java)
                val angka = menu.angka
                inten.putExtra("pos", angka)
                buatSP().saveSPString(buatSP().POS_BERAPA, angka!!.toString());
                context!!.startActivity(inten);
            }

            return menuView
        }
        fun buatSP(): SharedPreference{
            val spHelp = SharedPreference(context!!)
            return spHelp;
        }
    }
}
