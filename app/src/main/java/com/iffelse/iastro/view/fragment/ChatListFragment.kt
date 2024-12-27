package com.iffelse.iastro.view.fragment


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.iffelse.iastro.databinding.FragmentChatListBinding
import com.iffelse.iastro.view.ui.ChatActivity
import com.sceyt.chatuikit.persistence.extensions.getPeer
import com.sceyt.chatuikit.presentation.components.channel_list.channels.adapter.ChannelListItem
import com.sceyt.chatuikit.presentation.components.channel_list.channels.listeners.click.ChannelClickListenersImpl
import com.sceyt.chatuikit.presentation.components.channel_list.channels.viewmodel.ChannelsViewModel
import com.sceyt.chatuikit.presentation.components.channel_list.channels.viewmodel.ChannelsViewModelFactory
import com.sceyt.chatuikit.presentation.components.channel_list.channels.viewmodel.bind

class ChatListFragment : Fragment() {
    private lateinit var binding: FragmentChatListBinding
    private val viewModel: ChannelsViewModel by viewModels(factoryProducer = {
        ChannelsViewModelFactory()
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentChatListBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.channelListView.setCustomChannelClickListeners(object : ChannelClickListenersImpl() {
            override fun onChannelClick(item: ChannelListItem.ChannelItem) {
                val sceytChannel = item.channel
                Log.i("TAG", "onChannelClick: ${sceytChannel.getPeer()?.id}")
                val intent = Intent(activity, ChatActivity::class.java)
                intent.putExtra("CHANNEL", sceytChannel)
                intent.putExtra("astrologer_phone", sceytChannel.getPeer()?.id)
                startActivity(intent)
            }
        })


        viewModel.bind(binding.channelListView, this)
    }
}