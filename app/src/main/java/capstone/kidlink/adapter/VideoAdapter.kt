package capstone.kidlink.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import capstone.kidlink.databinding.VideoPlayerItemBinding

class VideoAdapter(
    private val videoList: List<Int>,
    private val context: Context,
    private val viewPager2: ViewPager2
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {
    private val activeViewHolders = mutableListOf<VideoViewHolder>()

    val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            // Hentikan semua video yang aktif
            activeViewHolders.forEach { it.pauseVideo() }
        }
    }

    init {
        viewPager2.registerOnPageChangeCallback(pageChangeCallback)
    }

    inner class VideoViewHolder(private val binding: VideoPlayerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var player: ExoPlayer? = null

        @OptIn(UnstableApi::class)
        fun bind(videoResId: Int, position: Int, currentItem: Any) {
            val videoUri = RawResourceDataSource.buildRawResourceUri(videoResId)
            val mediaItem = MediaItem.fromUri(videoUri)

            player = ExoPlayer.Builder(context).build().also { exoPlayer ->
                binding.exoPlayerView.player = exoPlayer
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()

                // Hapus autoplay saat ViewHolder di-bind
                exoPlayer.playWhenReady = false

                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            exoPlayer.seekTo(0)
                            exoPlayer.playWhenReady = false
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("VideoViewHolder", "ExoPlayer error: ${error.message}")
                    }
                })

                binding.exoPlayerView.setOnClickListener {
                    if (player?.isPlaying == true) {
                        pauseVideo()
                    } else {
                        // Hentikan semua video yang aktif sebelum memainkan video yang dipilih
                        activeViewHolders.forEach { if (it != this) it.pauseVideo() }
                        playVideo()
                    }
                }
            }
        }

        fun releasePlayer() {
            player?.release()
            player = null
        }

        @OptIn(UnstableApi::class)
        fun playVideo() {
            player?.play()
            binding.exoPlayerView.showController()
        }

        @OptIn(UnstableApi::class)
        fun pauseVideo() {
            player?.pause()
            player?.playWhenReady = false
            binding.exoPlayerView.hideController()
        }
    }

    override fun onViewAttachedToWindow(holder: VideoViewHolder) {
        super.onViewAttachedToWindow(holder)
        activeViewHolders.add(holder)
    }

    override fun onViewDetachedFromWindow(holder: VideoViewHolder) {
        super.onViewDetachedFromWindow(holder)
        activeViewHolders.remove(holder)
        holder.pauseVideo()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideoPlayerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoList[position], position, viewPager2.currentItem)
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }

    override fun getItemCount(): Int = videoList.size

    fun pauseAllPlayers() {
        activeViewHolders.forEach { it.pauseVideo() }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewPager2.unregisterOnPageChangeCallback(pageChangeCallback)
        releaseAllPlayers()
    }

    fun releaseAllPlayers() {
        activeViewHolders.forEach { it.releasePlayer() }
        activeViewHolders.clear()
    }

}
