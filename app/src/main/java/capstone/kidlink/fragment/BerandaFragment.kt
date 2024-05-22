package capstone.kidlink.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import capstone.kidlink.R
import capstone.kidlink.adapter.CarouselAdapter

class BerandaFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: CarouselAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)
        viewPager = view.findViewById(R.id.viewPager)
        adapter = CarouselAdapter(listOf(R.drawable.register, R.drawable.login, R.drawable.welcome))
        viewPager.adapter = adapter
        return view
    }
}