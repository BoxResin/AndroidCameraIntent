package boxresin.test.androidcameraintent.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.content.FileProvider
import boxresin.test.androidcameraintent.BuildConfig
import boxresin.test.androidcameraintent.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : BaseActivity() {
    private val uiScope = MainScope()

    /** 임시 파일 디렉터리 */
    private val tempDir: File
        get() = File(this.filesDir, "tmp/").also {
            if (!it.exists()) {
                it.mkdir()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)
    }

    fun onClick(view: View) {
        uiScope.launch {
            // 사진 찍기
            val imageFile: File = takePicture() ?: return@launch

            // 사진 디코딩
            Glide.with(this@MainActivity).load(imageFile).into(result_image)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.uiScope.cancel()
    }

    /**
     * 카메라 앱을 실행하고 카메라 앱이 닫힐 때까지 코루틴을 정지한다.
     * @return 찍힌 이미지 파일. 유저가 사진을 찍지 않았으면 `null`
     */
    private suspend fun takePicture(): File? {
        // 내부 저장소에 임시 이미지 파일 생성
        val file = File.createTempFile("IMG", ".jpg", tempDir)

        // 카메라 앱에서 임시 이미지 파일에 접근할 수 있도록 콘텐트 프로바이더 URI 생성
        val fileUri: Uri = FileProvider.getUriForFile(
            this@MainActivity, "${BuildConfig.APPLICATION_ID}.provider", file
        )

        // 카메라 앱 실행
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        val result: ActivityResult = startActivity(intent, 1)

        return if (result.resultCode == Activity.RESULT_OK) file else null
    }
}
