package com.example.newsapp.all.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.all.adapter.NewsAdapter
import com.example.newsapp.all.ui.NewsActivity
import com.example.newsapp.all.ui.NewsViewModel
import com.example.newsapp.all.utils.Constants.Companion.NEWS_TIME_DELAY
import com.example.newsapp.all.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.example.newsapp.all.utils.Resource
import com.example.newsapp.databinding.FragmentSearchNewsBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchNewsFragment : Fragment() {
    private var _binding: FragmentSearchNewsBinding? = null
    private val binding get() = _binding!!

    val TAG = "SearchNewsFragment"

    lateinit var newsAdapter: NewsAdapter
    lateinit var viewModel: NewsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchNewsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel

        setUpRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("articles", it)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment2_to_articleFragment,
                bundle
            )
        }

        var job: Job? = null
        binding.etSearch.addTextChangedListener {
            job?.cancel()
            job = MainScope().launch {
                delay(NEWS_TIME_DELAY)
                it?.let {
                    if (it.toString().isNotEmpty()) {
                        viewModel.searchNews(it.toString())
                    }
                }
            }
        }
        viewModel.searchNews.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    hideProgressBar()
                    it.data?.let {
                        newsAdapter.differ.submitList(it.articles.toList())
                        val totalPages = it.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.searchNewsPage == totalPages
                        if (isLastPage) {
                            binding.rvSearchNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    showProgressBar()
                    it.message?.let {
                        Log.e(TAG, "Error occurred: $it")
                    }
                }
                is Resource.Loading -> {
                    hideProgressBar()
                }
            }
        })
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndIsNotAtLasPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndIsNotAtLasPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible
                        && isScrolling
            if (shouldPaginate) {
//                viewModel.searchNews(binding.etSearch.text.toString())
                isScrolling = false
            }
        }
    }

    fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }
}