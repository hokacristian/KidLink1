package capstone.kidlink.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import capstone.kidlink.R
import capstone.kidlink.adapter.VideoAdapter
import capstone.kidlink.databinding.FragmentBerandaBinding
import kotlin.math.abs

class BerandaFragment : Fragment() {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VideoAdapter
    private val videoList = arrayListOf(
        R.raw.video1,
        R.raw.video2,
        R.raw.video3
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setUpTransformer()
        binding.viewPager2.registerOnPageChangeCallback(adapter.pageChangeCallback)
    }

    private fun setUpTransformer() {
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }

        binding.viewPager2.setPageTransformer(transformer)
    }

    private fun init() {
        adapter = VideoAdapter(videoList, requireContext(), binding.viewPager2)
        binding.viewPager2.adapter = adapter
        binding.viewPager2.offscreenPageLimit = 3
        binding.viewPager2.clipToPadding = false
        binding.viewPager2.clipChildren = false
        binding.viewPager2.getChildAt(0)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.releaseAllPlayers()
        binding.viewPager2.adapter = null
        _binding = null
        binding.viewPager2.unregisterOnPageChangeCallback(adapter.pageChangeCallback)
    }
}
