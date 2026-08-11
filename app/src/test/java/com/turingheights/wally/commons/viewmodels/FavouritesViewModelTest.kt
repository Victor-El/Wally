package com.turingheights.wally.commons.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.turingheights.wally.commons.models.Photo
import com.turingheights.wally.commons.repositories.FavouritePhotosRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavouritesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val repository: FavouritePhotosRepository = mockk(relaxed = true)
    private lateinit var viewModel: FavouritesViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockPhoto = mockk<Photo>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FavouritesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getFavourites calls repository fetch`() {
        every { repository.fetch() } returns flowOf(emptyList())
        viewModel.getFavourites()
        coVerify { repository.fetch() }
    }

    @Test
    fun `removeFavourite calls repository remove`() = runTest {
        viewModel.removeFavourite(mockPhoto)
        coVerify { repository.remove(mockPhoto) }
    }
}
