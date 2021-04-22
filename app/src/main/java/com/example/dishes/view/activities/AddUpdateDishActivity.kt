package com.example.dishes.view.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.dishes.R
import com.example.dishes.application.FavDishApplication
import com.example.dishes.databinding.ActivityAddUpdateDishBinding
import com.example.dishes.databinding.DialogCustomImageSelectionBinding
import com.example.dishes.databinding.DialogCustomListBinding
import com.example.dishes.model.entities.FavDish
import com.example.dishes.utils.Constants
import com.example.dishes.view.adapters.CustomListItemAdapter
import com.example.dishes.viewmodel.FavDishViewModel
import com.example.dishes.viewmodel.FavDishViewModelFactory
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*


class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mBinding: ActivityAddUpdateDishBinding
    private var mImagePath: String = ""
    private lateinit var mCustomListDialog: Dialog

    private val mFavDishViewModel: FavDishViewModel by viewModels{
        FavDishViewModelFactory((application as FavDishApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setupActionBar()
        mBinding.ivAddDishImage.setOnClickListener(this)
        mBinding.etType.setOnClickListener(this)
        mBinding.etCategory.setOnClickListener(this)
        mBinding.etCookingTime.setOnClickListener(this)
        mBinding.btnAddDish.setOnClickListener(this)
    }

    private fun setupActionBar() {
        setSupportActionBar(mBinding.toolbarAddDishActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbarAddDishActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.iv_add_dish_image -> {
                customImageSelectionDialog()
                return
            }
            R.id.et_type -> {
                customItemsListDialog("SELECT DISH TYPE", Constants.dishTypes(), Constants.DISH_TYPE)
                return
            }
            R.id.et_category -> {
                customItemsListDialog("SELECT DISH CATEGORY", Constants.dishCategories(), Constants.DISH_CATEGORY)
                return
            }
            R.id.et_cooking_time -> {
                customItemsListDialog("SELECT COOKING TIME(in minutes)", Constants.dishCookTime(), Constants.DISH_COOKING_TIME)
                return
            }
            R.id.btn_add_dish -> {
                val title = mBinding.etTitle.text.toString().trim { it <= ' '}
                val type = mBinding.etType.text.toString().trim { it <= ' '}
                val category = mBinding.etCategory.text.toString().trim { it <= ' '}
                val cookingTime = mBinding.etCookingTime.text.toString().trim { it <= ' '}
                val ingredients = mBinding.etIngredients.text.toString().trim { it <= ' '}
                val directionToCook = mBinding.etDirectionToCook.text.toString().trim { it <= ' '}

                when{
                    TextUtils.isEmpty(mImagePath) -> {
                        Toast.makeText(this@AddUpdateDishActivity, "Select Dish Image", Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(title) -> {
                        Toast.makeText(this@AddUpdateDishActivity, "Enter Image Title", Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(type) -> {
                        Toast.makeText(this@AddUpdateDishActivity, "Select Dish Type", Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(category) -> {
                        Toast.makeText(this@AddUpdateDishActivity, "Select Dish Category", Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(cookingTime) -> {
                        Toast.makeText(this@AddUpdateDishActivity, "Select Cooking Time", Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(ingredients) -> {
                        Toast.makeText(this@AddUpdateDishActivity, "Enter Dish Ingredients", Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(directionToCook) -> {
                        Toast.makeText(this@AddUpdateDishActivity, "Enter Direction To Cook", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val favDishDetails = FavDish(
                            mImagePath,
                            Constants.DISH_IMAGE_SOURCE_LOCAL,
                            title,
                            type,
                            category,
                            ingredients,
                            cookingTime,
                            directionToCook,
                            false
                        )
                        mFavDishViewModel.insert(favDishDetails)
                        Toast.makeText(this@AddUpdateDishActivity,
                            "You have successfully added dish details",
                            Toast.LENGTH_SHORT).show()
                        Log.i("Insertion", "Success")
                        finish()
                    }
                }



            }
        }
    }

    private fun customImageSelectionDialog() {
        val dialog = Dialog(this)
        val binding: DialogCustomImageSelectionBinding = DialogCustomImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.tvCamera.setOnClickListener {
            Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
                //Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if(report.areAllPermissionsGranted()){
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            startActivityForResult(intent, CAMERA)
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

            }).onSameThread().check()
            dialog.dismiss()
        }

        binding.tvGallery.setOnClickListener {
            Dexter.withContext(this).withPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE
                //Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object: PermissionListener{
                override fun onPermissionGranted(permission: PermissionGrantedResponse?) {
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(this@AddUpdateDishActivity, "You have denied the gallery access", Toast.LENGTH_SHORT).show()
                }

            }).onSameThread().check()

            dialog.dismiss()
        }

        dialog.show()
    }

    fun selectedListItem(item: String, selection: String){
        when(selection){
            Constants.DISH_TYPE -> {
                mCustomListDialog.dismiss()
                mBinding.etType.setText(item)
            }
            Constants.DISH_CATEGORY -> {
                mCustomListDialog.dismiss()
                mBinding.etCategory.setText(item)
            }
            Constants.DISH_COOKING_TIME -> {
                mCustomListDialog.dismiss()
                mBinding.etCookingTime.setText(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == CAMERA){
                data?.extras?.let{
                    val thumbnail: Bitmap = data.extras!!.get("data") as Bitmap
//                    mBinding.ivDishImage.setImageBitmap(thumbnail)
                    Glide.with(this).load(thumbnail).centerCrop().into(mBinding.ivDishImage)
                    mImagePath = saveImageToInternalStorage(thumbnail)
                    Log.i("ImagePath", mImagePath)
                    mBinding.ivAddDishImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_vector_edit))
                }
            }
            if(requestCode == GALLERY){
                data?.let{
                    val selectedImageUri = data.data
//                    mBinding.ivDishImage.setImageURI(selectedImageUri)
                    Glide.with(this)
                        .load(selectedImageUri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object: RequestListener<Drawable>{
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("TAG", "Error Loading Image", e)
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                resource?.let{
                                    val bitmap: Bitmap = resource.toBitmap()
                                    mImagePath = saveImageToInternalStorage(bitmap)
                                    Log.i("ImagePath", mImagePath)
                                }
                                return false
                            }

                        })
                        .centerCrop()
                        .into(mBinding.ivDishImage)
                    mBinding.ivAddDishImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_vector_edit))
                }
            }
        }else if(resultCode == Activity.RESULT_CANCELED){
            Log.e("cancelled", "User cancelled image selection ")
        }
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("Looks like your have turned off the permissions required for this feature." +
                " You can turn them on in application settings").setPositiveButton("GO TO SETTINGS"){
            _,_ -> try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }catch (e: ActivityNotFoundException){
                e.printStackTrace()
        }
        }.setNegativeButton("CANCEL"){
            dialog, _ -> dialog.dismiss()
        }.show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        try{
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }

        return file.absolutePath
    }

    private fun customItemsListDialog(title: String, itemsList: List<String>, selection: String){
        mCustomListDialog = Dialog(this)
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        mCustomListDialog.setContentView(binding.root)
        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(this)
        val adapter = CustomListItemAdapter(this, itemsList, selection)
        binding.rvList.adapter = adapter
        mCustomListDialog.show()
    }

    companion object {
        private const val CAMERA = 1
        private const val GALLERY = 2
        private const val IMAGE_DIRECTORY = "favDishImages"
    }
}
