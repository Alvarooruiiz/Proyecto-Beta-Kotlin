package com.example.proyectobetakotlin.List

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectobetakotlin.EditRegist.SharedEditRegister
import com.example.proyectobetakotlin.R
import com.example.proyectobetakotlin.User

class RecyclerAdapterList(
    private val context: Context,
    private val usuarios: ArrayList<User>,
    private val userLog: User?,
    private val usuariosMarcados: ArrayList<User>
) : RecyclerView.Adapter<RecyclerAdapterList.ViewHolder>() {


    private var onAvatarClickListener: OnAvatarClickListener? = null
    private var onItemCardViewClickListener: OnItemCardViewClickListener? = null

    companion object {
        private const val REQUEST_EDIT_USER = 123
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_cardview_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userSelected = usuarios[position]
        holder.username.text = userSelected.userName
        holder.correo.text = userSelected.userEmail
        holder.fechaNacimiento.text = userSelected.userBirth
        holder.tipoCuenta.text = if (userSelected.userAcc == 1) "Normal" else "Admin"

        holder.checkBox.visibility = if (userLog?.userAcc == 0) View.VISIBLE else View.INVISIBLE

        val imageData = userSelected.userImage
        if (imageData != null) {
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            holder.avatar.setImageBitmap(bitmap)
        } else {
            holder.avatar.setImageResource(R.drawable.user_icon)
        }

        val backgroundColor =
            if (userSelected.userStatus == 1) Color.parseColor("#FFBABABA") else Color.parseColor("#D8F4B6")
        holder.constraintLayout.setBackgroundColor(backgroundColor)

        holder.itemView.setOnClickListener {
            if (userLog != null && userLog.userAcc == 0 || (userLog?.userAcc == 1 && userSelected.userName == userLog.userName)) {
                val intent = Intent(context, SharedEditRegister::class.java)
                intent.putExtra("isRegistering", false)
                intent.putExtra("userSelected", userSelected)
                intent.putExtra("userLog", userLog)
                (context as UserListActivity).startActivityForResult(intent, REQUEST_EDIT_USER)
            } else {
                Toast.makeText(
                    context,
                    "No tienes permiso para editar este perfil",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        holder.avatar.setOnClickListener {
            onAvatarClickListener?.onAvatarClick(userSelected)
        }

        holder.checkBox.isChecked = userSelected.isChecked

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            usuarios[position].isChecked = isChecked
            userSelected.isChecked = isChecked
            if (isChecked) {
                usuariosMarcados.add(usuarios[position])
            } else {
                usuariosMarcados.remove(usuarios[position])
            }
        }
    }

    fun updateData(newData: List<User>) {
        usuarios.clear()
        usuarios.addAll(newData)
        notifyDataSetChanged()
    }

    fun setOnAvatarClickListener(listener: ListFragment) {
        this.onAvatarClickListener = listener
    }

    override fun getItemCount(): Int {
        return usuarios.size
    }

    fun toggleCheckbox(position: Int) {
        val usuario = usuarios[position]
        usuario.isChecked = !usuario.isChecked
        notifyItemChanged(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelection() {
        for (usuario in usuarios) {
            usuario.isChecked
        }
        notifyDataSetChanged()
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var avatar: ImageView
        var username: TextView
        var correo: TextView
        var fechaNacimiento: TextView
        var tipoCuenta: TextView
        var checkBox: CheckBox
        var constraintLayout: ConstraintLayout

        init {
            avatar = itemView.findViewById<ImageView>(R.id.ivAvatarList)
            username = itemView.findViewById<TextView>(R.id.tvUserList)
            correo = itemView.findViewById<TextView>(R.id.tvMailList)
            fechaNacimiento = itemView.findViewById<TextView>(R.id.tvDateList)
            tipoCuenta = itemView.findViewById<TextView>(R.id.tvAccList)
            constraintLayout = itemView.findViewById<ConstraintLayout>(R.id.constraintColor)
            checkBox = itemView.findViewById<CheckBox>(R.id.cbCheck)
        }
    }


}