package com.example.oderapp.Activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.oderapp.Adapter.CategoryFoodAdapter
import com.example.oderapp.Adapter.FoodManagementAdapter
import com.example.oderapp.Model.LoaiMonAn
import com.example.oderapp.Model.MonAn
import com.example.oderapp.R
import com.example.oderapp.databinding.ActivityFoodManagementBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FoodManagement : AppCompatActivity() {
    lateinit var bind: ActivityFoodManagementBinding
    lateinit var listFood: MutableList<MonAn>
    lateinit var adapter: FoodManagementAdapter
    val database = FirebaseDatabase.getInstance()
    val mydata = database.getReference("MonAn")
    lateinit var listCategory: MutableList<LoaiMonAn>
    lateinit var adapterCategory: CategoryFoodAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bind = ActivityFoodManagementBinding.inflate(layoutInflater)
        setContentView(bind.root)

        //Toolbar
        setSupportActionBar(bind.toolbar1)
        supportActionBar?.title = "Quản lý món ăn"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listFood = mutableListOf()
        listCategory = mutableListOf()
        adapterCategory = CategoryFoodAdapter(this, listCategory)
        adapterCategory.getCategoryData()
        adapter = FoodManagementAdapter(this, listFood)
        bind.rcFoodManagement.layoutManager = LinearLayoutManager(this)
        bind.rcFoodManagement.adapter = adapter
        adapter.getFoodData()
       bind.addFood.setOnClickListener(){
           addFood()
       }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return true
    }
    fun addFood(){
        var dialog = layoutInflater.inflate(R.layout.dialog_add_food, null)
        var alertDialog = AlertDialog.Builder(this).setView(dialog).create()
        val foodName = dialog.findViewById<EditText>(R.id.addFoodName)
        val idFood = dialog.findViewById<EditText>(R.id.idFood)
        val price = dialog.findViewById<EditText>(R.id.addFoodPrice)
        val chooseCategory = dialog.findViewById<Button>(R.id.chooseCategory)
        val btnAddFood = dialog.findViewById<Button>(R.id.btnAddFood)
        chooseCategory.setOnClickListener{ view->
            showPopUpMenu(chooseCategory){
                chooseCategory.setText(it)
            }
        }
        btnAddFood.setOnClickListener{
            if (idFood.text.toString().isEmpty()
                || foodName.text.toString().isEmpty() ||
                price.text.toString().isEmpty() || chooseCategory.text.toString().equals("Chọn loại món")){
                Toast.makeText(this, "vui lòng nhập đầy đủ thông tin món ăn", Toast.LENGTH_LONG).show()
            }else{
                mydata.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            for(food in snapshot.children){
                                val checkName = food.child("tenMonAn").getValue(String::class.java)
                                if (food.key.equals(idFood.text.toString().trim())){
                                    Toast.makeText(applicationContext, "Mã món ăn đã tồn tại", Toast.LENGTH_LONG).show()
                                    return
                                }
                                if(checkName.equals(foodName.text.toString().trim())){
                                    Toast.makeText(applicationContext, "tên món ăn không được trùng", Toast.LENGTH_LONG).show()
                                    return
                                }
                            }
                            var prices = price.text.toString().toInt()
                            val food = MonAn(
                                idFood.text.toString(),
                                foodName.text.toString(),
                                chooseCategory.text.toString(),
                                prices,
                                0
                            )
                            mydata.child(idFood.text.toString()).setValue(food)
                                .addOnSuccessListener {
                                    listFood.clear()
                                    adapter.getFoodData()
                                    Toast.makeText(applicationContext, "add successfuly", Toast.LENGTH_SHORT).show()
                                    alertDialog.dismiss()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(applicationContext, "add field", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
        }
        alertDialog.show()
    }
    fun showPopUpMenu(view: View,callback: (String) -> Unit){
        val popupMenu = PopupMenu(this@FoodManagement, view)
        for (i in listCategory){
            popupMenu.menu.add(i.name)
        }
        var tt = ""
        popupMenu.setOnMenuItemClickListener { item ->
            when(item.title){
                in listCategory.map { it.name } -> {
                    val selectedKv = listCategory.find { it.name == item.title.toString() }
                    tt = selectedKv?.name ?: ""
                    callback(tt)

                    true
                }
                else -> {
                    false
                }
            }
        }
        popupMenu.show()
    }
}