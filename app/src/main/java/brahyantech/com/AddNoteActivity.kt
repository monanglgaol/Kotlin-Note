package brahyantech.com

import android.content.ContentValues
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_note.*


class AddNoteActivity : AppCompatActivity() {
    val dbTable = "Notes"
    var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        try {
            val bundle:Bundle = intent.extras
            id = bundle.getInt("ID",0)
            if (id != 0){
                //update note
                //change actionbar title
                supportActionBar!!.title = "Update Note"
                //change button text
                addBtn.text = "Update"
                titleEt.setText(bundle.getString("name"))
                descEt.setText(bundle.getString("des"))
            }
        }catch (ex:Exception){}


       addBtn.setOnClickListener {
           var dbManager = DBManager(this)

           var values = ContentValues()
           values.put("Title", titleEt.text.toString())
           values.put("Description", descEt.text.toString())

           if (id == 0){
               val ID = dbManager.insert(values)
               if (ID>0){
                   Toast.makeText(this,"Note is added", Toast.LENGTH_LONG).show()
                   finish()
               }
               else{
                   Toast.makeText(this,"Error adding note", Toast.LENGTH_LONG).show()
               }

           }
           else{
               var selectionArgs = arrayOf(id.toString())
               val ID = dbManager.update(values, "ID=?", selectionArgs)
               if (ID>0){
                   Toast.makeText(this,"Note is updated", Toast.LENGTH_LONG).show()
                   finish()
               }
               else{
                   Toast.makeText(this,"Error updating note", Toast.LENGTH_LONG).show()
               }

           }

       }
    }




}
