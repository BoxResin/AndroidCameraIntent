package boxresin.test.androidcameraintent.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** [Activity.startActivityForResult]로 실행한 결과 */
data class ActivityResult(
    val resultCode: Int,
    val data: Intent?
)

abstract class BaseActivity : AppCompatActivity() {
    /** requestCode 별 [startActivity]의 [Continuation] */
    private val startActivityContinuations = mutableMapOf<Int, Continuation<ActivityResult>>()

    suspend fun startActivity(
        intent: Intent,
        requestCode: Int
    ): ActivityResult = suspendCoroutine { cont: Continuation<ActivityResult> ->
        if (requestCode < 0) {
            this.startActivity(intent)
            cont.resume(ActivityResult(Activity.RESULT_OK, null))
            return@suspendCoroutine
        }

        startActivityContinuations[requestCode] = cont
        this.startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.startActivityContinuations[requestCode]?.resume(ActivityResult(resultCode, data))
        this.startActivityContinuations -= requestCode
    }
}
