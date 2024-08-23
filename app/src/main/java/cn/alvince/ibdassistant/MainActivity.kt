package cn.alvince.ibdassistant

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import cn.alvince.ibdassistant.alarm.InfusionAlarmFragment
import cn.alvince.ibdassistant.main.model.NavItem
import cn.alvince.ibdassistant.profile.MineFragment
import cn.alvince.ibdassistant.toolbox.ToolboxFragment
import cn.alvince.ibdassistant.ui.theme.IBDAssistantTheme

class MainActivity : FragmentActivity() {

    private var contentContainer: FragmentContainerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IBDAssistantTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainPage("Android") { context ->
                        contentContainer ?: FragmentContainerView(context).apply {
                            id = R.id.contentMainPage
                            contentContainer = this
                        }
                    }
                }
            }
        }
        // install top content container
        addContentView(
            FrameLayout(this).apply {
                id = R.id.contentTopAndroidView
            },
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        contentContainer = null
    }

    companion object Ui {
        private val navItems = arrayOf(NavItem.Home, NavItem.Toolbox, NavItem.Mine)

        @Composable
        fun MainPage(name: String, contentSupplier: (Context) -> View) {
            val contentAndroidViewSupplier = remember { contentSupplier }
            Column(Modifier.fillMaxWidth()) {
                Spacer(Modifier.statusBarsPadding().fillMaxWidth())
                val navPageTag = remember { mutableStateOf(NavItem.Home) }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1F, true)
                ) {
                    Text(
                        text = "Hello $name!",
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center,
                    )
                    if (!LocalView.current.isInEditMode) {
                        MainPageContent(navPageTag, Modifier.fillMaxWidth(), contentAndroidViewSupplier)
                    }
                }
                MainNavigation(navPageTag) {
                    navPageTag.value = it
                }
            }
        }

        @Composable
        fun MainNavigation(selectedTag: MutableState<NavItem>, onItemSelect: (NavItem) -> Unit) {
            NavigationBar(Modifier.fillMaxWidth()) {
                navItems.forEach {
                    val selected = selectedTag.value == it
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onItemSelect(it) },
                        icon = {
                            Image(
                                painter = painterResource(it.icon),
                                contentDescription = null,
                                Modifier
                                    .size(28.dp)
                                    .background(color = Color.Transparent),
                                colorFilter = ColorFilter.tint(Color.Gray)
                            )
                        },
                        label = {
                            Text(
                                text = it.labelText(LocalView.current.context),
                                color = if (selected) Color.DarkGray else Color.Gray,
                            )
                        }
                    )
                }
            }
        }

        @Composable
        fun MainPageContent(navItemState: MutableState<NavItem>, modifier: Modifier = Modifier, contentSupplier: (Context) -> View) {
            navItemState.value.also { navItem ->
                val view = LocalView.current
                val fragmentManager = remember(view) { FragmentManager.findFragmentManager(view) }
                AndroidView(
                    factory = { context -> contentSupplier(context) },
                    modifier,
                    update = {
                        fragmentManager.installFragment(navItem.tag) {
                            when (navItem) {
                                NavItem.Home -> InfusionAlarmFragment()
                                NavItem.Toolbox -> ToolboxFragment()
                                NavItem.Mine -> MineFragment()
                            }
                        }
                    }
                )
//                AndroidFragment<InfusionAlarmFragment>(Modifier.fillMaxWidth())
            }
        }

        private fun FragmentManager.installFragment(tag: String, instantiate: () -> Fragment) {
            findFragmentByTag(tag)
                ?.also {
                    commit {
                        primaryNavigationFragment?.also { f ->
                            if (f != it) {
                                hide(f)
                            }
                        }
                        show(it)
                        setPrimaryNavigationFragment(it)
                    }
                }
                ?: instantiate().also {
                    commit {
                        primaryNavigationFragment?.also { f -> hide(f) }
                        add(R.id.contentMainPage, it, tag)
                        setPrimaryNavigationFragment(it)
                    }
                }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode")
@Composable
fun MainPagePreview() {
    IBDAssistantTheme {
        MainActivity.MainPage("Android") { context ->
            FragmentContainerView(context).apply {
                id = R.id.contentMainPage
            }
        }
    }
}
