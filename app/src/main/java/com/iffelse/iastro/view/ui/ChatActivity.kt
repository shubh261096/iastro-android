package com.iffelse.iastro.view.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.iffelse.iastro.BuildConfig
import com.iffelse.iastro.databinding.ActivityChatBinding
import com.iffelse.iastro.model.BaseErrorModel
import com.iffelse.iastro.model.response.AstrologerStatusResponseModel
import com.iffelse.iastro.model.response.ChatTokenResponseModel
import com.iffelse.iastro.model.response.GetTimeResponseModel
import com.iffelse.iastro.utils.AppConstants
import com.iffelse.iastro.utils.KeyStorePref
import com.iffelse.iastro.utils.OkHttpNetworkProvider
import com.iffelse.iastro.utils.Utils
import com.sceyt.chatuikit.SceytChatUIKit
import com.sceyt.chatuikit.data.managers.connection.ConnectionEventManager
import com.sceyt.chatuikit.extensions.parcelable
import com.sceyt.chatuikit.presentation.components.channel.header.listeners.click.MessageListHeaderClickListenersImpl
import com.sceyt.chatuikit.presentation.components.channel.messages.viewmodels.MessageListViewModel
import com.sceyt.chatuikit.presentation.components.channel.messages.viewmodels.MessageListViewModelFactory
import com.sceyt.chatuikit.presentation.components.channel.messages.viewmodels.bindings.bind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {

    private val TAG = "ChatActivity"
    private lateinit var binding: ActivityChatBinding
    private val viewModel: MessageListViewModel by viewModels(factoryProducer = { factory })
    private var timer: CountDownTimer? = null
    private lateinit var astrologerPhone: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            ActivityChatBinding.inflate(layoutInflater)
                .also { binding = it }
                .root)

        setCustomHeaderClickListeners()

        viewModel.bind(binding.messagesListView, lifecycleOwner = this)
        viewModel.bind(binding.messageInputView, null, lifecycleOwner = this)
        viewModel.bind(binding.headerView, null, lifecycleOwner = this)

        getChatToken()

        if (intent != null && intent.hasExtra("astrologer_phone") && !intent.getStringExtra("astrologer_phone")
                .isNullOrEmpty()
        ) {
            intent.getStringExtra("astrologer_phone")?.let {
                astrologerPhone = it
                getTimeRemaining(it)
            }
        } else {
            binding.timerLayout.visibility = View.GONE
            binding.messageInputView.disableInputWithMessage(message = "Something went wrong!")
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@ChatActivity, HomeActivity::class.java))
                finish()
            }
        })
    }

    private fun getChatToken() {
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/x-www-form-urlencoded"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)

            val jsonObjectBody = JSONObject()
            jsonObjectBody.put("phone", KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID))

            OkHttpNetworkProvider.post(
                BuildConfig.BASE_URL + "common/chat",
                jsonObjectBody,
                headers,
                null,
                null,
                ChatTokenResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<ChatTokenResponseModel> {
                    override fun onResponse(response: ChatTokenResponseModel?) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            if (response != null) {
                                Log.i(TAG, "onResponse: $response")
                                if (response.error == false && !response.chatToken.isNullOrEmpty()) {
                                    connectToChatClient(response.chatToken)
                                } else {
                                    Toast.makeText(
                                        this@ChatActivity,
                                        response.message ?: "Something went wrong!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            Toast.makeText(
                                this@ChatActivity,
                                error?.message ?: "Something went wrong!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                })
        }
    }
    private suspend fun connectToChatClient(token: String) {
        // Step 1: Connect to the chat client
        withContext(Dispatchers.IO) {
            SceytChatUIKit.connect(token)
        }

        // Step 2: Wait for connection result and update profile
        val connectionResult = withContext(Dispatchers.IO) {
            ConnectionEventManager.awaitToConnectSceyt()
        }

        if (connectionResult) {
            withContext(Dispatchers.Main) {
                SceytChatUIKit.chatUIFacade.userInteractor.updateProfile(
                    username = "",
                    firstName = KeyStorePref.getString(AppConstants.KEY_STORE_NAME),
                    lastName = "",
                    avatarUrl = null, // Pass your avatar URL here
                    metadataMap = null // Pass metadata if needed
                )
            }
        } else {
            // Handle connection failure if needed
            println("Failed to connect to the chat client.")
        }
    }

    // TODO: Change the api to fetch the total time in the current time frame if it is 5 rows or anything.
    private fun getTimeRemaining(astrologerPhone: String) {
        Utils.showProgress(this@ChatActivity, "Please wait...")
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/json"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)

            val jsonObjectBody = JSONObject()
            jsonObjectBody.put("user_phone", KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID))
            jsonObjectBody.put("current_time", Utils.getCurrentTime())
            jsonObjectBody.put("astrologer_phone", astrologerPhone)

            OkHttpNetworkProvider.post(
                BuildConfig.BASE_URL_V2 + "booking/get_remaining_time_for_chat",
                jsonObjectBody,
                headers,
                null,
                null,
                GetTimeResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<GetTimeResponseModel> {
                    override fun onResponse(response: GetTimeResponseModel?) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            if (response != null) {
                                Log.i(TAG, "onResponse: $response")
                                if (response.error == false && response.getTime != null) {
                                    startTimer(Utils.convertTimeToMilliseconds(response.getTime.timeRemaining!!))
                                } else {
                                    binding.timerLayout.visibility = View.GONE
                                    binding.messageInputView.disableInputWithMessage(message = "Session Expired!")
                                    Toast.makeText(
                                        this@ChatActivity,
                                        response.message ?: "Something went wrong!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            Toast.makeText(
                                this@ChatActivity,
                                error?.message ?: "Something went wrong!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            binding.timerLayout.visibility = View.GONE
                            binding.messageInputView.disableInputWithMessage(message = "Something went wrong!")
                        }
                    }
                })
        }
    }

    private fun updateAstrologerBusyStatus() {
        lifecycleScope.launch(Dispatchers.IO) {
            val headers = mutableMapOf<String, String>()
            headers["Content-Type"] = "application/x-www-form-urlencoded"
            headers["Authorization"] =
                Utils.encodeToBase64(KeyStorePref.getString(AppConstants.KEY_STORE_USER_ID)!!)

            val jsonObjectBody = JSONObject()
            jsonObjectBody.put("phone", astrologerPhone)
            jsonObjectBody.put("is_busy", false)

            OkHttpNetworkProvider.post(
                BuildConfig.BASE_URL + "astrologers/profile/switch_is_busy_status",
                jsonObjectBody,
                headers,
                null,
                null,
                AstrologerStatusResponseModel::class.java,
                object : OkHttpNetworkProvider.NetworkListener<AstrologerStatusResponseModel> {
                    override fun onResponse(response: AstrologerStatusResponseModel?) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            if (response != null) {
                                Log.i(TAG, "onResponse: $response")
                            }
                        }
                    }

                    override fun onError(error: BaseErrorModel?) {
                        Log.i(TAG, "onError: ")
                        lifecycleScope.launch(Dispatchers.Main) {
                            Utils.hideProgress()
                            Toast.makeText(
                                this@ChatActivity,
                                error?.message ?: "Something went wrong!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                })
        }
    }


    private fun startTimer(duration: Long) {
        timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                binding.timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                updateAstrologerBusyStatus()
                binding.timerTextView.text = "00:00"
                binding.messageInputView.disableInputWithMessage(message = "Please add money!")
            }
        }.start()
    }


    private val factory: MessageListViewModelFactory by lazy(LazyThreadSafetyMode.NONE) {
        MessageListViewModelFactory(requireNotNull(intent.parcelable("CHANNEL")))
    }

    private fun setCustomHeaderClickListeners() {
        binding.headerView.setCustomClickListener(object : MessageListHeaderClickListenersImpl() {
            override fun onAvatarClick(view: View) {
                // Do nothing
            }


            override fun onToolbarClick(view: View) {
                // Do nothing
            }
        })
    }
}