package com.example.proyectobetakotlin.EditRegist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectobetakotlin.Login.Login
import com.example.proyectobetakotlin.User
import com.example.proyectobetakotlin.databinding.EditRegisLayoutBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Locale


class SharedEditRegister : AppCompatActivity() {

    private lateinit var binding: EditRegisLayoutBinding

    companion object{
        private var userLog: User? = null
        private var userSelected: User? = null
        private var isRegistering: Boolean? = null
        private val REQUEST_LOGIN = 102
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditRegisLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)


        isRegistering = intent.getBooleanExtra("isRegistering",false)
        userLog = intent.getSerializableExtra("userLog") as User?

        showCorrectLayout()

        if(isRegistering==false){
            fillFields()
            userSelected = intent.getSerializableExtra("userSelected") as User?
        }

        binding.etDate.setOnClickListener{
            closeKeyboard(this,binding.etDate)
            selectBirthDatePicker()
        }

        binding.spTypeAcc.setOnClickListener{
            closeKeyboard(this,binding.spAcc)
        }

        binding.btnRegister.setOnClickListener{
            if (registerUser()) {
                val loginLayout = Intent(this@SharedEditRegister, Login::class.java)
                startActivityForResult(loginLayout, SharedEditRegister.REQUEST_LOGIN)
                finish()
            }
        }

        binding.btnEdit.setOnClickListener{
            if (editUser()) {
                val resultIntent = Intent()
                resultIntent.putExtra("userLogUpdated", userLog)
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }

        binding.btnDelete.setOnClickListener{
            if (deleteUser()) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun registerUser(): Boolean{
        return false
    }

    private fun editUser(): Boolean{
        return false
    }

    private fun deleteUser(): Boolean{
        return false
    }




    private fun fillFields(){
        binding.txtUser.editText?.text = Editable.Factory.getInstance().newEditable(userSelected?.userName.toString())
        binding.txtEmail.editText?.text = Editable.Factory.getInstance().newEditable(userSelected?.userEmail.toString())
        binding.txtPassword.editText?.text = Editable.Factory.getInstance().newEditable(userSelected?.userPass.toString())
        binding.txtPasswordRepeat.editText?.text = Editable.Factory.getInstance().newEditable(userSelected?.userPass.toString())
        binding.etDate.text = Editable.Factory.getInstance().newEditable(userSelected?.userBirth.toString())

        if(userSelected?.userAcc == 1){
            binding.spAcc.text =  Editable.Factory.getInstance().newEditable("Estándar")
        }else binding.spAcc.text =  Editable.Factory.getInstance().newEditable("Administrador")

        if(userSelected?.userStatus == 1){
            binding.switchBaja.isEnabled = true
        }else binding.switchBaja.isEnabled = false

        binding.ivIconImage.setImageURI(Uri.parse(userSelected?.userImage.toString()))

    }

    private fun areFieldsFilled(): Boolean {
        var isFilled = true
        val userName = binding.txtUser.editText?.getText().toString().trim { it <= ' ' }
        val userEmail = binding.txtEmail.editText?.getText().toString().trim { it <= ' ' }
        val userPass = binding.txtPassword.editText?.getText().toString().trim { it <= ' ' }
        val userPassRep = binding.txtPasswordRepeat.editText?.getText().toString().trim { it <= ' ' }
        val date = binding.etDate.editableText.toString().trim { it <= ' ' }

        if(userName.isEmpty()){
            Toast.makeText(this@SharedEditRegister, "Introduzca el nombre de usuario", Toast.LENGTH_SHORT).show()
            isFilled = false
        }else if(userEmail.isEmpty()){
            Toast.makeText(this@SharedEditRegister, "Introduzca el email", Toast.LENGTH_SHORT).show()
            isFilled = false
        }else if(userPass.isEmpty()){
            Toast.makeText(this@SharedEditRegister, "Introduzca la contraseña", Toast.LENGTH_SHORT).show()
            isFilled = false
        }else if(userPassRep.isEmpty()){
            Toast.makeText(this@SharedEditRegister, "Introduzca la contraseña de comprobacion", Toast.LENGTH_SHORT).show()
            isFilled = false
        }else if(userPass!=userPassRep){
            Toast.makeText(this@SharedEditRegister, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            isFilled = false
        }else if(date.isEmpty()){
            Toast.makeText(this@SharedEditRegister, "Introduzca la fecha de nacimiento", Toast.LENGTH_SHORT).show()
            isFilled = false
//        }else if(binding.ivIconImage==null){
//            Toast.makeText(this@SharedEditRegister, "Introduzca la imagen de perfil", Toast.LENGTH_SHORT).show()
//            isFilled = false
        }else if(!spinnerChecked()){
            Toast.makeText(this@SharedEditRegister, "Introduzca el tipo de cuenta", Toast.LENGTH_SHORT).show()
            isFilled = false
        }
        return isFilled
    }

    private fun spinnerChecked(): Boolean{
        val selectedItem = binding.spAcc.text.toString().trim()
        return selectedItem.isNotEmpty()
    }


    private fun showCorrectLayout(){
        val typesOfAccount = listOf("Administrador", "Estándar")

        if (binding.spAcc != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                typesOfAccount
            )
            (binding.spAcc as? AutoCompleteTextView)?.setAdapter(adapter)
        }

        if(isRegistering == true){
            binding.constraintStatus.visibility = View.GONE
            binding.tvRegis.text = "Registro"
            binding.btnDelete.visibility = View.GONE
            binding.btnEdit.visibility = View.GONE
            binding.btnRegister.visibility = View.VISIBLE
            binding.btnLoginLayout.visibility = View.VISIBLE
            binding.tvYaTeHasReg.visibility = View.VISIBLE

        }else{
            binding.tvRegis.text = "Editar"
            binding.constraintStatus.visibility = View.VISIBLE
            binding.btnRegister.visibility = View.GONE
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnLoginLayout.visibility = View.GONE
            binding.tvYaTeHasReg.visibility = View.GONE

            if(userLog?.userAcc == 0){
                binding.constraintStatus.isEnabled = true
                binding.switchBaja.isEnabled = true
            }else{
                binding.constraintStatus.isEnabled = false
                binding.switchBaja.isEnabled = false

            }


        }
    }

    private fun selectBirthDatePicker(){
        val today = MaterialDatePicker.todayInUtcMilliseconds()

        val constraintsBuilder=CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())

        val constraints = constraintsBuilder.build()

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(today)
                .setCalendarConstraints(constraints)
                .build()

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate)
            binding.etDate.setText(formattedDate)
        }

        datePicker.show(supportFragmentManager, "date")
    }

    private fun closeKeyboard(context: Context, view: EditText) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }private fun closeKeyboard(context: Context, view: AutoCompleteTextView) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}