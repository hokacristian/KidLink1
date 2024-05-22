package capstone.kidlink.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import capstone.kidlink.R

class EditTextEmailortu : AppCompatEditText {
    private val errorMessage: String by lazy { context.getString(R.string.invalidEmail) }
    private val emailPattern: Regex by lazy {
        context.getString(R.string.regex_email).trimIndent().toRegex()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null && !isEmailValid(s.toString())) {
                    handleError()
                } else {
                    clearError()
                }
            }
        })
    }

    private fun clearError() {
        error = null
    }

    private fun handleError() {
        error = errorMessage
    }

    private fun isEmailValid(email: String): Boolean {
        return emailPattern.matches(email)
    }
}