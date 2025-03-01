package com.example.ridesynq.view

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ridesynq.models.BooksModel
import com.example.ridesynq.viewmodel.BooksScreenVM
import com.example.ridesynq.R
import com.example.ridesynq.models.AutoresizedText
import com.example.ridesynq.view.navigation.GraphRoute


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripScreen(
    booksViewModel: BooksScreenVM = viewModel(),
    //retrofitViewModel: RetrofitVM = viewModel(), authViewModel: AuthVM
) {
    //val bookApi = retrofitViewModel.retrofit.create(BooksApi::class.java)
    val tokenState = remember { mutableStateOf("") }

    //  val queryState = remember { mutableStateOf("") }
    // val activeState = remember { mutableStateOf(false) }

    val bookListState = remember { mutableStateOf<List<BooksModel>?>(null) }
    val tabItems = listOf(stringResource(R.string.books_screen_tab_b), stringResource(R.string.books_screen_tab_h))
    val amount = 30
    /* val items = remember {
         mutableListOf(
             "Honor"
         )
     }*/
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState { tabItems.size }
    /*
    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            selectedTabIndex = pagerState.currentPage
        }
    }
    LaunchedEffect(booksViewModel) {
        booksViewModel.getListOfBooks(bookApi = bookApi, token = tokenState.value, amount = amount)
    }

     */
    // Получение текста из ViewModel
    DisposableEffect(booksViewModel) {
        val observerBooksList = Observer<List<BooksModel>> { _booksList ->
            bookListState.value = _booksList
        }
        booksViewModel.booksList.observeForever(observerBooksList)
        val observerToken = Observer<String> { token ->
            tokenState.value = token
        }
      //  authViewModel.tokenState.observeForever(observerToken)
        /* val observerQuery = Observer<String> { query ->
             queryState.value = query
         }*/
        //  booksViewModel.query.observeForever(observerQuery)
        onDispose {
            booksViewModel.booksList.observeForever(observerBooksList)
          //  authViewModel.tokenState.observeForever(observerToken)
            //     booksViewModel.query.observeForever(observerQuery)
        }

    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.padding(top = 60.dp))
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabItems.forEachIndexed() { index, item ->
                Tab(selected = index + 1 == selectedTabIndex,
                    onClick = {
                        selectedTabIndex = index
                    },
                    text = {
                        Text(item)
                    }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { index ->
            if (index == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterHorizontally)
                ) {
                    //костыль
                    item {
                        Text("Нет поездок", modifier = Modifier.padding(start = 150.dp, top = 50.dp))
                    }
                    bookListState.value?.let { bookList ->
                        items(bookList) { book ->
                            BooksScreenItems(book = book)
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, end = 20.dp)
                                    .clip(
                                        RoundedCornerShape(50)
                                    ),
                                thickness = 2.dp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                    }
                    // костыль
                    item {
                        Button(
                            onClick = {

                            },
                            shape = RoundedCornerShape(30),
                            modifier = Modifier
                                .padding(start = 120.dp, top = 450.dp)
                                .width(180.dp)
                                .height(50.dp)

                        ) {
                            AutoresizedText(
                                stringResource(R.string.create_trip),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                }
            } else {
                LazyColumn {
                    item {
                        Text("Нет поездок")
                    }
                }
            }

        }

    }
}