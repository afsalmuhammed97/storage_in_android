package com.plcoding.androidstorage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.plcoding.androidstorage.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {
private lateinit var binding: ActivityMainBinding
private lateinit var internalStoragePhotoAdapter: InternalStoragePhotoAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        internalStoragePhotoAdapter=InternalStoragePhotoAdapter {
                      val isDeletionSuccessFull=deleteFileFromInternalStorage(it.name)


            if (isDeletionSuccessFull){

                loadPhotosFromInternalStorageInToRecyclerView()
                Toast.makeText( this,"Photo deleted successfully", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText( this,"Fail to delete photo", Toast.LENGTH_SHORT).show()
            }

        }

        val takePhoto=registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {

        val isPrivet=binding.switchPrivate.isChecked

            if(isPrivet){
                if (it != null) {
                  val saveSuccessFully= savePhotoToInternalStorage(UUID.randomUUID().toString(),it)

                    if (saveSuccessFully){
                        loadPhotosFromInternalStorageInToRecyclerView()
                        Toast.makeText( this,"Photo saved successfully", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText( this,"Fail to save photo", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            setUpInternalStorageRecyclerView()
        }


        binding.btnTakePhoto.setOnClickListener{

            takePhoto.launch()
        }

        loadPhotosFromInternalStorageInToRecyclerView()
    }


    private fun setUpInternalStorageRecyclerView()=binding.rvPrivatePhotos.apply {
        adapter=internalStoragePhotoAdapter
        layoutManager= StaggeredGridLayoutManager(3,RecyclerView.VERTICAL)

    }

    private fun loadPhotosFromInternalStorageInToRecyclerView(){
        lifecycleScope.launch {
            val photos=loadPhotosFromInternalStorage()
                internalStoragePhotoAdapter.submitList(photos)
        }
    }

    private fun deleteFileFromInternalStorage(fileName: String):Boolean{
        return  try {

            deleteFile(fileName)
        }catch (e:Exception){
            e.printStackTrace()
            false
        }
    }




    private suspend fun loadPhotosFromInternalStorage():List<InternalStoragePhoto>{

        return  withContext(Dispatchers.IO){

            val files=filesDir.listFiles()

            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg")  }?.map {
                val bytes=it.readBytes()
                val bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.size)
                InternalStoragePhoto(it.name,bitmap)

            }?: listOf()
        }
    }



    private fun savePhotoToInternalStorage(fileName:String,bitmap: Bitmap):Boolean{
        return try {

            openFileOutput("$fileName.jpg", MODE_PRIVATE).use {stream ->

               if (!bitmap.compress(Bitmap.CompressFormat.JPEG,95,stream)){

                   throw IOException("Could't save bitmap") }
            }
            true
        }catch (e:IOException){
            e.printStackTrace()
            false
        }
    }
}