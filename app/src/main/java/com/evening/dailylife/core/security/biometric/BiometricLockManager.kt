package com.evening.dailylife.core.security.biometric

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.evening.dailylife.R
import com.evening.dailylife.core.data.preferences.PreferencesManager
import com.evening.dailylife.core.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricLockManager @Inject constructor(
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val appContext: Context,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : DefaultLifecycleObserver {

    companion object {
        private const val ALLOWED_AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _lockRequired = MutableStateFlow(false)
    val lockRequired = _lockRequired.asStateFlow()

    private var activityRef: WeakReference<FragmentActivity>? = null
    private var currentPrompt: BiometricPrompt? = null
    private var isPromptActive = false
    private var lockPending = preferencesManager.fingerprintLockEnabled.value
    private var isLockEnabled = preferencesManager.fingerprintLockEnabled.value

    private val promptInfo by lazy {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(appContext.getString(R.string.fingerprint_prompt_title))
            .setSubtitle(appContext.getString(R.string.fingerprint_prompt_subtitle))
            .setDescription(appContext.getString(R.string.fingerprint_prompt_description))
            .setNegativeButtonText(appContext.getString(R.string.fingerprint_prompt_negative))
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        applicationScope.launch {
            preferencesManager.fingerprintLockEnabled.collectLatest { enabled ->
                val previouslyEnabled = isLockEnabled
                isLockEnabled = enabled
                if (!enabled) {
                    lockPending = false
                    _lockRequired.value = false
                    requestCancelAuthentication()
                } else if (!previouslyEnabled) {
                    lockPending = false
                    _lockRequired.value = false
                }
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (isLockEnabled && lockPending) {
            _lockRequired.value = true
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        if (isLockEnabled) {
            lockPending = true
        }
    }

    fun register(activity: FragmentActivity) {
        activityRef = WeakReference(activity)
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                if (_lockRequired.value) {
                    authenticateIfNeeded(activity)
                }
            }

            override fun onPause(owner: LifecycleOwner) {
                if (isPromptActive) {
                    requestCancelAuthentication()
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                if (activityRef?.get() === activity) {
                    activityRef = null
                    currentPrompt = null
                    isPromptActive = false
                }
            }
        })

        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                lockRequired.collectLatest { required ->
                    if (required) {
                        authenticateIfNeeded(activity)
                    }
                }
            }
        }
    }

    private fun authenticateIfNeeded(activity: FragmentActivity) {
        if (!isLockEnabled || !lockRequired.value || isPromptActive) {
            return
        }
        val biometricManager = BiometricManager.from(activity)
        val capability = biometricManager.canAuthenticate(ALLOWED_AUTHENTICATORS)
        if (capability != BiometricManager.BIOMETRIC_SUCCESS) {
            val messageRes = when (capability) {
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> R.string.fingerprint_not_enrolled
                else -> R.string.fingerprint_not_supported
            }
            Toast.makeText(activity, messageRes, Toast.LENGTH_SHORT).show()
            lockPending = false
            _lockRequired.value = false
            // 设备状态发生变化时，自动关闭指纹锁
            preferencesManager.setFingerprintLockEnabled(false)
            return
        }
        val executor = ContextCompat.getMainExecutor(activity)
        currentPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                isPromptActive = false
                lockPending = false
                _lockRequired.value = false
                currentPrompt = null
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                isPromptActive = false
                currentPrompt = null
                handleAuthenticationError(activity, errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                // 保持弹窗，等待下一次指纹输入
            }
        })
        isPromptActive = true
        currentPrompt?.authenticate(promptInfo)
    }

    private fun requestCancelAuthentication() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            cancelAuthenticationInternal()
        } else {
            mainHandler.post { cancelAuthenticationInternal() }
        }
    }

    private fun cancelAuthenticationInternal() {
        try {
            currentPrompt?.cancelAuthentication()
        } catch (ignored: IllegalStateException) {
            // BiometricPrompt already detached; nothing to cancel.
        }
        currentPrompt = null
        isPromptActive = false
    }

    private fun handleAuthenticationError(
        activity: FragmentActivity,
        errorCode: Int,
        errString: CharSequence,
    ) {
        if (errorCode == BiometricPrompt.ERROR_CANCELED) {
            // Prompt was cancelled programmatically (e.g. app moved to background).
            return
        }

        val userDismissed = errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
            errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
            errorCode == BiometricPrompt.ERROR_TIMEOUT
        val lockedOut = errorCode == BiometricPrompt.ERROR_LOCKOUT ||
            errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT
        val shouldDisableLock = errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT ||
            errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS ||
            errorCode == BiometricPrompt.ERROR_SECURITY_UPDATE_REQUIRED

        val message = when {
            lockedOut -> errString
            userDismissed -> activity.getString(R.string.fingerprint_auth_failed_exit)
            else -> errString.takeIf { it.isNotBlank() } ?: activity.getString(R.string.fingerprint_auth_failed_exit)
        }
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

        lockPending = true
        _lockRequired.value = true
        if (shouldDisableLock) {
            preferencesManager.setFingerprintLockEnabled(false)
        }
        activity.moveTaskToBack(true)
    }
}
