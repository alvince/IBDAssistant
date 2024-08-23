package cn.alvince.ibdassistant.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.alvince.ibdassistant.R
import cn.alvince.ibdassistant.alarm.vm.InfusionAlarmViewModel
import cn.alvince.ibdassistant.ui.theme.iconButtonLarge

/**
 * Infusion alert controller [Fragment]
 *
 * Create by ZhangYang on 2024/8/21
 *
 * @author zhangyang.alvince@bytedance.com
 */
class InfusionAlarmFragment : Fragment() {

    private val viewModel: InfusionAlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.attachToLifecycle(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        content {
            AlarmScreen(viewModel)
        }

    companion object Ui {
        @Composable
        fun AlarmScreen(viewModel: InfusionAlarmViewModel) {
            Surface(Modifier.fillMaxSize()) {
                ConstraintLayout(Modifier.fillMaxSize()) {
                    val (speedText, speedSpace, timeText, countDownTimeText, middleSpace, startOrNextButton, buttonSpace, stopButton) = createRefs()
                    val startAlarm = viewModel.startedAlarm.value
                    ClockTime(viewModel.time, timeText, if (startAlarm) countDownTimeText else middleSpace)
                    if (startAlarm) {
                        CountDownTime(viewModel.countDownTime, countDownTimeText, timeText, middleSpace)
                    }
                    Spacer(
                        Modifier
                            .height(128.dp)
                            .constrainAs(middleSpace) {
                                start.linkTo(parent.start)
                                bottom.linkTo(startOrNextButton.top)
                                top.linkTo(if (startAlarm) countDownTimeText.bottom else timeText.bottom)
                            }
                    )
                    Image(
                        painterResource(if (startAlarm) R.drawable.ic_arrow_circle_right_24_fill else R.drawable.ic_alarm_add_24_fill),
                        contentDescription = null,
                        Modifier
                            .size(iconButtonLarge)
                            .constrainAs(startOrNextButton) {
                                bottom.linkTo(parent.bottom)
                                top.linkTo(middleSpace.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(if (startAlarm) stopButton.start else parent.end)
                            }
                            .clickable {
                                viewModel.startAlarmOrNext()
                            },
                        colorFilter = ColorFilter.tint(Color.Gray)
                    )
                    if (startAlarm) {
                        Spacer(
                            Modifier
                                .width(48.dp)
                                .constrainAs(buttonSpace) {
                                    start.linkTo(startOrNextButton.end)
                                    top.linkTo(startOrNextButton.top)
                                    end.linkTo(stopButton.start)
                                }
                        )
                        Image(
                            painterResource(R.drawable.ic_cancel_24_fill),
                            contentDescription = null,
                            Modifier
                                .size(iconButtonLarge)
                                .constrainAs(stopButton) {
                                    top.linkTo(startOrNextButton.top)
                                    start.linkTo(buttonSpace.end)
                                    end.linkTo(parent.end)
                                    bottom.linkTo(startOrNextButton.bottom)
                                }
                                .clickable {
                                    viewModel.stopAlarm()
                                },
                            colorFilter = ColorFilter.tint(Color.Gray)
                        )
                        Spacer(
                            Modifier
                                .height(16.dp)
                                .constrainAs(speedSpace) {
                                    start.linkTo(parent.start)
                                    bottom.linkTo(timeText.top)
                                }
                        )
                        InfusionSpeed(viewModel.speed, speedText, speedSpace)
                    }
                    if (startAlarm) {
                        createVerticalChain(timeText, countDownTimeText, middleSpace, startOrNextButton, chainStyle = ChainStyle.Packed)
                        createHorizontalChain(startOrNextButton, buttonSpace, stopButton, chainStyle = ChainStyle.Packed)
                    } else {
                        createVerticalChain(timeText, middleSpace, startOrNextButton, chainStyle = ChainStyle.Packed)
                    }
                }
            }
        }

        @Composable
        fun ConstraintLayoutScope.ClockTime(
            timeState: MutableState<String>,
            reference: ConstrainedLayoutReference,
            dependDown: ConstrainedLayoutReference
        ) {
            Text(
                timeState.value,
                Modifier
                    .fillMaxWidth()
                    .constrainAs(reference) {
                        centerHorizontallyTo(parent)
                        top.linkTo(parent.top)
                        bottom.linkTo(dependDown.top)
                    },
                fontSize = 48.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }

        @Composable
        fun ConstraintLayoutScope.CountDownTime(
            countDownSpeed: MutableState<String>,
            reference: ConstrainedLayoutReference,
            topReference: ConstrainedLayoutReference,
            bottomReference: ConstrainedLayoutReference,
        ) {
            Text(
                countDownSpeed.value,
                Modifier
                    .fillMaxWidth()
                    .constrainAs(reference) {
                        centerHorizontallyTo(parent)
                        top.linkTo(topReference.bottom)
                        bottom.linkTo(bottomReference.top)
                    },
                color = Color.Gray,
                fontSize = 28.sp,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }

        @Composable
        fun ConstraintLayoutScope.InfusionSpeed(
            speedState: MutableState<Int>,
            reference: ConstrainedLayoutReference,
            dependency: ConstrainedLayoutReference
        ) {
            Text(
                "当前调速：${speedState.value}",
                Modifier
                    .wrapContentSize()
                    .constrainAs(reference) {
                        centerHorizontallyTo(parent)
                        bottom.linkTo(dependency.top)
                    },
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview
@Composable
fun AlarmScreenPreview() {
    InfusionAlarmFragment.AlarmScreen(viewModel())
}
