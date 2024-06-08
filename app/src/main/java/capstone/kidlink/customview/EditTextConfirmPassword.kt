package capstone.kidlink.customview

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import capstone.kidlink.R

class EditTextConfirmPassword : AppCompatEditText {

    var matchingPassword: String? = null  // Renamed from passwordToMatch to avoid naming conflict

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
        // Initial setup can include any configuration you need
    }

    fun setPasswordToMatch(password: String) {
        matchingPassword = password  // Update the password to match
    }

    fun validatePassword(): Boolean {
        if (text.toString() != matchingPassword) {
            setError(context.getString(R.string.password_confirm_error))
            return false
        } else {
            error = null
            return true
        }
    }
}
