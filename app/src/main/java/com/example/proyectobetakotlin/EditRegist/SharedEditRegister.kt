package com.example.proyectobetakotlin.EditRegist

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectobetakotlin.BBDD.UserProvider
import com.example.proyectobetakotlin.Images.OnImageClickListener
import com.example.proyectobetakotlin.Images.RecyclerAdapterImages
import com.example.proyectobetakotlin.Login.Login
import com.example.proyectobetakotlin.User
import com.example.proyectobetakotlin.databinding.EditRegisLayoutBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale


class SharedEditRegister : AppCompatActivity() {

    private lateinit var binding: EditRegisLayoutBinding
    private var userLog: User? = null
    private var userSelected: User? = null
    private var isRegistering: Boolean? = null
    private val imagesList: ArrayList<Bitmap> = ArrayList()
    private var recyclerAdapterImages: RecyclerAdapterImages? = null
    private var imageOn: Boolean? = null

    companion object {
        private val REQUEST_LOGIN = 102
        private const val REQUEST_SELECT_IMAGE = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditRegisLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)


        isRegistering = intent.getBooleanExtra("isRegistering", false)
        userLog = intent.getSerializableExtra("userLog") as User?

        showCorrectLayout()

        if (isRegistering == false) {
            userSelected = intent.getSerializableExtra("userSelected") as User?
            fillFields()
        }

        recyclerAdapterImages = RecyclerAdapterImages(this, imagesList)
        val onImageClickListener = object : OnImageClickListener {
            override fun onImageClick(position: Int) {
                MaterialAlertDialogBuilder(this@SharedEditRegister)
                    .setTitle("Mensaje")
                    .setMessage("¿Qué desea hacer con la imagen?")
                    .setNegativeButton("Eliminar la imagen") { dialog, which ->
                        deletePhoto(position)
                    }
                    .setPositiveButton("Utilizarla de avatar") { dialog, which ->
                        useAsAvatar(position)
                    }
                    .setNeutralButton("Cancelar", null)
                    .show()
            }
        }
        recyclerAdapterImages!!.setOnImageClickListener(onImageClickListener)



        binding.rvImagesUser.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvImagesUser.adapter = recyclerAdapterImages


        binding.etDate.setOnClickListener {
            closeKeyboard(this, binding.etDate)
            selectBirthDatePicker()
        }

        binding.spTypeAcc.setOnClickListener {
            closeKeyboard(this, binding.spAcc)
        }

        binding.btnRegister.setOnClickListener {
            if (registerUser()) {
                val loginLayout = Intent(this@SharedEditRegister, Login::class.java)
                startActivityForResult(loginLayout, SharedEditRegister.REQUEST_LOGIN)
                imagesList.clear()
                finish()
            }
        }

        binding.btnEdit.setOnClickListener {
            if (editUser()) {
                val resultIntent = Intent()
                resultIntent.putExtra("userLogUpdated", userLog)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        binding.btnDelete.setOnClickListener {
            if (deleteUser()) {
                setResult(RESULT_OK)
                finish()
            }
        }

        binding.ivIconImage.setOnClickListener {
            showOptionsDialog()
        }
    }

    private fun registerUser(): Boolean {
        if (areFieldsFilled()) {

            val registro = ContentValues().apply {
                put(
                    UserProvider.Users.COL_USER,
                    binding.txtUser.editText?.getText().toString().trim()
                )
                put(
                    UserProvider.Users.COL_PASSWORD,
                    binding.txtPassword.editText?.getText().toString().trim()
                )
                put(
                    UserProvider.Users.COL_EMAIL,
                    binding.txtEmail.editText?.getText().toString().trim()
                )
                put(UserProvider.Users.COL_DATE, binding.etDate.editableText.toString().trim())
                put(UserProvider.Users.COL_ACCTYPE, getTypeAccount())

                val ivIconImage = binding.ivIconImage

                ivIconImage.isDrawingCacheEnabled = true
                ivIconImage.buildDrawingCache()
                val bitmap = (ivIconImage.drawable as BitmapDrawable).bitmap

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val imageData = outputStream.toByteArray()
                put(UserProvider.Users.COL_ICON, imageData)

                if (isUserExists(binding.txtUser.editText?.getText().toString().trim())) {
                    Toast.makeText(
                        this@SharedEditRegister,
                        "El usuario ya existe",
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                } else if (isEmailExists(binding.txtEmail.editText?.getText().toString().trim())) {
                    Toast.makeText(
                        this@SharedEditRegister,
                        "El correo ya existe",
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }
            }
            val cr = contentResolver
            val newUri = cr.insert(UserProvider.CONTENT_URI_USERS, registro)
            return if (newUri != null) {
                val userId = ContentUris.parseId(newUri)
                for (bitmap in imagesList) {
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    val imageData = baos.toByteArray()
                    val imageValues = ContentValues()
                    imageValues.put(UserProvider.Images.COL_USER_ID, userId)
                    imageValues.put(UserProvider.Images.COL_IMAGE_URL, imageData)
                    val imagesUri: Uri = UserProvider.CONTENT_URI_IMAGES
                    contentResolver.insert(imagesUri, imageValues)
                }
                Toast.makeText(
                    this@SharedEditRegister,
                    "Usuario registrado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()
                generateNotification(binding.txtUser.editText?.getText().toString().trim())
                true
            } else {
                Toast.makeText(
                    this@SharedEditRegister,
                    "Error al guardar el usuario",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
        }
        return false
    }

    private fun editUser(): Boolean {
        var listo = true
        if (areFieldsFilled()) {
            val newUsername = binding.txtUser.editText?.text.toString().trim()
            val newEmail = binding.txtEmail.editText?.text.toString().trim()
            val userCursor = contentResolver.query(
                UserProvider.CONTENT_URI_USERS,
                null,
                "${UserProvider.Users.COL_USER} = ? AND ${UserProvider.Users._ID} != ?",
                arrayOf(newUsername, userSelected?.id.toString()),
                null
            )

            val emailCursor = contentResolver.query(
                UserProvider.CONTENT_URI_USERS,
                null,
                "${UserProvider.Users.COL_EMAIL} = ? AND ${UserProvider.Users._ID} != ?",
                arrayOf(newEmail, userSelected?.id.toString()),
                null
            )

            val userExists = (userCursor?.count ?: 0) > 0
            val emailExists = (emailCursor?.count ?: 0) > 0

            userCursor?.close()
            emailCursor?.close()

            if (userExists) {
                Toast.makeText(this, "El nombre de usuario ya existe", Toast.LENGTH_SHORT).show()
                return false
            } else if (emailExists) {
                Toast.makeText(this, "El correo electrónico ya existe", Toast.LENGTH_SHORT).show()
                return false
            }


            val registro = ContentValues().apply {

                val ivIconImage = binding.ivIconImage
                ivIconImage.isDrawingCacheEnabled = true
                ivIconImage.buildDrawingCache()
                val bitmap = (ivIconImage.drawable as BitmapDrawable).bitmap
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val imageData = outputStream.toByteArray()

                var newAccType = 1

                val selectedOption: String = binding.spAcc.getText().toString()
                if (selectedOption == "Administrador") {
                    newAccType = 0
                }


                val isChangingOwnAccountType =
                    userSelected?.id == userLog?.id && userLog?.userAcc != newAccType


                var newStatus = 0
                if (binding.switchBaja.isChecked) {
                    newStatus = 1
                }

                if (isChangingOwnAccountType) {
                    userLog?.userAcc = newAccType
                }
                put(UserProvider.Users.COL_USER, newUsername)
                put(
                    UserProvider.Users.COL_PASSWORD,
                    binding.txtPassword.editText?.text.toString().trim()
                )
                put(UserProvider.Users.COL_EMAIL, newEmail)
                put(UserProvider.Users.COL_DATE, binding.etDate.text.toString().trim())
                put(UserProvider.Users.COL_ICON, imageData)
                put(UserProvider.Users.COL_ACCTYPE, getTypeAccount())
                put(UserProvider.Users.COL_STATUS, newStatus)
            }

            val cr = contentResolver
            val userUri = ContentUris.withAppendedId(
                UserProvider.CONTENT_URI_USERS,
                userSelected?.id!!.toLong()
            )
            val rowsUpdated = cr.update(userUri, registro, null, null)

            return if (rowsUpdated > 0) {
                Toast.makeText(
                    this@SharedEditRegister,
                    "Usuario actualizado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()
                true
            } else {
                Toast.makeText(
                    this@SharedEditRegister,
                    "Error al actualizar el usuario",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
        }
        return false
    }

    private fun deleteUser(): Boolean {
        return false
    }

    private fun generateNotification(name: String) {

    }

    private fun updateUserImages() {
        val cursor = contentResolver.query(
            UserProvider.CONTENT_URI_IMAGES,
            null,
            UserProvider.Images.COL_USER_ID + " = ?", arrayOf<String>(userSelected?.id.toString()),
            null
        )
        val existingImageIds = java.util.ArrayList<Int>()

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") val imageId =
                    cursor.getInt(cursor.getColumnIndex(UserProvider.Images._ID))
                existingImageIds.add(imageId)
            }
            cursor.close()
        }

        for (existingImageId in existingImageIds) {
            var found = false
            for (bitmap in imagesList) {
                val bitmapId = bitmap.hashCode()
                if (bitmapId == existingImageId) {
                    found = true
                    break
                }
            }
            if (!found) {
                val selection = "${UserProvider.Images._ID}=?"
                val selectionArgs = arrayOf(existingImageId.toString())
                contentResolver.delete(UserProvider.CONTENT_URI_IMAGES, selection, selectionArgs)
            }
        }

        for (bitmap in imagesList) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val imageData = baos.toByteArray()
            val imageValues = ContentValues()
            imageValues.put(UserProvider.Images.COL_USER_ID, userSelected?.id)
            imageValues.put(UserProvider.Images.COL_IMAGE_URL, imageData)
            contentResolver.insert(UserProvider.CONTENT_URI_IMAGES, imageValues)
        }
    }

    @SuppressLint("Range")
    private fun cargarImagenesUsuario(userId: Int) {
        imagesList.clear()

        val cursor = contentResolver.query(
            UserProvider.CONTENT_URI_IMAGES,
            null,
            UserProvider.Images.COL_USER_ID + " = ?",
            arrayOf(userId.toString()),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val imageData = it.getBlob(it.getColumnIndex(UserProvider.Images.COL_IMAGE_URL))
                    if (imageData != null) {
                        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                        imagesList.add(bitmap)
                    }
                } while (it.moveToNext())
            }
        }

        recyclerAdapterImages?.notifyDataSetChanged()
    }

    private fun clearFields() {
        binding.txtUser.editText?.setText("")
        binding.txtPassword.editText?.setText("")
        binding.txtEmail.editText?.setText("")
        binding.txtPasswordRepeat.editText?.setText("")
        binding.etDate.setText("")
        binding.spAcc.setText("")
    }


    private fun isUserExists(userName: String): Boolean {
        val cursor = contentResolver.query(
            UserProvider.CONTENT_URI_USERS,
            null,
            UserProvider.Users.COL_USER + " = ?", arrayOf<String>(userName),
            null
        )
        val exists = cursor != null && cursor.count > 0
        cursor?.close()
        return exists
    }

    private fun isEmailExists(email: String): Boolean {
        val cursor = contentResolver.query(
            UserProvider.CONTENT_URI_USERS,
            null,
            UserProvider.Users.COL_EMAIL + " = ?", arrayOf<String>(email),
            null
        )
        val exists = cursor != null && cursor.count > 0
        cursor?.close()
        return exists
    }

    private fun getTypeAccount(): Int {
        if (binding.spAcc.text.toString() == "Administrador") {
            return 0
        }
        return 1
    }

    private fun deletePhoto(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val iconBitmap = (binding.ivIconImage.getDrawable() as BitmapDrawable).bitmap
            val imageToDeleteBitmap = imagesList[position]
            if (imageToDeleteBitmap != null && imageToDeleteBitmap.sameAs(iconBitmap)) {
                binding.ivIconImage.setImageDrawable(null)
            }
            recyclerAdapterImages!!.notifyItemRemoved(position)
            imagesList.removeAt(position)
            Toast.makeText(this, "La imagen ha sido eliminada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun useAsAvatar(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            binding.ivIconImage.setImageBitmap(imagesList[position])
            Toast.makeText(this, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showOptionsDialog() {
        val alertDialog = AlertDialog.Builder(this)

        alertDialog.apply {
            //setIcon(R.drawable.ic_hello)
            setTitle("Introduzca las imagenes")
            setItems(
                arrayOf<CharSequence>("Seleccionar desde la galeria", "Abrir camara")
            ) { dialog, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                    else -> {}
                }
            }
        }.create().show()
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SELECT_IMAGE && data != null) {
                val selectedImageUri = data.data
                try {
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                    imagesList.add(bitmap)
                    binding.ivIconImage.setImageBitmap(bitmap)
                    recyclerAdapterImages?.notifyDataSetChanged()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                val imageBitmap = data.extras?.get("data") as Bitmap?
                if (imageBitmap != null) {
                    imagesList.add(imageBitmap)
                    binding.ivIconImage.setImageBitmap(imageBitmap)
                    recyclerAdapterImages?.notifyDataSetChanged()
                }
            }
        }
    }


    private fun fillFields() {
        binding.txtUser.editText!!.text =
            Editable.Factory.getInstance().newEditable(userSelected?.userName.toString())
        binding.txtEmail.editText!!.text =
            Editable.Factory.getInstance().newEditable(userSelected?.userEmail.toString())
        binding.txtPassword.editText!!.text =
            Editable.Factory.getInstance().newEditable(userSelected?.userPass.toString())
        binding.txtPasswordRepeat.editText!!.text =
            Editable.Factory.getInstance().newEditable(userSelected?.userPass.toString())
        binding.etDate.text =
            Editable.Factory.getInstance().newEditable(userSelected?.userBirth.toString())

        if (userSelected?.userAcc == 1) {
            binding.spAcc.setText("Estándar", false)
        } else binding.spAcc.setText("Administrador", false)

        if (userSelected?.userStatus == 1) {
            binding.switchBaja.isChecked = true
        } else binding.switchBaja.isChecked = false

        val bitmap = BitmapFactory.decodeByteArray(
            userSelected?.userImage,
            0,
            userSelected?.userImage?.size ?: 0
        )
        binding.ivIconImage.setImageBitmap(bitmap)

        cargarImagenesUsuario(userSelected!!.id)

    }

    private fun areFieldsFilled(): Boolean {
        if (imagesList.size > 0) {
            imageOn = true
        } else imageOn = false

        var isFilled = true
        val userName = binding.txtUser.editText?.getText().toString().trim { it <= ' ' }
        val userEmail = binding.txtEmail.editText?.getText().toString().trim { it <= ' ' }
        val userPass = binding.txtPassword.editText?.getText().toString().trim { it <= ' ' }
        val userPassRep =
            binding.txtPasswordRepeat.editText?.getText().toString().trim { it <= ' ' }
        val date = binding.etDate.editableText.toString().trim { it <= ' ' }

        if (userName.isEmpty()) {
            Toast.makeText(
                this@SharedEditRegister,
                "Introduzca el nombre de usuario",
                Toast.LENGTH_SHORT
            ).show()
            isFilled = false
        } else if (userEmail.isEmpty()) {
            Toast.makeText(this@SharedEditRegister, "Introduzca el email", Toast.LENGTH_SHORT)
                .show()
            isFilled = false
        } else if (userPass.isEmpty()) {
            Toast.makeText(this@SharedEditRegister, "Introduzca la contraseña", Toast.LENGTH_SHORT)
                .show()
            isFilled = false
        } else if (userPassRep.isEmpty()) {
            Toast.makeText(
                this@SharedEditRegister,
                "Introduzca la contraseña de comprobacion",
                Toast.LENGTH_SHORT
            ).show()
            isFilled = false
        } else if (userPass != userPassRep) {
            Toast.makeText(
                this@SharedEditRegister,
                "Las contraseñas no coinciden",
                Toast.LENGTH_SHORT
            ).show()
            isFilled = false
        } else if (date.isEmpty()) {
            Toast.makeText(
                this@SharedEditRegister,
                "Introduzca la fecha de nacimiento",
                Toast.LENGTH_SHORT
            ).show()
            isFilled = false
        } else if (imageOn == false) {
            Toast.makeText(
                this@SharedEditRegister,
                "Introduzca la imagen de perfil",
                Toast.LENGTH_SHORT
            ).show()
            isFilled = false
        } else if (!spinnerChecked()) {
            Toast.makeText(
                this@SharedEditRegister,
                "Introduzca el tipo de cuenta",
                Toast.LENGTH_SHORT
            ).show()
            isFilled = false
        }
        return isFilled
    }

    private fun spinnerChecked(): Boolean {
        val selectedItem = binding.spAcc.text.toString().trim()
        return selectedItem.isNotEmpty()
    }


    private fun showCorrectLayout() {
        val typesOfAccount = listOf("Administrador", "Estándar")

        val adapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, typesOfAccount)
        (binding.spAcc as? AutoCompleteTextView)?.setAdapter(adapter)

        if (isRegistering == true) {
            binding.constraintStatus.visibility = View.GONE
            binding.tvRegis.text = "Registro"
            binding.btnDelete.visibility = View.GONE
            binding.btnEdit.visibility = View.GONE
            binding.btnRegister.visibility = View.VISIBLE
            binding.btnLoginLayout.visibility = View.VISIBLE
            binding.tvYaTeHasReg.visibility = View.VISIBLE

        } else {
            binding.tvRegis.text = "Editar"
            binding.constraintStatus.visibility = View.VISIBLE
            binding.btnRegister.visibility = View.GONE
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnLoginLayout.visibility = View.GONE
            binding.tvYaTeHasReg.visibility = View.GONE

            if (userLog?.userAcc == 0) {
                binding.constraintStatus.isEnabled = true
                binding.switchBaja.isEnabled = true
            } else {
                binding.constraintStatus.isEnabled = false
                binding.switchBaja.isEnabled = false

            }


        }
    }

    private fun selectBirthDatePicker() {
        val today = MaterialDatePicker.todayInUtcMilliseconds()

        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())

        val constraints = constraintsBuilder.build()

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(today)
                .setCalendarConstraints(constraints)
                .build()

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val formattedDate =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate)
            binding.etDate.setText(formattedDate)
        }

        datePicker.show(supportFragmentManager, "date")
    }

    private fun closeKeyboard(context: Context, view: EditText) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun closeKeyboard(context: Context, view: AutoCompleteTextView) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}