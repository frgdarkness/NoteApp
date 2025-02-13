package com.sun.noteapp.ui.textnote

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.sun.noteapp.R
import com.sun.noteapp.data.model.Note
import com.sun.noteapp.data.repository.NoteLocalRepository
import com.sun.noteapp.data.source.local.LocalDataSource
import com.sun.noteapp.data.source.local.NoteDatabase
import com.sun.noteapp.ui.base.BaseDialog
import com.sun.noteapp.utils.*
import kotlinx.android.synthetic.main.activity_text_note.*
import kotlinx.android.synthetic.main.toolbar_text_note.*
import java.text.SimpleDateFormat
import java.util.*

class TextNoteActivity : AppCompatActivity(),
    View.OnClickListener,
    TextNoteContract.View {
    override fun backToListNote() {
        finish()
    }

    private val local by lazy {
        LocalDataSource(NoteDatabase(this))
    }
    private val repository by lazy {
        NoteLocalRepository(local)
    }
    private val presenter by lazy {
        TextNotePresenter(this, repository)
    }
    private val simpleDateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    private val adapter = LabelAdapter()
    private val date = Calendar.getInstance()
    private var note: Note? = null
    private var listLabel = listOf<String>()
    private var remindTime = NONE
    private var id = 0
    private var status = "0"
    private var color = 0
    private var password = NONE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_note)
        initView()
        initData()
    }

    private fun initView() {
        setSupportActionBar(toolbarTitle)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarBottom.inflateMenu(R.menu.bottom_option_menu)
        toolbarBottom.setOnMenuItemClickListener { item: MenuItem? ->
            openDialog(item?.itemId)
        }
        imageButtonHeaderColorTextNote.setOnClickListener(this)
        buttonAlarmTextNote.setOnClickListener(this)
        imageButtonBottomSaveTextNote.setOnClickListener(this)
        recyclerTextNoteLabel.adapter = adapter
    }

    private fun openDialog(itemId: Int?) = when (itemId) {
        R.id.bottomMenuDelete -> {
            if (id != 0) showDeleteDialog()
            true
        }
        R.id.bottomMenuLock -> showPassDialog()
        R.id.bottomMenuDeleteRemind -> {
            if (buttonAlarmTextNote.text.isNotEmpty()) showDeleteAlarmDialog()
            true
        }
        else -> false

    }

    private fun showDeleteAlarmDialog() {
        AlertDialog.Builder(this)
            .setMessage(R.string.message_alarm_off)
            .setPositiveButton(R.string.button_yes) { _, _ ->
                buttonAlarmTextNote.text = null
                remindTime = NONE
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> }
            .show()
    }

    private fun showPassDialog(): Boolean {
        val input = EditText(this)
        input.apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            )
            inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            transformationMethod = PasswordTransformationMethod.getInstance()
            if (password != NONE) {
                setText(password)
                setSelection(password.length)
            }
        }

        AlertDialog.Builder(this)
            .setMessage(R.string.message_input_password)
            .setView(input)
            .setPositiveButton(R.string.button_set) { _, _ ->
                password = input.text.toString()
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> }
            .show()
        return true
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.message_confirm_delete)
            .setMessage(R.string.message_delete)
            .setPositiveButton(R.string.button_yes) { _, _ ->
                status = getCurrentDate()
                saveNote()
            }
            .setNegativeButton(R.string.button_no) { _, _ -> }
            .show()
    }

    private fun initData() {
        note = intent.getParcelableExtra(INTENT_NOTE_DETAIL)
        Log.i("abc","$note")
        note?.let {
            id = it.id
            if (it.remindTime != NONE) {
                date.time = simpleDateFormat.parse(it.remindTime)
                buttonAlarmTextNote.text = it.remindTime
            }
            editTitleTextNote.apply {
                setText(it.title)
                setSelection(it.title.length)
            }
            if (it.content != NONE) lineContentTextNote.apply {
                setText(it.content)
                setSelection(it.content.length)
            }
            if (it.remindTime != NONE) buttonAlarmTextNote.text = it.remindTime
            textUpdateTime.text = it.modifyTime
            color = it.color
            password = it.password
            if (it.label != NONE) listLabel =
                ConvertString.labelStringDataToLabelList(it.label)
            adapter.submitList(listLabel)
        }
        updateView(color)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imageButtonHeaderColorTextNote -> showImageColorDialog()
            R.id.buttonAlarmTextNote -> showDateTimePickerDialog()
            R.id.imageButtonBottomSaveTextNote -> {
                if (editTitleTextNote.text.isEmpty()) {
                    showToast(R.string.message_error_save.toString())
                } else {
                    saveNote()
                }
            }
        }
    }

    private fun showImageColorDialog() {
        ImageColorDialog(
            this,
            R.layout.dialog_color,
            object : BaseDialog.OnLoadDialogCallback<Int> {
                override fun onSuccess(parrams: Int) {
                    updateView(parrams)
                }
            }).show()
    }

    private fun updateView(params: Int) {
        color = params
        val mediumColor = ColorPicker.getMediumColor(params)
        val lightColor = ColorPicker.getLightColor(params)
        imageButtonHeaderColorTextNote.setBackgroundResource(mediumColor)
        toolbarTitle.setBackgroundResource(mediumColor)
        lineContentTextNote.setBackgroundResource(lightColor)
        horizontalScrollLabel.setBackgroundResource(mediumColor)
        buttonAlarmTextNote.setBackgroundResource(mediumColor)
        toolbarBottom.setBackgroundResource(mediumColor)
        if (listLabel.isEmpty()) horizontalScrollLabel.gone()
    }

    private fun showDateTimePickerDialog() {
        DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                date.set(year, monthOfYear, dayOfMonth)
                TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minutes ->
                    date.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    date.set(Calendar.MINUTE, minutes)
                    buttonAlarmTextNote.text = simpleDateFormat.format(date.time)
                    remindTime = simpleDateFormat.format(date.time)
                }, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), true).show()
            },
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH),
            date.get(Calendar.DATE)
        ).show()
    }

    private fun saveNote() {
        val values = ContentValues()
        values.put(NoteDatabase.NOTE_TITLE, editTitleTextNote.text.toString())
        values.put(NoteDatabase.NOTE_CONTENT, lineContentTextNote.text.toString())
        values.put(NoteDatabase.NOTE_TYPE, TYPE_TEXT_NOTE)
        values.put(
            NoteDatabase.NOTE_LABEL,
            "music_us_uk_best_music_us_uk_best_music_us_uk_best_music_us_uk_best_music_us_uk_best"
        )
        values.put(NoteDatabase.NOTE_COLOR, color)
        values.put(NoteDatabase.NOTE_MODIFYTIME, getCurrentTime())
        values.put(NoteDatabase.NOTE_REMINDTIME, remindTime)
        values.put(NoteDatabase.NOTE_PASSWORD, password)
        values.put(NoteDatabase.NOTE_HIDE, status)
        if (id == 0) {
            presenter.addNote(values)
        } else {
            presenter.editNote(id, values)
        }
    }

    companion object {
        fun getIntent(context: Context, note: Note?) =
            Intent(context, TextNoteActivity::class.java).apply {
                note?.let {
                    putExtra(INTENT_NOTE_DETAIL, it)
                }
            }
        const val DATE_FORMAT = "yyyy-MM-dd HH:mm"
    }
}
