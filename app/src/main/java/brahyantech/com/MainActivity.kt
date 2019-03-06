package brahyantech.com

import android.app.AlertDialog
import android.app.SearchManager
import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.row.view.*

class MainActivity : AppCompatActivity() {

    var listNotes = ArrayList<Note>()

    //share preferences
    var mSharedPref:SharedPreferences?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSharedPref = this.getSharedPreferences("My_Data", Context.MODE_PRIVATE)

        //load sorting
        var mSorting = mSharedPref!!.getString("Sort", "ascending")
        when(mSorting){
            "ascending" -> LoadQuery("%")
            "descending" -> loadQueryDescending("%")
        }


    }

    override fun onResume() {
        super.onResume()
        //load sorting
        var mSorting = mSharedPref!!.getString("Sort", "ascending")
        when(mSorting){
            "ascending" -> LoadQuery("%")
            "descending" -> loadQueryDescending("%")
        }
    }

    private fun LoadQuery(title:String){
        var dbManager = DBManager(this)
        val projections = arrayOf("ID", "Title", "Description")
        val selectionArgs = arrayOf(title)
        val cursor = dbManager.Query(projections,"Title Like ?", selectionArgs, "Title")
        listNotes.clear()
        if (cursor.moveToFirst()){

            do {
                val ID = cursor.getInt(cursor.getColumnIndex("ID"))
                val Title = cursor.getString(cursor.getColumnIndex("Title"))
                val Description = cursor.getString(cursor.getColumnIndex("Description"))

                listNotes.add(Note(ID, Title, Description))
            }while (cursor.moveToNext())
        }

        //Adapter
        var myNotesAdapter = MyNotesAdapter(this, listNotes)
        //Set Adapter
        notesLv.adapter = myNotesAdapter


        //get total number of task from Listview
        val total = notesLv.count
        //actionbar
        val mActionBar = supportActionBar
        if (mActionBar != null){
            //set to actionbar as subtitle of action bar
            mActionBar.subtitle = "You have $total note(s) in list"
        }


    }

    private fun loadQueryDescending(title:String){
        var dbManager = DBManager(this)
        val projections = arrayOf("ID", "Title", "Description")
        val selectionArgs = arrayOf(title)
        val cursor = dbManager.Query(projections,"Title Like ?", selectionArgs, "Title")
        listNotes.clear()
        if (cursor.moveToLast()){

            do {
                val ID = cursor.getInt(cursor.getColumnIndex("ID"))
                val Title = cursor.getString(cursor.getColumnIndex("Title"))
                val Description = cursor.getString(cursor.getColumnIndex("Description"))

                listNotes.add(Note(ID, Title, Description))
            }while (cursor.moveToPrevious())
        }

        //Adapter 
        var myNotesAdapter = MyNotesAdapter(this, listNotes)
        //Set Adapter
        notesLv.adapter = myNotesAdapter


        //get total number of task from Listview
        val total = notesLv.count
        //actionbar
        val mActionBar = supportActionBar
        if (mActionBar != null){
            //set to actionbar as subtitle of action bar
            mActionBar.subtitle = "You have $total note(s) in list"
        }


    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        //searchView
        val sv: SearchView = menu!!.findItem(R.id.app_bar_search).actionView as SearchView
        val sm = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        sv.setSearchableInfo(sm.getSearchableInfo(componentName))
        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                LoadQuery("%"+query+"%")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                LoadQuery("%"+newText+"%")
                return false

            }

        });

        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null){
            when(item.itemId){
                R.id.addNote->{
                    startActivity(Intent(this,AddNoteActivity::class.java))
                }
                R.id.action_sort->{
                    //show sorting dialog
                    showSortDialog()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSortDialog() {
        //list of sorting options
        val sortOption = arrayOf("Title(Ascending)", "Title(Descending)")
        val mBuilder = AlertDialog.Builder(this)
        mBuilder.setTitle("Sort by")
        mBuilder.setIcon(R.drawable.ic_action_sort)
        mBuilder.setSingleChoiceItems(sortOption, -1){
            dialogInterface, i ->
            if (i==0){
                Toast.makeText(this,"Title(Ascending)", Toast.LENGTH_LONG).show()
                val editor = mSharedPref!!.edit()
                editor.putString("Sort", "ascending")
                editor.apply()
                LoadQuery("%")


            }
            if (i==1){
                Toast.makeText(this,"Title(Descending)", Toast.LENGTH_LONG).show()
                val editor = mSharedPref!!.edit()
                editor.putString("Sort", "descending")
                editor.apply()
                loadQueryDescending("%")

            }
            dialogInterface.dismiss()
        }
        val mDialog = mBuilder.create()
        mDialog.show()
    }

    inner class MyNotesAdapter:BaseAdapter{
        var listNotesAdapter = ArrayList<Note>()
        var context:Context?=null

        constructor(context: Context, listNotesArray: ArrayList<Note>):super(){
            this.listNotesAdapter = listNotesArray
            this.context = context
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            //inflate layout row.xml
            var myView = layoutInflater.inflate(R.layout.row,null)
            val myNote = listNotesAdapter[position]
            myView.titleTv.text = myNote.nodeName
            myView.descTv.text = myNote.nodeDesc
            
            //delete button click
            myView.deleteBtn.setOnClickListener { 
                var dbManager = DBManager(this.context!!)
                val selectionArgs = arrayOf(myNote.nodeID.toString())
                dbManager.delete("ID=?", selectionArgs)
                LoadQuery("%")
            }
            
            //edit button click
            myView.editBtn.setOnClickListener { 
                GoToUpdate(myNote)
            }

            //copy button click
            myView.copyBtn.setOnClickListener {
                val title = myView.titleTv.text.toString()
                val desc = myView.descTv.text.toString()
                //concatinate
                val s = title +"\n"+ desc
                val cb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cb.text = s
                Toast.makeText(this@MainActivity, "Copied..", Toast.LENGTH_LONG).show()
            }

            //share button click
            myView.shareBtn.setOnClickListener {
                val title = myView.titleTv.text.toString()
                val desc = myView.descTv.text.toString()
                //concatinate
                val s = title +"\n"+ desc

                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, s)
                startActivity(Intent.createChooser(shareIntent,s))

            }

            return myView
        }

        override fun getItem(position: Int): Any {
            return listNotesAdapter[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return listNotesAdapter.size

        }

    }

    private fun GoToUpdate(myNote: Note) {
        var intent = Intent(this, AddNoteActivity::class.java)
        intent.putExtra("ID", myNote.nodeID)
        intent.putExtra("name", myNote.nodeName)
        intent.putExtra("des", myNote.nodeDesc)
        startActivity(intent)



    }
}
